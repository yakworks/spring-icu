package yakworks.i18n.icu


import spock.lang.Specification;

import java.time.LocalDate
import java.time.ZoneId
import java.util.stream.Stream
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments

class KitchenSinkSpec extends Specification  {

    private ICUMessageSource messageSource

    void setup() {
        ICUMessageSource messageSource = new DefaultICUMessageSource()
        messageSource.defaultEncoding = "UTF-8"
        messageSource.basename = "messages"
        messageSource.useCodeAsDefaultMessage = true
        this.messageSource = messageSource
    }

    void "locales should get transaled"() {
        expect:
        expected == messageSource.getMessage("simple", [] as Object[], locale)

        where:
        locale         | expected
        Locale.ENGLISH | "Refresh inbox"
        Locale.FRENCH  | "Actualiser la boîte de réception"
    }

    void "emoji"() {
        expect:
        "I am 🚀" == messageSource.getMessage("emoji", null, null)

    }

    void 'maps for named arguments'() {
        when:
        def args =[name: "confidential.pdf"]
        String msg = messageSource.getMessage("named.arguments", args, Locale.ENGLISH)

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
        expected == messageSource.getMessage("plurals.language.specific", [count: count], Locale.ENGLISH)

        where:
        count | expected
        1     | "Message"
        2     | "Messages"
    }

    void "should pick up plurals exacts"() {
        expect:
        expected == messageSource.getMessage("plurals.exact.matches", [count: count], Locale.ENGLISH)

        where:
        count | expected
        0     | "No messages"
        1     | "1 message"
        2     | "2 messages"
    }

    void "plural offseting"() {
        expect:
        expected == messageSource.getMessage("plurals.offsetting.form", [count: count], Locale.ENGLISH)

        where:
        count | expected
        0     | "Nobody read this message"
        1     | "Only you read this message"
        2     | "You and 1 friend read this message"
        3     | "You and 2 friends read this message"
    }

    void "gender select"() {
        expect:
        expected == messageSource.getMessage("select", [gender: gender], Locale.ENGLISH)

        where:
        gender | expected
        'male'       | "He replied to your message"
        'female'     | "She replied to your message"
        'other'      | "They replied to your message"
    }

    void "ordinal args"() {
        expect:
        expected == messageSource.getMessage("ordinals", [count: count], Locale.ENGLISH)

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
        String msg = messageSource.getMessage("numbers", [size: 0.9], Locale.ENGLISH);
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
        "The unix epoch is Jan 1, 1970" == messageSource.getMessage("dates", args, Locale.ENGLISH);
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

    void testDefaultMessageWithNamedArguments() {
        when:
        Map args = ["unimportant": "not used"]

        then:
        "default" == messageSource.getMessage("nonexistent.message", args, "default", Locale.ENGLISH);
    }

    void 'null local only'() {
        when:
        Map args = ["unimportant": "not used"]

        then:
        "default" == messageSource.getMessage("nonexistent.message", args, "default", null);
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
