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
public interface MsgKey {

    String getCode();
    default void setCode(String code){
        throw new UnsupportedOperationException("setter not implemented");
    }

    /**
     * Return the Map of arguments to be used to resolve this message as ICU.
     * A default message can also be in the map params as a 'defaultMessage' key.
     */
    @Nullable
    default MsgArgs getArgs() {
        return null;
    }
    default void setArgs(MsgArgs v){
        throw new UnsupportedOperationException("setter not implemented");
    }
    default void setArgs(Object v){
        setArgs(MsgArgs.of(v));
    }

    /**
     * fallbackMessage is the same as defaultMessage for example in spring.
     * Its name as a fallback as thats what it is and should not really be used or leaned on, it means the i18n is not configured correctly
     * If one is set here then return it, if not it looks at args and if its a map then returns the 'fallbackMessage' key if it exists
     */
    default String getFallbackMessage(){ return null; }
    default void setFallbackMessage(String v) {
        throw new UnsupportedOperationException("setter not implemented");
    }
    // default void setFallbackMessage(String defMsg){ }

    /**
     * Make key form code
     */
    static DefaultMsgKey empty(){
        return new DefaultMsgKey();
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
    static DefaultMsgKey of(String code, Object args){
        return new DefaultMsgKey(code).args(args);
    }

}
