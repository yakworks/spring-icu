package yakworks.i18n.icu;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import yakworks.i18n.MsgKey;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Extends the {@link org.springframework.context.MessageSource} interface in that adds util for passing
 * arguments as Map. We also add lists and some of similiar shorter helper methods
 * to {@link org.springframework.context.support.MessageSourceAccessor}
 *
 * @author Joshua Burnett (@basejump)
 * @since 0.3.0
 */
@SuppressWarnings("unchecked")
public interface ICUMessageSource extends HierarchicalMessageSource {

    /**
     * checks if local is null and returns the LocaleContextHolder.getLocale() is so
     */
    default Locale checkLocale(Locale locale) {
        return (locale != null ? locale : getHolderLocale());
    }

    default Locale getHolderLocale() {
        return LocaleContextHolder.getLocale();
    }

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
    default String getMessage(String code, Map args, String defaultMessage, Locale locale){
        return getICUMessage(code, ICUArgsHolder.of(args), defaultMessage, locale);
    }

    /**
     * calls getMessage with null defaultMessage
     */
    default String getMessage(String code, Map args, Locale locale){
        return getMessage(code, args, null, locale);
    }

    /**
     * calls getMessage with null defaultMessage and default locale
     */
    default String getMessage(String code, Map args){
        return getMessage(code, args, null, checkLocale(null));
    }

    /**
     * since we are supporting maps with a MessageArgs wrapper its inconsequential to support lists too.
     */
    default String getMessage(String code, List args, String defaultMessage, Locale locale){
        return getICUMessage(code, ICUArgsHolder.of(args), defaultMessage, locale);
    }
    default String getMessage(String code, List args, Locale locale){
        return getMessage(code, args, null, locale);
    }
    default String getMessage(String code, List args){
        return getMessage(code, args, null, checkLocale(null));
    }

    default String getMessage(String code, Locale locale){
        return getICUMessage(code, ICUArgsHolder.of(null), null, checkLocale(null));
    }

    default String getMessage(String code, String defaultMessage) {
        return getICUMessage(code, ICUArgsHolder.of(null), defaultMessage, checkLocale(null));
    }

    default String getMessage(String code) {
        return getICUMessage(code, ICUArgsHolder.of(null), null, checkLocale(null));
    }

    //create null Object versions to trap whatver comes through
    default String getMessage(String code, Object nullArg, String defaultMessage, Locale locale){
        return getICUMessage(code, ICUArgsHolder.of(null), defaultMessage, locale);
    }
    default String getMessage(String code, Object nullArg, Locale locale){
        return getMessage(code, nullArg, null, locale);
    }
    default String getMessage(String code, Object nullArg, Object nullLoc){
        return getMessage(code, nullArg, null, checkLocale(null));
    }

    /**
     * Try to resolve the message using all the attributes contained within the
     * {@code ICUMessageSourceResolvable} argument that was passed in.
     * @param msgKey the value object storing attributes required to resolve a message
     * (may include a default message)
     * @param locale the locale in which to do the lookup
     * @return the resolved message
     * This differs from the MessageSourceResolvable as it can return a null and does not throw NoSuchMessageException
     */
    @Nullable
    default String getMessage(MsgKey msgKey, Locale locale) {
        return getMessage(msgKey.getCode(), msgKey.getArgs(), null, locale);
    }

    default String getMessage(MsgKey msgKey) {
        return getMessage(msgKey.getCode(), msgKey.getArgs(), null, checkLocale(null));
    }

    /**
     * Checks if args first item is a map, if so then it uses that map for the args and ignores the rest
     */
    default boolean isNamedArgumentsMapPresent(@Nullable Object... args) {
        return args != null && args.length == 1 && args[0] instanceof Map;
    }

    /**
     * main method used for new map based methods and calls with a defaultMessage.
     * defaultMessage can be null and will use the code itself as a message.
     */
    String getICUMessage(String code, ICUArgsHolder args, @Nullable String defaultMessage, Locale locale);


}
