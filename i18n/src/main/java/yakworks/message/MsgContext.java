package yakworks.message;

import java.util.*;
import java.util.function.Function;

/**
 * Context and Holder for arguments so its easier to keep compatibility between named map based palceholders like icu4j
 * and ordinal array based placeholder like like spring and java.text.messageFormat
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public class MsgContext implements MsgKey {

    //locale for the message
    Locale locale;
    //the message code/key
    private String code;
    // stored as either a list or map
    private MsgArgs msgArgs;
    // fallback message will get rendered if code fails
    String fallbackMessage;

    public MsgContext() {}

    /** static helper for quick default message */
    public static MsgContext withFallback(String fallback){
        return new MsgContext().fallbackMessage(fallback);
    }

    public Locale getLocale(){ return locale; }
    public void setLocale(Locale v){ locale = v; }
    /** builder version of setting locale */
    public MsgContext locale(Locale loc){ this.locale = loc; return this; }

    public void setCode(String v) { code = v; }
    @Override public String getCode() { return code; }

    @Override public MsgArgs getArgs(){
        if(msgArgs == null) msgArgs = MsgArgs.empty();
        return msgArgs;
    }

    @Override public void setArgs(MsgArgs v){ this.msgArgs = v; }

    public MsgContext args(Object args){ setArgs(args); return this; }

    public MsgContext args(MsgArgs args){ setArgs(args); return this; }

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
    MsgContext fallbackMessage(String defMsg){ fallbackMessage = defMsg; return this;}

    boolean useCodeAsDefaultMessage = true;
    public boolean isUseCodeAsDefaultMessage() {
        return useCodeAsDefaultMessage;
    }
    public MsgContext useCodeAsDefaultMessage(boolean v) {
        this.useCodeAsDefaultMessage = v;
        return this;
    }


    public static MsgContext of(Locale loc){
        return new MsgContext().locale(loc);
    }

    public static MsgContext of(Object args){
        return new MsgContext().args(args);
    }

    /**
     * makes context using the args and fallback message from msgKey.
     * Does NOT copy the code, just the args and fallback
     */
    public static MsgContext of(MsgKey msgKey){
        return new MsgContext().args(msgKey.getArgs()).fallbackMessage(msgKey.getFallbackMessage());
    }

    // static DefaultMsgContext empty(){
    //     return new DefaultMsgContext();
    // }

    /**
     * transforms the arguments with the transformation Function and returns a new MsgContext.
     * Used when the args are also message keys (such as a MessageSourceResolvable) and they need
     * to inturn be looked up message.properties before being passed as args for the primary message
     */
    public MsgContext transform(Function transformation) {
        return getArgs().isMap() ? transformMap(transformation) : transformList(transformation);

    }

    /**
     * called from transform when the args are mapped based
     */
    public MsgContext transformMap(Function transformation) {
        Map<Object, Object> curArgMap = (Map<Object, Object>)getArgs().get();
        Map newArgs = new LinkedHashMap<>(curArgMap.size());
        for (Map.Entry item: curArgMap.entrySet()) {
            newArgs.put(item.getKey(), transformation.apply(item.getValue()));
        }
        return new MsgContext().args(newArgs).locale(getLocale());
    }

    /**
     * called from transform when the args are array list based.
     */
    public MsgContext transformList(Function transformation) {
        List curArgList = (List)getArgs().get();
        List<Object> newArgs = new ArrayList<Object>(curArgList.size());

        for (Object item : curArgList)
            newArgs.add(transformation.apply(item));

        return new MsgContext().args(newArgs).locale(getLocale());
    }

}
