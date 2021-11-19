package yakworks.i18n

import spock.lang.Specification

class MsgKeySpec extends Specification  {

    void 'check builder'() {
        when: "of static is called on ICUMsgKey"
        MsgKey msgKey = MsgKey.of('named.arguments')

        then: 'should have set it up'
        msgKey instanceof DefaultMsgKey
        msgKey.code == 'named.arguments'
        //will always be intialized
        msgKey.args != null
        //but it should be empty
        msgKey.args.isEmpty()

        when: 'fallbackMessage is set'
        msgKey.fallbackMessage("go go go")

        then: 'args should have been setup'
        msgKey.fallbackMessage == 'go go go'
    }

    void 'check builder 2'() {
        when:
        MsgKey msgKey = MsgKey.of('named.arguments').args([name: 'foo'])

        then: 'should have set it up'
        msgKey.code == 'named.arguments'
        msgKey.args.get() == [name:'foo']

        when: 'def msg is set'
        msgKey.fallbackMessage("go")

        then: 'args should have been setup'
        msgKey.fallbackMessage == 'go'
    }

    void 'check fallback in map'() {
        when: "fallbackMessage is put in args"
        MsgKey msgKey = MsgKey.of('named.arguments').args([name: 'foo', fallbackMessage: 'go'])

        then: 'should have set it up'
        msgKey.code == 'named.arguments'
        msgKey.args.get() == [name:'foo', fallbackMessage: 'go']
        msgKey.fallbackMessage == 'go'

    }

    void 'get argsMap and add values'() {
        when: "no args is setup"
        MsgKey msgKey = MsgKey.of('some.key')
        assert msgKey.args.isEmpty()
        def args = msgKey.args.asMap()
        args.foo = 'bar'

        then:
        msgKey.args.get() == [foo: 'bar']

    }

    void 'args map if already setup'() {
        when:
        MsgKey msgKey = MsgKey.of('some.key', [foo: 'bar'])

        then:
        msgKey.args.asMap() == [foo: 'bar']
    }

    void 'args map if already setup as list'() {
        when:
        MsgKey msgKey = MsgKey.of('some.key', ['foo', 'bar'])

        then:
        msgKey.args.asMap() == null
    }

    void 'add arg'() {
        when:
        MsgKey msgKey = MsgKey.of('some.key', [foo: 'bar'])
        msgKey.args.put('buzz', 'bazz')

        then:
        msgKey.args.asMap() == [foo: 'bar', 'buzz': 'bazz']
    }

}
