package yakworks.message;

import java.util.Locale;

/**
 * Context and Holder for arguments so its easier to keep compatibility between named map based palceholders like icu4j
 * and ordinal array based placeholder like like spring and java.text.messageFormat
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public class DefaultMsgContext implements MsgContext {

    //locale for the message
    Locale locale;
    //the message code/key
    private String code;
    // stored as either a list or map
    private MsgArgs msgArgs;
    // fallback message will get rendered if code fails
    String fallbackMessage;

    public DefaultMsgContext() {}

    /** static helper for quick default message */
    public static DefaultMsgContext withFallback(String fallback){
        return new DefaultMsgContext().fallbackMessage(fallback);
    }

    @Override public Locale getLocale(){ return locale; }
    @Override public void setLocale(Locale v){ locale = v; }
    /** builder version of setting locale */
    public DefaultMsgContext locale(Locale loc){ this.locale = loc; return this; }

    public void setCode(String v) { code = v; }
    @Override public String getCode() { return code; }

    @Override public MsgArgs getArgs(){
        if(msgArgs == null) msgArgs = MsgArgs.empty();
        return msgArgs;
    }

    @Override public void setArgs(MsgArgs v){ this.msgArgs = v; }

    public DefaultMsgContext args(Object args){ setArgs(args); return this; }

    public DefaultMsgContext args(MsgArgs args){ setArgs(args); return this; }

    public void setFallbackMessage(String v) { fallbackMessage = v; }

    /**
     * If one is set then return it,
     * if not it looks at args and if its a map then returns the defaultMessage key if it exists
     */
    @Override
    public String getFallbackMessage(){
        return (fallbackMessage != null) ? fallbackMessage : getArgs().getFallbackMessage();
    }

    /** builderish setter for fallback */
    DefaultMsgContext fallbackMessage(String defMsg){ fallbackMessage = defMsg; return this;}

    boolean useCodeAsDefaultMessage = true;
    public boolean isUseCodeAsDefaultMessage() {
        return useCodeAsDefaultMessage;
    }
    public DefaultMsgContext useCodeAsDefaultMessage(boolean v) {
        this.useCodeAsDefaultMessage = v;
        return this;
    }
}
