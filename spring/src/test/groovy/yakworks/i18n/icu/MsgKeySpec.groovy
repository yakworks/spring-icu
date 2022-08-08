package yakworks.i18n.icu

import spock.lang.Specification
import yakworks.message.Msg
import yakworks.message.MsgKey

class MsgKeySpec extends Specification  {

    private ICUMessageSource messageSource

    void setup() {
        ICUMessageSource messageSource = new DefaultICUMessageSource()
        messageSource.useCodeAsDefaultMessage = true
        this.messageSource = messageSource
    }

    void 'maps for named arguments'() {
        when:
        MsgKey msgKey = Msg.key('named.arguments',[name: 'foo'])

        String msg = messageSource.get(msgKey)

        then:
        "Attachment foo saved" == msg
    }

    void 'without arguments'() {
        when:
        MsgKey msgKey = Msg.key('named.arguments')

        String msg = messageSource.get(msgKey)

        then:
        "Attachment {name} saved" == msg
    }

    void 'with bad code'() {
        when:
        MsgKey msgKey = Msg.key('nonexistent.message', null)

        String msg = messageSource.get(msgKey)

        then:
        'nonexistent.message' == msg
    }

    void 'with default'() {
        when:
        MsgKey msgKey = Msg.key('nonexistent').fallbackMessage("no such animal")

        String msg = messageSource.get(msgKey)

        then:
        'no such animal' == msg
    }

    void 'when default has named args'() {
        when:
        MsgKey msgKey = Msg.key('nonexistent', [name: 'taco']).fallbackMessage("have a {name} 🌮")

        String msg = messageSource.get(msgKey)

        then:
        "have a taco 🌮" == msg
    }

    void 'when args have an emoji'() {
        when:
        MsgKey msgKey = Msg.key('nonexistent', [name: 'taco 🌮']).fallbackMessage("have a {name}")

        String msg = messageSource.get(msgKey)

        then:
        "have a taco 🌮" == msg
    }

}
