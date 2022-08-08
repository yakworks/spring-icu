package yakworks.i18n.icu

import spock.lang.Specification
import yakworks.message.Msg
import yakworks.message.MsgKey

class PlaceholderMessageSpec extends Specification  {

    private ICUMessageSource messageSource

    void setup() {
        DefaultICUMessageSource messageSource = new DefaultICUMessageSource()
        messageSource.basename = "messages"
        //should only be on for testing
        messageSource.useCodeAsDefaultMessage = true
        this.messageSource = messageSource
    }

    void "test if placeHolders works"() {
        expect:
        "I am 🔥, Hi {name}" == messageSource.get("testing.namedWithRef")
        "I am 🔥, Hi Bobz" == messageSource.get(Msg.key("testing.namedWithRef", [name: 'Bobz']) )

    }

}
