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

package yakworks.icu;

import com.ibm.icu.text.MessageFormat;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
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
 */
@SuppressWarnings("unchecked")
public class DefaultICUMessageSource extends ICUBundleMessageSource implements ICUMessageSource {

    private static final com.ibm.icu.text.MessageFormat INVALID_MESSAGE_FORMAT = new com.ibm.icu.text.MessageFormat("");

    private final Map<String, Map<Locale, com.ibm.icu.text.MessageFormat>> messageFormatsPerMessage = new HashMap<>();

    /**
     * If locale is null then uses LocaleContextHolder.getLocale()
     */
    Locale checkLocale(Locale locale) {
        return (locale != null ? locale : LocaleContextHolder.getLocale());
    }

    boolean isNamedArgumentsMapPresent(@Nullable Object... args) {
        return args != null && args.length == 1 && args[0] instanceof Map;
    }

    ICUMessageArgs getICUArgs(Object[] args){
        if (isNamedArgumentsMapPresent(args)) {
            return new MapMessageArgs((Map)args[0]);
        } else {
            return new ListMessageArgs(args);
        }
    }

    @Override
    protected String renderDefaultMessage(String defaultMessage, @Nullable Object[] args, Locale locale) {
        return formatMessage(defaultMessage, getICUArgs(args), locale);
    }

    @Override
    protected String formatMessage(String msg, @Nullable Object[] args, Locale locale) {
        throw new UnsupportedOperationException("Use formatMessage with ICUMessageArguments");
    }

    /**
     * Format the given message String, using cached MessageFormats.
     * By default invoked for passed-in default messages, to resolve
     * any argument placeholders found in them.
     * @param msg the message to format
     * @param args array of arguments that will be filled in for params within
     * the message, or {@code null} if none
     * @param locale the Locale used for formatting
     * @return the formatted message (with resolved arguments)
     */
    protected String formatMessage(String msg, ICUMessageArgs args, Locale locale) {
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

    @Override
    public String getMessage(String code, @Nullable Map args, @Nullable String defaultMessage, Locale locale) {
        return getICUMessage(code, new MapMessageArgs(args), defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, @Nullable Map args, Locale locale) {
        return getICUMessage(code, new MapMessageArgs(args), null, locale);
    }

    @Override
    public final String getMessage(ICUMessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return getMessage(resolvable.getCode(), resolvable.getParams(), null, locale);
    }

    /**
     * used for new map based methods and calls with a defaultMessage.
     * defaultMessage can be null and will use the code itself as a message.
     */
    public final String getICUMessage(String code, ICUMessageArgs args, @Nullable String defaultMessage, Locale locale) {
        String msg = getMessageInternal(code, args, locale);
        if (msg != null) {
            return msg;
        }
        if (defaultMessage == null) {
            return getDefaultMessage(code);
        }
        return formatMessage(defaultMessage, args, locale);
    }

    @Override
    @Nullable
    protected String getMessageInternal(@Nullable String code, @Nullable Object[] args, @Nullable Locale locale) {
        return getMessageInternal(code, getICUArgs(args), locale);
    }

    @Nullable
    protected String getMessageInternal(@Nullable String code, ICUMessageArgs args, @Nullable Locale locale) {
        if (code == null) {
            return null;
        }

        locale = checkLocale(locale);

        ICUMessageArgs argsToUse = args;

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

        // Not found -> check parent, if any.
        return null;
    }

    protected ICUMessageArgs resolveArguments(ICUMessageArgs args, Locale locale) {
        return args.transform(new ICUMessageArgs.Transformation() {
            @Override
            public Object transform(Object item) {
                if (item instanceof MessageSourceResolvable)
                    return getMessage((MessageSourceResolvable) item, locale);
                return item;
            }
        });
    }

}
