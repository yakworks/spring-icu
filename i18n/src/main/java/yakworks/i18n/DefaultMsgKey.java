package yakworks.i18n;

/**
 * Default implementation of the MsgKey, normally should be build with
 * MsgKey.of('somekey',....), not directly with this class
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public class DefaultMsgKey implements MsgKey {

    public DefaultMsgKey() {}
    public DefaultMsgKey(String code) {
        this.code = code;
    }

    private String code;

    public void setCode(String v) { code = v; }
    @Override public String getCode() { return code; }
    DefaultMsgKey code(String code){ this.code = code; return this;}

    // stored as either a list or map
    private MsgArgs msgArgs;

    @Override public MsgArgs getArgs(){
        if(msgArgs == null) msgArgs = MsgArgs.empty();
        return msgArgs;
    }
    @Override public void setArgs(MsgArgs v){ this.msgArgs = v; }

    public DefaultMsgKey args(Object args){ setArgs(args); return this; }

    public DefaultMsgKey args(MsgArgs args){ setArgs(args); return this; }

    // fallback message will get rendered if code fails
    String fallbackMessage;
    public void setFallbackMessage(String v) { fallbackMessage = v; }
    /**
     * If one is set then return it,
     * if not it looks at args and if its a map then returns the defaultMessage key if it exists
     */
    @Override
    public String getFallbackMessage(){
        return (fallbackMessage != null) ? fallbackMessage : getArgs().getFallbackMessage();
    }

    public DefaultMsgKey fallbackMessage(String defMsg){ fallbackMessage = defMsg; return this;}
}
