package com.transferwise.icu;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;
import java.util.Map;

/**
 * Extends the {@link org.springframework.context.MessageSource} interface in that adds util for passing
 * arguments as Map
 */
public interface ICUMessageSource extends HierarchicalMessageSource {

    /**
     * Try to resolve the message. Return default message if no message was found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
     * this class are encouraged to base message names on the relevant fully
     * qualified class name, thus avoiding conflict and ensuring maximum clarity.
     * @param args map of arguments that will be filled in for params within
     * the message (params look like "{name}", "{birthday,date}", "{meeting,time}" within a message),
     * or {@code null} if none.
     * @param defaultMessage String to return if the lookup fails
     * @param locale the Locale in which to do the lookup
     * @return the resolved message if the lookup was successful;
     * otherwise the default message passed as a parameter
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Map<String, Object> args, String defaultMessage, Locale locale);

    /**
     * Try to resolve the message. Treat as an error if the message can't be found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'
     * @param args Map of arguments that will be filled in for params within
     * the message (params look like "{name}", "{birthday,date}", "{meeting,time}" within a message),
     * or {@code null} if none.
     * @param locale the Locale in which to do the lookup
     * @return the resolved message
     * @throws org.springframework.context.NoSuchMessageException if the message wasn't found
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Map<String, Object> args, Locale locale) throws NoSuchMessageException;
}
