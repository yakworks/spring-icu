package yakworks.i18n.icu

import spock.lang.Specification
import yakworks.i18n.DefaultMsgKey
import yakworks.i18n.MsgKey

class MsgKeySpec extends Specification  {

    private ICUMessageSource messageSource

    void setup() {
        ICUMessageSource messageSource = new DefaultICUMessageSource()
        messageSource.useCodeAsDefaultMessage = true
        this.messageSource = messageSource
    }

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

    void 'maps for named arguments'() {
        when:
        MsgKey msgKey = MsgKey.of('named.arguments', [name: 'foo'])

        String msg = messageSource.getMessage(msgKey)

        then:
        "Attachment foo saved" == msg
    }

    void 'without arguments'() {
        when:
        MsgKey msgKey = MsgKey.of('named.arguments')

        String msg = messageSource.getMessage(msgKey)

        then:
        "Attachment {name} saved" == msg
    }

    void 'with bad code'() {
        when:
        MsgKey msgKey = MsgKey.of('nonexistent.message')

        String msg = messageSource.getMessage(msgKey)

        then:
        'nonexistent.message' == msg
    }

    void 'with default'() {
        when:
        MsgKey msgKey = MsgKey.of('nonexistent').defaultMessage("no such animal")

        String msg = messageSource.getMessage(msgKey)

        then:
        'no such animal' == msg
    }

    void 'when default has named args'() {
        when:
        MsgKey msgKey = MsgKey.of('nonexistent', [name: 'taco']).defaultMessage("have a {name} 🌮")

        String msg = messageSource.getMessage(msgKey)

        then:
        "have a taco 🌮" == msg
    }

    void 'when args have an emoji'() {
        when:
        MsgKey msgKey = MsgKey.of('nonexistent', [name: 'taco 🌮']).defaultMessage("have a {name}")

        String msg = messageSource.getMessage(msgKey)

        then:
        "have a taco 🌮" == msg
    }

}
