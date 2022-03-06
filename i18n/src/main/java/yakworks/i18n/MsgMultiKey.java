package yakworks.i18n;

import javax.annotation.Nullable;
import java.util.List;

/**
 * MsgKey that has multiple codes to lookup.
 * Should use order to look them up
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public interface MsgMultiKey extends MsgKey {

    List<String> getCodes();
    default void setCodes(List<String> code){
        throw new UnsupportedOperationException("setter not implemented");
    }

    /**
     * the wrapped MsgKey
     */
    MsgKey getMsgKey();
    default void setMsgKey(MsgKey msg) { throw new UnsupportedOperationException("setter not implemented"); }

    default String getCode() {
        return getMsgKey().getCode();
    }
    default void setCode(String v){
        getMsgKey().setCode(v);
    }

    default MsgArgs getArgs(){
        return getMsgKey().getArgs();
    }
    default void setArgs(MsgArgs v){
        getMsgKey().setArgs(v);
    }

    default String getFallbackMessage(){
        return getMsgKey().getFallbackMessage();
    }
    default void setFallbackMessage(String v){
        getMsgKey().setFallbackMessage(v);
    }

    /**
     * key from code and msgKey
     */
    static DefaultMsgMultiKey of(MsgKey msgKey){
        return new DefaultMsgMultiKey(msgKey);
    }

    static DefaultMsgMultiKey ofCodes(List<String> codes){
        return new DefaultMsgMultiKey(codes);
    }
}
