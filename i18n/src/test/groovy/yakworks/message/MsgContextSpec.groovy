package yakworks.message

import spock.lang.Specification

class MsgContextSpec extends Specification  {

    void 'from locale'() {
        when:
        def msgCtx = MsgContext.of(Locale.getDefault())

        then: 'should have set it up'
        msgCtx instanceof DefaultMsgContext

        when: 'fallbackMessage is set'
        msgCtx.fallbackMessage("go go go")

        then: 'args should have been setup'
        msgCtx.fallbackMessage == 'go go go'
    }

    void 'check builder 2'() {
        when:
        MsgKey msgKey = MsgKey.ofCode('named.arguments').args([name: 'foo']).fallbackMessage("foo")
        def msgCtx = MsgContext.of(msgKey)

        then:
        !msgCtx.code
        msgKey.args.get() == msgCtx.args.get()
        msgKey.fallbackMessage == msgCtx.fallbackMessage
    }

}
