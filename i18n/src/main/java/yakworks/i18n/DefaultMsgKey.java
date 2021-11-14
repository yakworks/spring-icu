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

    private String code;
    private Map args;

    public DefaultMsgKey(String code) { this.code = code; }

    @Override
    public String getCode() { return code; }
    //should be rare but we are allowed to change the code
    DefaultMsgKey code(String v) { code = v; return this;}

    @Override
    public Map getArgs() { return args; }
    public void setArgs(Map v) { args = v; }
    DefaultMsgKey args(Map v) { args = v; return this;}

    /**
     * This should also be rare, but this sets the fallbackMessage key in the arg map, creates an arg map if none exists.
     * The idea here is that if code lookup fails then this can be the fallback
     */
    DefaultMsgKey fallbackMessage(String defMsg) {
        if(defMsg != null) {
            if (args == null) {
                args = new LinkedHashMap<>();
            }
            args.put("defaultMessage", defMsg);
        }
        return this;
    }
}
