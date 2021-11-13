package yakworks.i18n;

import javax.annotation.Nullable;
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
public interface MsgKey {

    String getCode();

    /**
     * Return the Map of arguments to be used to resolve this message as ICU.
     * A default message can also be in the map params as a 'defaultMessage' key.
     */
    @Nullable
    default Map getArgs() {
        return null;
    }

    /**
     * Make key form code
     */
    static DefaultMsgKey of(String code){
        return new DefaultMsgKey(code);
    }

}
