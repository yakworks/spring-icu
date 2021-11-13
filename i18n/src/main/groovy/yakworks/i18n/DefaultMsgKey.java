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
public class DefaultMsgKey implements MsgKey {

    private final String code;
    private Map args;

    public DefaultMsgKey(String code) { this.code = code; }

    @Override
    public String getCode() { return code; }

    @Override
    public Map getArgs() { return args; }
    public void setArgs(Map v) { args = v; }

    DefaultMsgKey args(Map v) { args = v; return this;}

    /**
     * sets the defaultMessage key in the map, creates an arg map if none exists
     */
    DefaultMsgKey defaultMessage(String defMsg) {
        return (DefaultMsgKey)MsgKeyUtils.defaultMessage(this, defMsg);
    }
}
