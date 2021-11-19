package yakworks.i18n;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * add to a class that has a MsgKey reference as field msg
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public interface MsgKeyDecorator extends MsgKey {

    /**
     * the MsgKey
     */
    MsgKey getMsg();
    default void setMsg(MsgKey msg) {}

    default String getCode() {
        return getMsg().getCode();
    }
    default void setCode(String v){
        getMsg().setCode(v);
    }

    default MsgArgs getArgs(){
        return getMsg().getArgs();
    }
    default void setArgs(MsgArgs v){
        getMsg().setArgs(v);
    }


}
