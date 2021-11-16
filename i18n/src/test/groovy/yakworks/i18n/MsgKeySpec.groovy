package yakworks.i18n

import spock.lang.Specification

class MsgKeySpec extends Specification  {

    void 'check builder'() {
        when: "of static is called on ICUMsgKey"
        MsgKey msgKey = MsgKey.of('named.arguments')

        then: 'should have set it up'
        msgKey instanceof DefaultMsgKey
        msgKey.code == 'named.arguments'
        msgKey.args == null

        when: 'def msg is set'
        msgKey.fallbackMessage("go go go")

        then: 'args should have been setup'
        msgKey.fallbackMessage == 'go go go'
    }

    void 'check builder 2'() {
        when: "of static is called on ICUMsgKey"
        MsgKey msgKey = MsgKey.of('named.arguments').args([name: 'foo'])

        then: 'should have set it up'
        msgKey.code == 'named.arguments'
        msgKey.args == [name:'foo']

        when: 'def msg is set'
        msgKey.fallbackMessage("go")

        then: 'args should have been setup'
        msgKey.fallbackMessage == 'go'
    }

    void 'check fallback in map'() {
        when: "of static is called on ICUMsgKey"
        MsgKey msgKey = MsgKey.of('named.arguments').args([name: 'foo', fallbackMessage: 'go'])

        then: 'should have set it up'
        msgKey.code == 'named.arguments'
        msgKey.args == [name:'foo', fallbackMessage: 'go']
        msgKey.fallbackMessage == 'go'

    }

}
