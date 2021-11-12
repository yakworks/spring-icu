package yakworks.i18n.icu;

import com.ibm.icu.text.MessageFormat;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Holder for arguments used internaly during message processing
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public class ICUArgsHolder {

    // stored as either a list or map
    Object args;

    public ICUArgsHolder() { }

    static ICUArgsHolder of(Object args){
        return new ICUArgsHolder().args(args);
    }

    public ICUArgsHolder args(Object args){
        if(ObjectUtils.isEmpty(args)) {
            args = Collections.emptyMap();
        } else if(ObjectUtils.isArray(args)){
            Object[] argsray = (Object[])args;
            //if first item is map the use that otherwise make array list
            args = isFirstItemMap(argsray) ? (Map)argsray[0] : Arrays.asList(argsray);
        }
        if(args instanceof Map || args instanceof List){
            this.args = args;
        } else {
            throw new IllegalArgumentException("Message arguments must be a Map, List or Object array");
        }
        return this;
    }

    public String getDefaultMessage(){
        if (args instanceof Map && !((Map)args).isEmpty() && ((Map)args).containsKey("defaultMessage")) {
            return (String) ((Map)args).get("defaultMessage");
        }
        return null;
    }

    /**
     * Checks if args first item is a map, if so then it uses that map for the args and ignores the rest
     */
    boolean isFirstItemMap(Object... args) {
        return args.length == 1 && args[0] instanceof Map;
    }

    public boolean isEmpty() {
        return ObjectUtils.isEmpty(args);
    }

    public ICUArgsHolder transform(Function transformation) {
        return args instanceof Map ? transformMap(transformation) : transformList(transformation);

    }

    protected ICUArgsHolder transformMap(Function transformation) {
        Map<Object, Object> curArgMap = (Map<Object, Object>)args;
        Map newArgs = new LinkedHashMap<>(curArgMap.size());
        for (Map.Entry item: curArgMap.entrySet()) {
            newArgs.put(item.getKey(), transformation.apply(item.getValue()));
        }
        return ICUArgsHolder.of(newArgs);
    }

    protected ICUArgsHolder transformList(Function transformation) {
        List curArgList = (List)args;
        List<Object> newArgs = new ArrayList<Object>(curArgList.size());
        for (Object item : curArgList)
            newArgs.add(transformation.apply(item));
        return ICUArgsHolder.of(newArgs);
    }

    public String formatWith(MessageFormat messageFormat) {
        //its either a list or a map
        return args instanceof Map ? messageFormat.format((Map)args) : messageFormat.format(toArray());
    }

    public Object[] toArray() {
        return ((List)args).toArray(new Object[((List)args).size()]);
    }
}
