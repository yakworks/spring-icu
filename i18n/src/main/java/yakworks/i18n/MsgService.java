package yakworks.i18n;

import java.util.List;
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

    // support the spring way and allows anything to be passed to args and the MsgArgHolder will try and sort it out
    default String get(String code, Object args, String fallbackMessage){
        return getMessage(code, MsgContext.of(args).fallbackMessage(fallbackMessage));
    }

    default String get(String code, Object args){
        return getMessage(code, MsgContext.of(args));
    }

    /**
     * Get first found message for multiKey
     */
    default String get(MsgMultiKey msgMultiKey){
        List<String> codes = msgMultiKey.getCodes();
        if (codes != null) {
            String lastCode = "";
            for (String code : codes) {
                lastCode = code;
                String message = get(code, MsgContext.of(msgMultiKey).useCodeAsDefaultMessage(false));
                if (message != null) {
                    return message;
                }
            }
            // if we got here then nothing found, if spring service has useCodeAsDefaultMessage
            // then run again and if true will return the code, otherwise null.
            return get(lastCode, MsgContext.of(msgMultiKey).useCodeAsDefaultMessage(true));
        }
        return null;
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
