package yakworks.i18n.icu

import java.time.LocalDate
import java.time.ZoneId

import org.springframework.beans.factory.annotation.Autowired

import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.i18n.DefaultMsgKey
import yakworks.i18n.MsgKey

// Use as a simple to test when trying to see why application context has problem on init
@Integration
class SanityCheckSpec extends Specification {

    @Autowired
    ICUMessageSource messageSource

    void 'maps for named arguments'() {
        when:
        MsgKey msgKey = MsgKey.of('named.arguments', [name: 'foo'])

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

    @Ignore //FIXME need to get this working
    void 'with bad code'() {
        when:
        MsgKey msgKey = MsgKey.ofCode('nonexistent.message')

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
