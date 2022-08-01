package yakworks.message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * A MsgKey but with a Locale. Also has a transform helper to do lookup when args are also 'message keys'  that should
 * be looked up first before being passed as args for main message.
 * Context and Holder for arguments so its easier to keep compatibility between named map based palceholders like icu4j
 * and ordinal array based placeholder like spring and java.text.messageFormat
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public interface MsgContext extends MsgKey{
    //option to pass in locale
    default Locale getLocale() { return null; }
    default void setLocale(Locale v){
        throw new UnsupportedOperationException("setter not implemented");
    }
    // E locale(Locale loc);

    static DefaultMsgContext of(Locale loc){
        return new DefaultMsgContext().locale(loc);
    }

    static DefaultMsgContext of(Object args){
        return new DefaultMsgContext().args(args);
    }

    /**
     * makes context using the args and fallback message from msgKey.
     * Does NOT copy the code, just the args and fallback
     */
    static DefaultMsgContext of(MsgKey msgKey){
        return new DefaultMsgContext().args(msgKey.getArgs()).fallbackMessage(msgKey.getFallbackMessage());
    }

    // static DefaultMsgContext empty(){
    //     return new DefaultMsgContext();
    // }

    /**
     * transforms the arguments with the transformation Function and returns a new MsgContext.
     * Used when the args are also message keys (such as a MessageSourceResolvable) and they need
     * to inturn be looked up message.properties before being passed as args for the primary message
     */
    default MsgContext transform(Function transformation) {
        return getArgs().isMap() ? transformMap(transformation) : transformList(transformation);

    }

    /**
     * called from transform when the args are mapped based
     */
    default MsgContext transformMap(Function transformation) {
        Map<Object, Object> curArgMap = (Map<Object, Object>)getArgs().get();
        Map newArgs = new LinkedHashMap<>(curArgMap.size());
        for (Map.Entry item: curArgMap.entrySet()) {
            newArgs.put(item.getKey(), transformation.apply(item.getValue()));
        }
        return MsgContext.of(newArgs).locale(getLocale());
    }

    /**
     * called from transform when the args are array list based.
     */
    default MsgContext transformList(Function transformation) {
        List curArgList = (List)getArgs().get();
        List<Object> newArgs = new ArrayList<Object>(curArgList.size());
        for (Object item : curArgList)
            newArgs.add(transformation.apply(item));
        return MsgContext.of(newArgs).locale(getLocale());
    }

    boolean isUseCodeAsDefaultMessage();

}
