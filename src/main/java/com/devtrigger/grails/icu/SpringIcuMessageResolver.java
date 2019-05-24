package com.devtrigger.grails.icu;

import org.springframework.context.NoSuchMessageException;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.ConfigurationException;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.util.Validate;

import java.util.Map;

public class SpringIcuMessageResolver extends AbstractMessageResolver {

    private ICUMessageSource icuMessageSource;

    private void checkMessageSourceInitialized() {
        if (this.icuMessageSource == null) {
            throw new ConfigurationException(
                    "Cannot initialize " + SpringIcuMessageResolver.class.getSimpleName() +
                            ": IcuMessageSource has not been set. Either define this object as " +
                            "a Spring bean (which will automatically set the MessageSource) or, " +
                            "if you instance it directly, set the MessageSource manually using its "+
                            "corresponding setter method.");
        }
    }

    public final String resolveMessage(
            final ITemplateContext context, final Class<?> origin, final String key, final Object[] messageParameters) {

        Validate.notNull(context.getLocale(), "Locale in context cannot be null");
        Validate.notNull(key, "Message key cannot be null");

        if (context != null) {

            checkMessageSourceInitialized();

            try {
                /*
                 * Attempt named argument resolution first
                 */
                if (messageParameters.length == 1 && messageParameters[0] instanceof Map) {
                    return this.icuMessageSource.getMessage(key, (Map<String, Object>)messageParameters[0], context.getLocale());
                } else {
                    /*
                     * If no named arguments present, resolve with an array of messageParameters
                     */
                    return this.icuMessageSource.getMessage(key, messageParameters, context.getLocale());
                }
            } catch (NoSuchMessageException e) {
                // Return null from this resolver; fall back on other ones
                return null;
            }

        }

        /*
         * NOT FOUND, return null
         */
        return null;

    }

    public void setIcuMessageSource(final ICUMessageSource icuMessageSource) {
        this.icuMessageSource = icuMessageSource;
    }

    public String createAbsentMessageRepresentation(final ITemplateContext context, final Class<?> origin, final String key, final Object[] messageParameters) {
        return null;
    }
}

