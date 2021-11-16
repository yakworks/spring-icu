package yakworks.i18n;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ICUMsgKey contains the lookup code for the message and the argument map for name substitutions.
 * Can also have a defaultMessage stored as a key in the argument map
 *
 * Related to org.springframework.context.MessageSourceResolvable interface but simplified.
 * * This differs in that its simplified and skinnied down
 *  - only one code instead of array
 *  - message arguments are params and are not an array but based on keys in map
 *  - if a list or array is passed then it looks at the first element to see if its a map and uses that
 *  - no default message prop but one can be passed into the map with the key 'defaultMessage'
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public interface MsgKey<E> {

    default String getCode(){ return null; }
    default void setCode(String code){ }
    default E code(String code){ setCode(code); return (E)this;}

    /**
     * Return the Map of arguments to be used to resolve this message as ICU.
     * A default message can also be in the map params as a 'defaultMessage' key.
     */
    @Nullable
    default Object getArgs() {
        return null;
    }
    default void setArgs(Object args){ }

    default E args(Object args){
        if(MsgKey.isEmpty(args)) {
            args = Collections.emptyMap();
        } else if(MsgKey.isArray(args)){
            Object[] argsray = (Object[])args;
            //if first item is map the use that otherwise make array list
            args = MsgKey.isFirstItemMap(argsray) ? (Map)argsray[0] : Arrays.asList(argsray);
        }
        if(args instanceof Map || args instanceof List){
            this.setArgs(args);
        } else {
            throw new IllegalArgumentException("Message arguments must be a Map, List or Object array");
        }
        return (E) this;
    }

    /**
     * get the args as a Map, returns null if they are a list and not a map
     */
    default Map getArgsMap() {
        return getArgs() instanceof Map ? (Map) getArgs() : null;
    }

    /**
     * fallbackMessage is the same as defaultMessage for example in spring.
     * Its name as a fallback as thats what it is and should not really be used or leaned on, it means the i18n is not configured correctly
     * If one is set here then return it, if not it looks at args and if its a map then returns the 'fallbackMessage' key if it exists
     */
    String getFallbackMessage();

    // default E fallbackMessage(String defMsg) {
    //     if(defMsg != null) {
    //         if (getArgs() == null) {
    //             setArgs(new LinkedHashMap<>());
    //         }
    //         getArgs().put("defaultMessage", defMsg);
    //     }
    //     return (E)this;
    // }
    default void setFallbackMessage(String defMsg){ }
    default E fallbackMessage(String defMsg){ setFallbackMessage(defMsg); return (E)this;}

    default boolean isEmpty() {
        return MsgKey.isEmpty(getArgs());
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

    static String getFallbackMessage(String fieldMessage, Object args){
        if(fieldMessage != null) return fieldMessage;
        if (args instanceof Map){
            Map argMap = (Map)args;
            if(!argMap.isEmpty()) {
                if (argMap.containsKey("fallbackMessage")) return (String) argMap.get("fallbackMessage");
                if (argMap.containsKey("defaultMessage")) return (String) argMap.get("defaultMessage");
            }
        }
        return null;
    }

    /**
     * Make key form code
     */
    static DefaultMsgKey of(String code){
        return new DefaultMsgKey(code);
    }

    /**
     * key from code and map args
     */
    static DefaultMsgKey of(String code, Map args){
        return new DefaultMsgKey(code).args(args);
    }

}
