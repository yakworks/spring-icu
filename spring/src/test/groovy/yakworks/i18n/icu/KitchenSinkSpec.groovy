package yakworks.i18n.icu


import spock.lang.Specification;

import java.time.LocalDate
import java.time.ZoneId
import java.util.stream.Stream
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments

import yakworks.i18n.MsgContext
import yakworks.i18n.MsgKey
import yakworks.i18n.MsgService

class KitchenSinkSpec extends Specification  {

    private ICUMessageSource messageSource

    void setup() {
        DefaultICUMessageSource messageSource = new DefaultICUMessageSource()
        messageSource.defaultEncoding = "UTF-8"
        messageSource.basename = "messages"
        messageSource.useCodeAsDefaultMessage = true
        this.messageSource = messageSource
    }

    void "locales should get transaled with springs messageSource.getMessage and MsgService using context"() {
        expect:
        expected == messageSource.getMessage("simple", [] as Object[], locale)
        expected == messageSource.getMessage("simple", MsgContext.empty().locale(locale))

        where:
        locale         | expected
        Locale.ENGLISH | "Refresh inbox"
        Locale.FRENCH  | "Actualiser la boîte de réception"
    }

    void "emoji"() {
        expect:
        "I am 🚀" == messageSource.getMessage("emoji")

    }

    void 'maps for named arguments'() {
        when:
        def args =[name: "confidential.pdf"]
        String msg = messageSource.getMessage(MsgKey.of("named.arguments", args))

        then:
        "Attachment confidential.pdf saved" == msg
    }

    void 'normal array based arguments'() {
        expect:
        String msg = messageSource.getMessage("unnamed.arguments", ["confidential.pdf"] as Object[], Locale.ENGLISH);
        "Attachment confidential.pdf saved" == msg
    }

    private static Stream<Arguments> pluralsArgs() {
        return Stream.of(
                Arguments.of(1, "Message"),
                Arguments.of(2, "Messages")
        );
    }

    void "should pick up plurals"() {
        expect:
        expected == messageSource.getMessage(MsgKey.of("plurals.language.specific", [count: count]))

        where:
        count | expected
        1     | "Message"
        2     | "Messages"
    }

    void "should pick up plurals exacts"() {
        expect:
        expected == messageSource.getMessage(MsgKey.of("plurals.exact.matches", [count: count]))

        where:
        count | expected
        0     | "No messages"
        1     | "1 message"
        2     | "2 messages"
    }

    void "plural offseting"() {
        expect:
        expected == messageSource.getMessage(MsgKey.of("plurals.offsetting.form", [count: count]))

        where:
        count | expected
        0     | "Nobody read this message"
        1     | "Only you read this message"
        2     | "You and 1 friend read this message"
        3     | "You and 2 friends read this message"
    }

    void "gender select"() {
        expect:
        expected == messageSource.getMessage(MsgKey.of("select", [gender: gender]))

        where:
        gender | expected
        'male'       | "He replied to your message"
        'female'     | "She replied to your message"
        'other'      | "They replied to your message"
    }

    void "ordinal args"() {
        expect:
        expected == messageSource.getMessage(MsgKey.of("ordinals", [count: count]))

        where:
        count | expected
        1     | "1st message"
        2     | "2nd message"
        3     | "3rd message"
        4     | "4th message"
        5     | "5th message"
    }

    void testNumbers() {
        expect:
        String msg = messageSource.getMessage(MsgKey.of("numbers", [size: 0.9]));
        "You're using 90% of your quota" == msg
    }

    @Test
    void testDates() {
        when:
        Map<String, Object> args = new HashMap<>();

        java.util.Date date = Date.from(
                LocalDate.of(1970, 1, 1)
                        .atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        args.put("epoch", date);

        then:
        "The unix epoch is Jan 1, 1970" == messageSource.getMessage(MsgKey.of("dates", args));
    }

    void testDefaultMessage() {
        expect:
        "default" == messageSource.getMessage("nonexistent.message", ["not used"] as Object[], "default", Locale.ENGLISH);
    }

    void "code shoudl be returned if nothing found"() {
        expect:
        "nonexistent.message" == messageSource.getMessage("nonexistent.message", [foo: 'bar'], null);
        "nonexistent.message" == messageSource.getMessage("nonexistent.message", null, null);
    }

    void "fallback in message context"() {
        expect:
        "got me" == messageSource.getMessage("nonexistent.message", MsgContext.empty().fallbackMessage('got me'));
    }

    void 'fallback in argument map'() {
        when:
        Map args = ["unimportant": "not used", fallbackMessage: 'got me']

        then:
        "got me" == messageSource.getMessage(MsgKey.of("nonexistent.message", args));
    }

    void 'null local only'() {
        when:
        Map args = ["unimportant": "not used"]

        then:
        "default" == messageSource.getMessage("nonexistent.message", [] as Object[], "default", null);
    }

    void "null args and null 3rd arg(locale or defMsg) should work"() {
        expect:
        Object[] args = null;
        "Refresh inbox" == messageSource.getMessage("simple", null, null);
    }

    void testNullArguments() {
        expect:
        Object[] args = null;
        "Refresh inbox" == messageSource.getMessage("simple", args as Object[], "default", Locale.ENGLISH);
    }

}
