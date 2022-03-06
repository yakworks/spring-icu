package yakworks.i18n.icu

import spock.lang.Specification
import yakworks.i18n.MsgMultiKey;

import java.time.LocalDate
import java.time.ZoneId

import yakworks.i18n.MsgContext
import yakworks.i18n.MsgKey

class KitchenSinkSpec extends Specification  {

    private ICUMessageSource msgService

    void setup() {
        DefaultICUMessageSource messageSource = new DefaultICUMessageSource()
        // messageSource.defaultEncoding = "UTF-8"
        messageSource.basename = "messages"
        //should only be on for testing
        messageSource.useCodeAsDefaultMessage = true
        this.msgService = messageSource
    }

    void "locales should get transaled with springs messageSource.getMessage and MsgService using context"() {
        expect:
        expected == msgService.getMessage("simple", [] as Object[], locale)
        expected == msgService.get("simple", MsgContext.of(locale))

        where:
        locale         | expected
        Locale.ENGLISH | "Simple Message"
        Locale.FRENCH  | "Actualiser la boîte de réception"
    }

    void "should pick up messages.yaml"() {
        expect:
        //old way
        expected == msgService.getMessage(key, [] as Object[], locale)
        //new way
        expected == msgService.get(key, MsgContext.of(locale))

        where:
        key             | locale         | expected
        'go'            | Locale.ENGLISH | "Go Go Go" //exists in both
        'go'            | Locale.FRENCH  | "aller aller aller"
        'testing.emoji' | Locale.ENGLISH | "I am 🔥" //exists in yml
        'testing.emoji' | Locale.FRENCH  | "je suis 🔥"
        'testing.go'    | Locale.ENGLISH | "got it" //exists only in messages.yml
        'testing.go'    | Locale.FRENCH  | "got it" //exists only in messages.yml, not fr
    }

    void "using default locale"() {
        expect:
        expected == msgService.get(key)

        where:
        key             | expected
        'simple'        | "Simple Message" //exists in props
        'testing.emoji' | "I am 🔥" //exists in yml
    }

    void "yml with args"() {
        expect:
        expected == msgService.get(key, MsgContext.of(locale))

        where:
        key             | locale         | expected
        'testing.emoji' | Locale.ENGLISH | "I am 🔥" //exists in yml
        'testing.emoji' | Locale.FRENCH  | "je suis 🔥"
        'testing.go'    | Locale.ENGLISH | "got it" //exists only in messages.yml
        'testing.go'    | Locale.FRENCH  | "got it" //exists only in messages.yml, not fr
    }

    void "emoji"() {
        expect:
        "I am 🚀" == msgService.get("emoji")

    }

    void 'maps for named arguments'() {
        when:
        def args =[name: "confidential.pdf"]
        String msg = msgService.get("named.arguments", args)

        then:
        "Attachment confidential.pdf saved" == msg
    }

    void 'standard spring array based arguments'() {
        expect:
        String msg = msgService.getMessage("unnamed.arguments", ["confidential.pdf"] as Object[], Locale.ENGLISH);
        "Attachment confidential.pdf saved" == msg
    }


    void "should pick up plurals"() {
        expect:
        expected == msgService.get("plurals.language.specific", [count: count])

        where:
        count | expected
        1     | "Message"
        2     | "Messages"
    }

    void "should pick up plurals exacts"() {
        expect:
        expected == msgService.get(MsgKey.of("plurals.exact.matches", [count: count]))

        where:
        count | expected
        0     | "No messages"
        1     | "1 message"
        2     | "2 messages"
    }

    void "plural offseting"() {
        expect:
        expected == msgService.get(MsgKey.of("plurals.offsetting.form", [count: count]))

        where:
        count | expected
        0     | "Nobody read this message"
        1     | "Only you read this message"
        2     | "You and 1 friend read this message"
        3     | "You and 2 friends read this message"
    }

    void "gender select"() {
        expect:
        expected == msgService.get(MsgKey.of("select", [gender: gender]))

        where:
        gender | expected
        'male'       | "He replied to your message"
        'female'     | "She replied to your message"
        'other'      | "They replied to your message"
    }

    void "ordinal args"() {
        expect:
        expected == msgService.get(MsgKey.of("ordinals", [count: count]))

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
        String msg = msgService.get(MsgKey.of("numbers", [size: 0.9]));
        "You're using 90% of your quota" == msg
    }

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
        "The unix epoch is Jan 1, 1970" == msgService.get(MsgKey.of("dates", args));
    }

    void testDefaultMessage() {
        expect:
        "default" == msgService.getMessage("nonexistent.message", ["not used"] as Object[], "default", Locale.ENGLISH);
    }

    void "code shoudl be returned if nothing found"() {
        expect:
        "nonexistent.message" == msgService.get("nonexistent.message", [foo: 'bar'], null);
        "nonexistent.message" == msgService.get("nonexistent.message", null, null);
    }

    void "fallback in message context"() {
        expect:
        "got me" == msgService.get("nonexistent.message", MsgContext.withFallback('got me'));
    }

    void 'fallback in argument map'() {
        when:
        Map args = ["unimportant": "not used", fallbackMessage: 'got me']

        then:
        "got me" == msgService.get(MsgKey.of("nonexistent.message", args));
    }

    void 'null local only'() {
        when:
        Map args = ["unimportant": "not used"]

        then:
        "default" == msgService.getMessage("nonexistent.message", [] as Object[], "default", null);
    }

    void "null args and null 3rd arg(locale or defMsg) should work"() {
        expect:
        Object[] args = null;
        "Simple Message" == msgService.get("simple", null, null);
    }

    void "multiKey test args"() {
        expect:
        //first one is not there, second gets picked up
        def mmk = MsgMultiKey.ofCodes(["nonexistent.message", 'simple'])
        "Simple Message" == msgService.get(mmk)

        //first one is not there, second gets picked up
        def mmk2 = MsgMultiKey.ofCodes(["emoji", 'simple'])
        "I am 🚀" == msgService.get(mmk2)
    }

    void "multiKey args"() {
        expect:
        //first one is not there, second gets picked up
        def mmk = MsgMultiKey.of(MsgKey.of('', [name: 'Bob'])).codes(["nonexistent.message", 'testing.named'])
        "Hi Bob" == msgService.get(mmk)

        //first one is not there, second gets picked up
        def mmk2 = MsgMultiKey.of(MsgKey.of('', [name: 'Bob'])).codes(["testing.named2", 'testing.named'])
        "Hi Bob 2" == msgService.get(mmk2)
    }

}
