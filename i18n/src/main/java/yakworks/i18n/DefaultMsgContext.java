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
public class DefaultMsgContext implements MsgContext {
    //option to pass in locale
    Locale locale;

    // stored as either a list or map
    Object args;

    // fallback message will get rendered if code fails
    String fallbackMessage;

    public DefaultMsgContext() {}

    @Override
    public Object getArgs(){ return args; }

    @Override
    public DefaultMsgContext args(Object args){
        if(MsgContext.isEmpty(args)) {
            args = Collections.emptyMap();
        } else if(MsgContext.isArray(args)){
            Object[] argsray = (Object[])args;
            //if first item is map the use that otherwise make array list
            args = MsgContext.isFirstItemMap(argsray) ? (Map)argsray[0] : Arrays.asList(argsray);
        }
        if(args instanceof Map || args instanceof List){
            this.args = args;
        } else {
            throw new IllegalArgumentException("Message arguments must be a Map, List or Object array");
        }
        return this;
    }

    @Override
    public Locale getLocale(){ return locale; }

    @Override
    public DefaultMsgContext locale(Locale loc){ this.locale = loc; return this; }

    /**
     * If one is set then return it,
     * if not it looks at args and if its a map then returns the defaultMessage key if it exists
     */
    @Override
    public String getFallbackMessage(){
        if(fallbackMessage != null) return fallbackMessage;
        if (getArgs() instanceof Map){
            Map argMap = (Map)args;
            if(!argMap.isEmpty()) {
                if (argMap.containsKey("fallbackMessage")) return (String) argMap.get("fallbackMessage");
                if (argMap.containsKey("defaultMessage")) return (String) argMap.get("defaultMessage");
            }
        }
        return null;
    }

    @Override
    public MsgContext fallbackMessage(String fallbackMsg){
        this.fallbackMessage = fallbackMsg;
        return this;
    }
}
