package yakworks.message;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An arg wrapper that allows the args to be an array, List or Map
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public class MsgArgs {

    // will either be a list or map
    private Object value;

    public MsgArgs() { }

    public static MsgArgs of(Object args){
        return new MsgArgs().args(args);
    }

    /**
     * similiar to Optional.
     * @return returns the value of args
     * @throws NoSuchElementException if args is null
     */
    public Object get() {
        if (value == null) {
            throw new NoSuchElementException("No value present, call args() to set first");
        }
        return value;
    }

    // public boolean isPresent() {
    //     return value != null;
    // }

    /**
     * sets the args, if array converts to List.
     * @param args array list or map
     */
    public void setArgs(Object args){
        if(MsgArgs.isEmpty(args)) {
            args = new LinkedHashMap<>();
        } else if(MsgArgs.isArray(args)){
            Object[] argsray = (Object[]) args;
            //if first item is map the use that otherwise make array list
            args = MsgArgs.isFirstItemMap(argsray) ? (Map)argsray[0] : Arrays.asList(argsray);
        }
        if(args instanceof Map || args instanceof List){
            this.value = args;
        } else {
            throw new IllegalArgumentException("Message arguments must be a Map, List or Object array");
        }
    }

    /**
     * sets args
     * @see #setArgs(Object)
     */
    public MsgArgs args(Object args){
        setArgs(args);
        return this;
    }

    public boolean isEmpty() {
        return MsgArgs.isEmpty(value);
    }

    public boolean isMap() { return value instanceof Map; }

    /**
     * calls messageFormat.format on the passed in messageFormat.
     * if the args is a list then it tranforms it to an array if map then passed it straight in.
     */
    public String formatWith(java.text.Format messageFormat) {
        //its either a list or a map
        return isMap() ? messageFormat.format((Map)value) : messageFormat.format(toArray());
    }

    /**
     * converts the args list to array
     */
    public Object[] toArray() {
        if(value == null) return new Object[0];
        return ((List)value).toArray(new Object[((List)value).size()]);
    }

    /**
     * if args is null or empty then this initializes it to a map for names args
     * should check that return map as null means it didnt succeed
     * @return the initialized Map reference, null if its a list arg
     */
    public Map asMap(){
        return isMap() ? (Map)value : null;
    }

    /**
     * adds an enrty to the msg arg if its a map
     * returns the map or null if its list/array based args
     */
    public Map putIfAbsent(Object key, Object val){
        Map argsMap = asMap();
        if(argsMap != null) argsMap.putIfAbsent(key, val);
        return argsMap;
    }

    /**
     * adds an arg to the map, see getArgMap, will set one up
     * @return the args as map
     */
    public Map put(Object key, Object val){
        Map argMap = asMap();
        if(argMap != null) argMap.put(key, val);
        return argMap;
    }

    /**
     * static helper to lookup the fallback in the args if its a map.
     * Looks for fallbackMessage or defaultMessage key, in that order
     */
    public String getFallbackMessage(){
        if (isMap()){
            Map argMap = asMap();
            if(!argMap.isEmpty()) {
                if (argMap.containsKey("fallbackMessage")) return (String) argMap.get("fallbackMessage");
                if (argMap.containsKey("defaultMessage")) return (String) argMap.get("defaultMessage");
            }
        }
        return null;
    }

    /**
     * gets an instance initialzed with and empty map
     */
    public static MsgArgs empty(){
        return new MsgArgs().args(null);
    }

    /**
     * Checks if args is Array or List and if the first item is a map,
     * if so then it should use that map for the args and ignores the rest. Used for compatibility with Spring tempaltes
     * where is can only pass arrays for args
     */
    public static boolean isFirstItemMap(Object... args) {
        return args.length == 1 && args[0] instanceof Map;
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) return true;
        if (obj.getClass().isArray()) return Array.getLength(obj) == 0;
        if (obj instanceof Collection) return ((Collection) obj).isEmpty();
        if (obj instanceof Map) return ((Map) obj).isEmpty();
        // else
        return false;
    }

    public static boolean isArray(Object obj) {
        return (obj != null && obj.getClass().isArray());
    }

}
