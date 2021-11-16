package yakworks.i18n.icu


import org.springframework.context.support.StaticApplicationContext;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode

import spock.lang.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringTemplateEngineSpec extends Specification  {

    private SpringTemplateEngine templateEngine;

    void setup() {
        ICUMessageSource messageSource = new DefaultICUMessageSource()

        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(new StaticApplicationContext());
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);

        templateEngine = new SpringTemplateEngine();
        templateEngine.setMessageSource(messageSource);
        templateEngine.setEnableSpringELCompiler(true);
        templateEngine.setTemplateResolver(templateResolver);
    }

    void "should pick up plurals"() {
        expect:
        Map msgArgs = [count: count] as Map<String, Object>
        Map params =[ messageArgs: [count: count]] as Map<String, Object>

        String actual = templateEngine.process(
            "pluralsOffsettingForm",
            new Context(Locale.ENGLISH, params)
        ).trim()

        expected == actual

        where:
        count | expected
        0     | "<p>Nobody read this message</p>"
        1     | "<p>Only you read this message</p>"
        2     | "<p>You and 1 friend read this message</p>"
        3     | "<p>You and 2 friends read this message</p>"

    }

    void testNamedArguments() {
        expect:
        Map args = [
            irrelevant: 'IRRELEVANT', name: "NAME", 'also.irrelevant': "ALSO_IRRELEVANT"
        ]

        Map<String, Object> contextVariables = ["messageArgs": args]

        String actual = templateEngine.process(
            "namedArguments",
            new Context(Locale.ENGLISH, contextVariables)
        ).trim();

        assertEquals("<p>Attachment NAME saved</p>", actual);
    }
}
