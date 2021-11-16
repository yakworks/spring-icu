package yakworks.i18n;

import java.util.Locale;

/**
 * Similiar to org.springframework.context.MessageSource but no dependencies so can be used across spring, micronaut and grails
 *
 * @author Joshua Burnett (@basejump)
 * @since 0.3.0
 */
@SuppressWarnings("unchecked")
public interface MsgService {


    default String getMessage(String code){
        return getMessage(MsgKey.of(code));
    }

    default String getMessage(MsgKey msgKey){
        return getMessage(msgKey.getCode(), MsgContext.of(msgKey));
    }

    default String getMessage(MsgKey msgKey, Locale locale){
        return getMessage(msgKey.getCode(), MsgContext.of(msgKey).locale(locale));
    }

    // support the spring way and allows anything to be passed to args and the MsgArgHolder will try and sort it out
    default String getMessage(String code, Object args, String fallbackMessage){
        return getMessage(code, MsgContext.of(args).fallbackMessage(fallbackMessage));
    }

    default String getMessage(String code, Object args){
        return getMessage(code, MsgContext.of(args));
    }

    /**
     * This is the primary getMessage that the rest flow through.
     */
    String getMessage(String code, MsgContext args);

    /**
     * Process the string template through whatever i18n engine your are using.
     *
     * @param template the string template to process with args in MsgContext
     * @param context the msgContext
     * @return the translated message
     */
    String interpolate(String template, MsgContext context);

}
