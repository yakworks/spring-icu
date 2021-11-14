package yakworks.i18n;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default implementation of the MsgKey, normally should be build with
 * MsgKey.of('somekey',....), not directly with this class
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public class DefaultMsgKey implements MsgKey<DefaultMsgKey> {

    private String code;
    @Override public void setCode(String v) { code = v; }
    @Override public String getCode() { return code; }

    private Map args;
    @Override public void setArgs(Map v) { args = v; }
    @Override public Map getArgs() { return args; }

    public DefaultMsgKey(String code) { this.code = code; }

}
