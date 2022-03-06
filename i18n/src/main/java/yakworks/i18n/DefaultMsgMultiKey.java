package yakworks.i18n;

import java.util.List;

/**
 * Default implementation of the MsgKey, normally should be build with
 * MsgKey.of('somekey',....), not directly with this class
 *
 *  @author Joshua Burnett (@basejump)
 *  @since 0.3.0
 */
@SuppressWarnings("unchecked")
public class DefaultMsgMultiKey implements MsgMultiKey {
    private List<String> codes;
    private MsgKey msg;

    public DefaultMsgMultiKey() {}
    public DefaultMsgMultiKey(List<String> codes) {
        this.codes = codes;
        msg = new DefaultMsgKey();
    }
    public DefaultMsgMultiKey(MsgKey msg) {
        this.msg = msg;
    }

    public void setCodes(List<String> v) { this.codes = v; }
    @Override public List<String> getCodes() { return this.codes; }
    DefaultMsgMultiKey codes(List<String> v){ this.codes = v; return this;}

    public void setMsgKey(MsgKey v) { this.msg = v; }
    @Override public MsgKey getMsgKey() { return this.msg; }
    DefaultMsgMultiKey msgKey(MsgKey v){ this.msg = v; return this;}

}
