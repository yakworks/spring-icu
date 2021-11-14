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
public interface MsgContext {
    //option to pass in locale
    default Locale getLocale() { return null; }
    MsgContext locale(Locale loc);

    // stored as either a list or map
    Object getArgs();
    //builder to add the args
    MsgContext args(Object args);

    static MsgContext of(Object args){
        return new DefaultMsgContext().args(args);
    }

    static MsgContext empty(){
        return new DefaultMsgContext();
    }

    /**
     * fallbackMessage is the same as defaultMessage for example in spring.
     * Its name as a fallback as thats what it is and should not really be used or leaned on, it means the i18n is not configured correctly
     * If one is set here then return it, if not it looks at args and if its a map then returns the 'fallbackMessage' key if it exists
     */
    String getFallbackMessage();

    MsgContext fallbackMessage(String defMsg);

    default boolean isEmpty() {
        return MsgContext.isEmpty(getArgs());
    }

    /**
     * transforms the arguments with the transformation Function and returns a new MsgContext.
     * Used to spin through args and look them up to for message.properties
     */
    default MsgContext transform(Function transformation) {
        return getArgs() instanceof Map ? transformMap(transformation) : transformList(transformation);

    }

    default MsgContext transformMap(Function transformation) {
        Map<Object, Object> curArgMap = (Map<Object, Object>)getArgs();
        Map newArgs = new LinkedHashMap<>(curArgMap.size());
        for (Map.Entry item: curArgMap.entrySet()) {
            newArgs.put(item.getKey(), transformation.apply(item.getValue()));
        }
        return MsgContext.of(newArgs).locale(getLocale());
    }

    default MsgContext transformList(Function transformation) {
        List curArgList = (List)getArgs();
        List<Object> newArgs = new ArrayList<Object>(curArgList.size());
        for (Object item : curArgList)
            newArgs.add(transformation.apply(item));
        return MsgContext.of(newArgs).locale(getLocale());
    }

    /**
     * calls format on the passed in messageFormat. works with ICU and stock java one spring uses
     */
    default String formatWith(java.text.Format messageFormat) {
        //its either a list or a map
        return getArgs() instanceof Map ? messageFormat.format((Map)getArgs()) : messageFormat.format(toArray());
    }

    /**
     * converts the args list to array
     */
    default Object[] toArray() {
        if(getArgs() == null) return new Object[0];
        return ((List)getArgs()).toArray(new Object[((List)getArgs()).size()]);
    }

    /**
     * Checks if args is Array or List and if the first item is a map,
     * if so then it should use that map for the args and ignores the rest. Used for compatibility with Spring tempaltes
     * where is can only pass arrays for args
     */
    static boolean isFirstItemMap(Object... args) {
        return args.length == 1 && args[0] instanceof Map;
    }

    static boolean isEmpty(Object obj) {
        if (obj == null) return true;
        if (obj.getClass().isArray()) return Array.getLength(obj) == 0;
        if (obj instanceof Collection) return ((Collection) obj).isEmpty();
        if (obj instanceof Map) return ((Map) obj).isEmpty();
        // else
        return false;
    }

    static boolean isArray(Object obj) {
        return (obj != null && obj.getClass().isArray());
    }

}
