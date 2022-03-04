package yakworks.i18n.icu

import org.grails.testing.GrailsUnitTest
import org.springframework.context.MessageSource

import grails.testing.spring.AutowiredTest
import spock.lang.Specification

class MessageSourceSpec extends Specification implements GrailsUnitTest, AutowiredTest {

    ICUMessageSource messageSource

    void setupSpec() {
        defineBeans{
            messageSource(GrailsICUMessageSource)
        }
    }

    void "sanity check"(){
        expect:
        messageSource instanceof ICUMessageSource
    }

    void "messageSource lookup"(){
        when:

        def msg = messageSource.getMessage("default.not.found.message", ['Foo', 2] as Object[], Locale.default)

        then:
        msg == "Foo not found with id 2"

    }
    //
    // void "basic look up"(){
    //     expect:
    //     // r.code == "default.not.found.message"
    //     // r.args == ['MockDomain', 2]
    //     "Foo not found for id:2" == messageSource.getMessage("default.not.found.message", ['Foo', 2])
    // }
}
