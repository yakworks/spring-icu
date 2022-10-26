/*
* Copyright 2002-2018 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n.icu;

import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.lang.Nullable
import org.springframework.util.ObjectUtils
import org.springframework.util.PropertyPlaceholderHelper

import yakworks.message.MsgContext

/**
 * ICU4j Overrides, Lots of copy/paste from ReloadableResourceBundleMessageSource as it wasn't made to be easy to override
 * and so much is private and final there.
 * The core difference here is that we need to return com.ibm.icu.text.MessageFormat and NOT java.text.MessageFormat
 * from many of the methods and so can't easily override
 * basically overrides what we need from {@link org.springframework.context.support.MessageSourceSupport} and
 * {@link org.springframework.context.support.AbstractMessageSource}
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
@CompileStatic
public class DefaultICUMessageSource extends ICUBundleMessageSource implements ICUMessageSource {

    public static final com.ibm.icu.text.MessageFormat INVALID_MESSAGE_FORMAT = new com.ibm.icu.text.MessageFormat("");

    private final Map<String, Map<Locale, com.ibm.icu.text.MessageFormat>> messageFormatsPerMessage = new HashMap<>();

    @Value('${yakworks.i18n.externalLocation}')
    Resource externalRoot

    @Value('${yakworks.i18n.cacheSeconds:0}')
    Integer cacheSecondsConfig

    protected ResourceLoader localResourceLoader
    PathMatchingResourcePatternResolver resourceResolver

    public DefaultICUMessageSource(){
        //set defaults
        setBasename("classpath:messages");
        setDefaultEncoding("UTF-8");
    }

    @PostConstruct
    void init() throws Exception {
        if (cacheSecondsConfig) {
            super.setCacheSeconds(cacheSecondsConfig)
        }
    }

    @Override //implement the empty mergePluginProperties which gets called first
    protected long mergeExternalProperties(final Locale locale, Properties mergedProps) {
        if(externalRoot){
            def emm = new ExternalMessagesMerger(externalRoot)
            return emm.mergeExternalProperties(locale, mergedProps)
        } else {
            return -1
        }
    }

    @Override
    void setResourceLoader(ResourceLoader resourceLoader) {
        super.setResourceLoader(resourceLoader);

        this.localResourceLoader = resourceLoader;
        if (resourceResolver == null) {
            resourceResolver = new CachingPathMatchingResourcePatternResolver(localResourceLoader);
        }
    }

    /**
     * Default icu and mapped based method
     */
    @Override //MsgService and ICUMessageSource
    public String getMessage(String code, MsgContext msgCtx) {
        String msg = getMessageInternal(code, msgCtx);
        if (msg != null) {
            return msg;
        }
        String defaultMessage = msgCtx.getFallbackMessage();
        if (defaultMessage == null) {
            if(isUseCodeAsDefaultMessage() && msgCtx.isUseCodeAsDefaultMessage()) {
                return getDefaultMessage(code);
            } else {
                return null;
            }
        }
        return interpolate(defaultMessage, msgCtx);
    }

    @Override //MessageSourceSupport
    protected String renderDefaultMessage(String defaultMessage, @Nullable Object[] args, Locale locale) {
        return interpolate(defaultMessage, MsgContext.of(args).locale(locale));
    }

    /**
     * checks if local is null and returns the LocaleContextHolder.getLocale() is so
     */
    public Locale checkLocale(Locale locale) {
        return (locale != null ? locale : getHolderLocale());
    }

    public Locale getHolderLocale() {
        return LocaleContextHolder.getLocale();
    }

    // overrides to always pull from LocaleContextHolder.getLocale()
    @Nullable
    // @Override // AbstractResourceBasedMessageSource FIXME when going to spring 5.2 change this to override
    protected Locale getDefaultLocale() {
        return getHolderLocale();
    }

    @Override //MsgService
    public String interpolate(String msg, MsgContext msgCtx) {
        Locale locale = checkLocale(msgCtx.getLocale());
        if (!isAlwaysUseMessageFormat() && ObjectUtils.isEmpty(msgCtx)) {
            return msg;
        }
        com.ibm.icu.text.MessageFormat messageFormat = null;
        synchronized (this.messageFormatsPerMessage) {
            Map<Locale, com.ibm.icu.text.MessageFormat> messageFormatsPerLocale = this.messageFormatsPerMessage.get(msg);
            if (messageFormatsPerLocale != null) {
                messageFormat = messageFormatsPerLocale.get(locale);
            }
            else {
                messageFormatsPerLocale = new HashMap<>();
                this.messageFormatsPerMessage.put(msg, messageFormatsPerLocale);
            }
            if (messageFormat == null) {
                try {
                    messageFormat = createMessageFormatICU(msg, locale);
                }
                catch (IllegalArgumentException ex) {
                    // Invalid message format - probably not intended for formatting,
                    // rather using a message structure with no arguments involved...
                    if (isAlwaysUseMessageFormat()) {
                        throw ex;
                    }
                    // Silently proceed with raw message if format not enforced...
                    messageFormat = INVALID_MESSAGE_FORMAT;
                }
                messageFormatsPerLocale.put(locale, messageFormat);
            }
        }
        if (messageFormat == INVALID_MESSAGE_FORMAT) {
            return msg;
        }
        synchronized (messageFormat) {
            return msgCtx.getArgs().formatWith(messageFormat);
        }
    }

    @Override //AbstractMessageSource
    @Nullable
    protected String getMessageInternal(@Nullable String code, @Nullable Object[] args, @Nullable Locale locale) {
        return getMessageInternal(code, MsgContext.of(args).locale(locale));
    }

    @Nullable
    protected String getMessageInternal(@Nullable String code, MsgContext msgCtx) {
        if (code == null) {
            return null;
        }

        //update the local in case we need it
        msgCtx.setLocale( checkLocale(msgCtx.getLocale()) );

        MsgContext msgCtxToUse = msgCtx;

        if (!isAlwaysUseMessageFormat() && msgCtx.getArgs().isEmpty()) {
            // Optimized resolution: no arguments to apply,
            // therefore no MessageFormat needs to be involved.
            // Note that the default implementation still uses MessageFormat;
            // this can be overridden in specific subclasses.
            String message = resolveCodeWithoutArguments(code, msgCtx.getLocale());

            if (message != null) {
                def noArgsPlaceholderResolver = { String prop ->
                    return resolveCodeWithoutArguments(prop, msgCtx.getLocale());
                } as PropertyPlaceholderHelper.PlaceholderResolver;
                return placeholderHelper.replacePlaceholders(message, noArgsPlaceholderResolver);
            }
        }
        else {
            // Resolve arguments eagerly, for the case where the message
            // is defined in a parent MessageSource but resolvable arguments
            // are defined in the child MessageSource.
            msgCtxToUse = resolveArguments(msgCtx);

            com.ibm.icu.text.MessageFormat messageFormat = resolveCodeICU(code, msgCtx.getLocale());
            if (messageFormat != null) {
                synchronized (messageFormat) {
                    return msgCtxToUse.getArgs().formatWith(messageFormat);
                }
            }
        }

        // Check locale-independent common messages for the given message code.
        Properties commonMessages = getCommonMessages();
        if (commonMessages != null) {
            String commonMessage = commonMessages.getProperty(code);
            if (commonMessage != null) {
                return interpolate(commonMessage, msgCtx);
            }
        }

        // TODO Not implemented yet Not found -> check parent, if any.
        // return getMessageFromParent(code, argsToUse, locale);
        return null;
    }

    /**
     * transforms messsage args doing interpolation formatting on each one if it implements the MessageSourceResolvable
     * @return a new MsgContext
     */
    protected MsgContext resolveArguments(MsgContext msgCtx) {
        return (MsgContext) msgCtx.transform( { item ->
            if (item instanceof MessageSourceResolvable)
                return getMessage((MessageSourceResolvable) item, msgCtx.getLocale());
            return item;
        });
    }


    //should never get hit now with overrides but throw UnsupportedOperationException just in case
    @Override //MessageSourceSupport
    protected String formatMessage(String msg, @Nullable Object[] args, Locale locale) {
        throw new UnsupportedOperationException("Use formatMessage with ICUMessageArguments");
    }

    //should never get hit now with overrides but throw UnsupportedOperationException just in case we missed something
    @Override // AbstractMessageSource
    protected Object[] resolveArguments(@Nullable Object[] args, Locale locale) {
        throw new UnsupportedOperationException("caller methods should have been overriden");
    }

    // class MessagesPlaceholderResolver implements PropertyPlaceholderHelper.PlaceholderResolver {
    //
    //     @Override
    //     public String resolvePlaceholder(String placeholderName) {
    //
    //     }
    // }
}
