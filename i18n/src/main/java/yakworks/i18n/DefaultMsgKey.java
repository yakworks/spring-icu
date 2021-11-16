package yakworks.i18n;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

    public DefaultMsgKey() {}
    public DefaultMsgKey(String code) { this.code = code; }

    private String code;
    @Override public void setCode(String v) { code = v; }
    @Override public String getCode() { return code; }

    // stored as either a list or map
    Object args;
    @Override public Object getArgs(){ return args; }
    @Override public void setArgs(Object v) { args = v; }

    // fallback message will get rendered if code fails
    String fallbackMessage;
    @Override public void setFallbackMessage(String v) { fallbackMessage = v; }
    /**
     * If one is set then return it,
     * if not it looks at args and if its a map then returns the defaultMessage key if it exists
     */
    @Override
    public String getFallbackMessage(){
        return MsgKey.getFallbackMessage(fallbackMessage, getArgs());
    }
}
