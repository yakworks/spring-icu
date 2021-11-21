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

    void 'maps for named arguments'() {
        when:
        MsgKey msgKey = MsgKey.of('named.arguments',[name: 'foo'])

        String msg = messageSource.getMessage(msgKey)

        then:
        "Attachment foo saved" == msg
    }

    void 'without arguments'() {
        when:
        MsgKey msgKey = MsgKey.ofCode('named.arguments')

        String msg = messageSource.getMessage(msgKey)

        then:
        "Attachment {name} saved" == msg
    }

    void 'with bad code'() {
        when:
        MsgKey msgKey = MsgKey.of('nonexistent.message', null)

        String msg = messageSource.getMessage(msgKey)

        then:
        'nonexistent.message' == msg
    }

    void 'with default'() {
        when:
        MsgKey msgKey = MsgKey.ofCode('nonexistent').fallbackMessage("no such animal")

        String msg = messageSource.getMessage(msgKey)

        then:
        'no such animal' == msg
    }

    void 'when default has named args'() {
        when:
        MsgKey msgKey = MsgKey.of('nonexistent', [name: 'taco']).fallbackMessage("have a {name} 🌮")

        String msg = messageSource.getMessage(msgKey)

        then:
        "have a taco 🌮" == msg
    }

    void 'when args have an emoji'() {
        when:
        MsgKey msgKey = MsgKey.of('nonexistent', [name: 'taco 🌮']).fallbackMessage("have a {name}")

        String msg = messageSource.getMessage(msgKey)

        then:
        "have a taco 🌮" == msg
    }

}
