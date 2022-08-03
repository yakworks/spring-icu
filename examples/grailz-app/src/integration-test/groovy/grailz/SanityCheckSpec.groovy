package grailz

import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.message.MsgContext
import yakworks.i18n.icu.ICUMessageSource

// Use as a simple to test when trying to see why application context has problem on init
@Integration
class SanityCheckSpec extends Specification {

    @Autowired
    ICUMessageSource messageSource

    void "should pick up messages.yaml override"() {
        expect:
        expected == messageSource.get(key, MsgContext.of(locale))

        where:
        key             | locale         | expected
        'simple'        | Locale.ENGLISH | "Replace" //normal properties override
        'simple'        | Locale.FRENCH  | "Actualiser Replace"
        'go'            | Locale.ENGLISH | "go new" //overrides
        'go'            | Locale.FRENCH  | "aller new"
        'testing.emoji' | Locale.ENGLISH | "I am 🔥" //exists in plugin yml
        'testing.emoji' | Locale.FRENCH  | "je suis 🔥"
        'testing.go'    | Locale.ENGLISH | "got it" //exists only in messages.yml
        'testing.go'    | Locale.FRENCH  | "got it" //exists only in messages.yml, not fr
    }

    void 'does it pick up ValidationMessages files'() {
        expect:
        'must be less than or equal to 1' == messageSource.get('jakarta.validation.constraints.Max.message', [value:1])
        'Got it' == messageSource.get('some.validation.message')
    }

    void 'should pick up external'() {
        expect:
        'success' == messageSource.get('external.label')
    }
}
