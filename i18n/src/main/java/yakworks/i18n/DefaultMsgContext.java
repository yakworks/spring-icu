package yakworks.i18n;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Context and Holder for arguments so its easier to keep compatibility between named map based palceholders like icu4j
 * and ordinal array based placeholder like like spring and java.text.messageFormat
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public class DefaultMsgContext implements MsgContext<DefaultMsgContext> {

    public DefaultMsgContext() {}

    Locale locale;
    @Override public Locale getLocale(){ return locale; }
    @Override public DefaultMsgContext locale(Locale loc){ this.locale = loc; return this; }

    private String code;
    public void setCode(String v) { code = v; }
    @Override public String getCode() { return code; }

    // stored as either a list or map
    private MsgArgs msgArgs;

    @Override public MsgArgs getArgs(){
        if(msgArgs == null) msgArgs = MsgArgs.empty();
        return msgArgs;
    }

    @Override public void setArgs(MsgArgs v){ this.msgArgs = v; }

    public DefaultMsgContext args(Object args){ setArgs(args); return this; }

    public DefaultMsgContext args(MsgArgs args){ setArgs(args); return this; }

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

    DefaultMsgContext fallbackMessage(String defMsg){ fallbackMessage = defMsg; return this;}
}
