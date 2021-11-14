/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yakworks.i18n.icu;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import yakworks.i18n.MsgContext;
import yakworks.i18n.MsgService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * ICU4j Overrides, Lost of copy paste as so much in ReloadableResourceBundleMessageSource is
 * private and final. The core issue here is that we need to return com.ibm.icu.text.MessageFormat and not java.text.MessageFormat
 * from many of the methods and so can't easily override
 * basically overrides what we need from {@link org.springframework.context.support.MessageSourceSupport} and
 * {@link org.springframework.context.support.AbstractMessageSource}
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public class DefaultICUMessageSource extends ICUBundleMessageSource implements ICUMessageSource {

    public static final com.ibm.icu.text.MessageFormat INVALID_MESSAGE_FORMAT = new com.ibm.icu.text.MessageFormat("");

    private final Map<String, Map<Locale, com.ibm.icu.text.MessageFormat>> messageFormatsPerMessage = new HashMap<>();

    public DefaultICUMessageSource(){
        //set defaults
        setBasename("classpath:messages");
        setDefaultEncoding("UTF-8");
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
            return getDefaultMessage(code);
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
            return msgCtx.formatWith(messageFormat);
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
        msgCtx.locale(checkLocale(msgCtx.getLocale()));

        MsgContext msgCtxToUse = msgCtx;

        if (!isAlwaysUseMessageFormat() && msgCtx.isEmpty()) {
            // Optimized resolution: no arguments to apply,
            // therefore no MessageFormat needs to be involved.
            // Note that the default implementation still uses MessageFormat;
            // this can be overridden in specific subclasses.
            String message = resolveCodeWithoutArguments(code, msgCtx.getLocale());
            if (message != null) {
                return message;
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
                    return msgCtxToUse.formatWith(messageFormat);
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
        return msgCtx.transform( item -> {
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

}
