package yakworks.i18n;

import java.util.Locale;

/**
 * Similiar to org.springframework.context.MessageSource but no dependencies
 * so can be used outside spring, micronaut and grails without needing to depend on any framework
 *
 * @author Joshua Burnett (@basejump)
 * @since 0.3.0
 */
@SuppressWarnings("unchecked")
public interface MsgService {

    /**
     * This is the Main/primary getMessage method that needs to be implements and that the rest flow through.
     */
    String getMessage(String code, MsgContext msgContext);

    /**
     * shorter alias to getMessage
     */
    default String get(String code, MsgContext msgContext){
        return getMessage(code, msgContext);
    }

    /**
     * gets the message using a MsgKey, will make a context using the args and fallback in the msgKey.
     * Will use default Locale in the LocaleHolder
     */
    default String get(String code){
        return get(MsgKey.ofCode(code));
    }

    /**
     * gets the message using a MsgKey, will make a context using the args and fallback in the msgKey.
     * Will use default Locale in the LocaleHolder
     */
    default String get(MsgKey msgKey){
        return getMessage(msgKey.getCode(), MsgContext.of(msgKey));
    }

    /**
     * gets the message using MsgContext, which contains args and locale as well
     */
    default String get(MsgContext context){
        return getMessage(context.getCode(), context);
    }

    default String get(MsgKey msgKey, Locale locale){
        return getMessage(msgKey.getCode(), MsgContext.of(msgKey).locale(locale));
    }

    // support the spring way and allows anything to be passed to args and the MsgArgHolder will try and sort it out
    default String get(String code, Object args, String fallbackMessage){
        return getMessage(code, MsgContext.of(args).fallbackMessage(fallbackMessage));
    }

    default String get(String code, Object args){
        return getMessage(code, MsgContext.of(args));
    }

    /**
     * Process the string template through the prefered i18n engine..
     *
     * @param template the string template to process with args in MsgContext
     * @param context the msgContext
     * @return the translated message
     */
    String interpolate(String template, MsgContext context);

}
