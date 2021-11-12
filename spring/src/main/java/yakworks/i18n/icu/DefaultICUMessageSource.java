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

import org.springframework.context.MessageSourceResolvable;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

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

    @Override //MessageSourceSupport
    protected String renderDefaultMessage(String defaultMessage, @Nullable Object[] args, Locale locale) {
        return formatMessage(defaultMessage, ICUArgsHolder.of(args), locale);
    }

    //should never get hit now with overrides so throw UnsupportedOperationException just in case
    @Override //MessageSourceSupport
    protected String formatMessage(String msg, @Nullable Object[] args, Locale locale) {
        throw new UnsupportedOperationException("Use formatMessage with ICUMessageArguments");
    }

    //should never get hit now with overrides so throw UnsupportedOperationException just in case we missed something
    @Override // AbstractMessageSource
    protected Object[] resolveArguments(@Nullable Object[] args, Locale locale) {
        throw new UnsupportedOperationException("caller methods should have been overriden");
    }

    // overrides to always pull from LocaleContextHolder.getLocale()
    @Nullable
    @Override // AbstractResourceBasedMessageSource
    protected Locale getDefaultLocale() {
        return getHolderLocale();
    }

    protected String formatMessage(String msg, ICUArgsHolder args, Locale locale) {
        locale = checkLocale(locale);
        if (!isAlwaysUseMessageFormat() && ObjectUtils.isEmpty(args)) {
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
            return args.formatWith(messageFormat);
        }
    }

    /**
     * used for new map based methods and calls with a defaultMessage.
     * defaultMessage can be null and will use the code itself as a message.
     */
    @Override //ICUMessageSource
    public String getICUMessage(String code, ICUArgsHolder args, @Nullable String defaultMessage, Locale locale) {
        String msg = getMessageInternal(code, args, locale);
        if (msg != null) {
            return msg;
        }
        if (defaultMessage == null) {
            //see if key exists in args
            if(args.getDefaultMessage() != null){
                return formatMessage(args.getDefaultMessage(), args, locale);
            } else {
                return getDefaultMessage(code);
            }

        }
        return formatMessage(defaultMessage, args, locale);
    }

    @Override //AbstractMessageSource
    @Nullable
    protected String getMessageInternal(@Nullable String code, @Nullable Object[] args, @Nullable Locale locale) {
        return getMessageInternal(code, ICUArgsHolder.of(args), locale);
    }

    @Nullable
    protected String getMessageInternal(@Nullable String code, ICUArgsHolder args, @Nullable Locale locale) {
        if (code == null) {
            return null;
        }

        locale = checkLocale(locale);

        ICUArgsHolder argsToUse = args;

        if (!isAlwaysUseMessageFormat() && args.isEmpty()) {
            // Optimized resolution: no arguments to apply,
            // therefore no MessageFormat needs to be involved.
            // Note that the default implementation still uses MessageFormat;
            // this can be overridden in specific subclasses.
            String message = resolveCodeWithoutArguments(code, locale);
            if (message != null) {
                return message;
            }
        }
        else {
            // Resolve arguments eagerly, for the case where the message
            // is defined in a parent MessageSource but resolvable arguments
            // are defined in the child MessageSource.
            argsToUse = resolveArguments(args, locale);

            com.ibm.icu.text.MessageFormat messageFormat = resolveCodeICU(code, locale);
            if (messageFormat != null) {
                synchronized (messageFormat) {
                    return argsToUse.formatWith(messageFormat);
                }
            }
        }

        // Check locale-independent common messages for the given message code.
        Properties commonMessages = getCommonMessages();
        if (commonMessages != null) {
            String commonMessage = commonMessages.getProperty(code);
            if (commonMessage != null) {
                return formatMessage(commonMessage, args, locale);
            }
        }

        // TODO Not implemented yet Not found -> check parent, if any.
        // return getMessageFromParent(code, argsToUse, locale);
        return null;
    }

    protected ICUArgsHolder resolveArguments(ICUArgsHolder args, Locale locale) {
        return args.transform( item -> {
            if (item instanceof MessageSourceResolvable)
                return getMessage((MessageSourceResolvable) item, locale);
            return item;
        });
    }

}
