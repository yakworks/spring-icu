package yakworks.i18n;

import java.util.ArrayList;
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
public interface MsgContext<E> extends MsgKey{
    //option to pass in locale
    default Locale getLocale() { return null; }
    E locale(Locale loc);

    static DefaultMsgContext of(Object args){
        return new DefaultMsgContext().args(args);
    }

    static DefaultMsgContext of(MsgKey msgKey){
        return new DefaultMsgContext().args(msgKey.getArgs()).fallbackMessage(msgKey.getFallbackMessage());
    }

    static DefaultMsgContext empty(){
        return new DefaultMsgContext();
    }

    /**
     * transforms the arguments with the transformation Function and returns a new MsgContext.
     * Used to spin through args and look them up to for message.properties
     */
    default E transform(Function transformation) {
        return getArgs().isMap() ? transformMap(transformation) : transformList(transformation);

    }

    default E transformMap(Function transformation) {
        Map<Object, Object> curArgMap = (Map<Object, Object>)getArgs().get();
        Map newArgs = new LinkedHashMap<>(curArgMap.size());
        for (Map.Entry item: curArgMap.entrySet()) {
            newArgs.put(item.getKey(), transformation.apply(item.getValue()));
        }
        return (E) MsgContext.of(newArgs).locale(getLocale());
    }

    default E transformList(Function transformation) {
        List curArgList = (List)getArgs().get();
        List<Object> newArgs = new ArrayList<Object>(curArgList.size());
        for (Object item : curArgList)
            newArgs.add(transformation.apply(item));
        return (E) MsgContext.of(newArgs).locale(getLocale());
    }

}
