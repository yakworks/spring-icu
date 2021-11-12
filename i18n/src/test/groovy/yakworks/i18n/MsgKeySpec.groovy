package yakworks.i18n

import spock.lang.Specification

class MsgKeySpec extends Specification  {

    void 'check builder'() {
        when: "of static is called on ICUMsgKey"
        MsgKey msgKey = MsgKey.of('named.arguments')

        then: 'should have set it up'
        msgKey instanceof DefaultMsgKey
        msgKey.code == 'named.arguments'
        msgKey.params == null

        when: 'def msg is set'
        msgKey.defaultMessage("go go go")

        then: 'args should have been setup'
        msgKey.params['defaultMessage'] == 'go go go'
    }

    void 'check builder 2'() {
        when: "of static is called on ICUMsgKey"
        MsgKey msgKey = MsgKey.of('named.arguments', [name: 'foo'])

        then: 'should have set it up'
        msgKey.code == 'named.arguments'
        msgKey.params == [name:'foo']

        when: 'def msg is set'
        msgKey.defaultMessage("go")

        then: 'args should have been setup'
        msgKey.params == [name:'foo', defaultMessage: 'go']
    }

}
