package com.transferwise.icu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.support.StaticApplicationContext;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringTemplateEngineTest {

    private SpringTemplateEngine templateEngine;

    SpringTemplateEngineTest() {
        ICUReloadableResourceBundleMessageSource messageSource = new ICUReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasename("messages");

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

    private static Stream<Arguments> pluralsOffsettingFormArgs() {
        return Stream.of(
                Arguments.of(0, "<p>Nobody read this message</p>"),
                Arguments.of(1, "<p>Only you read this message</p>"),
                Arguments.of(2, "<p>You and 1 friend read this message</p>"),
                Arguments.of(3, "<p>You and 2 friends read this message</p>")
        );
    }

    @ParameterizedTest
    @MethodSource("pluralsOffsettingFormArgs")
    void testPluralsOffsettingForm(int count, String expected) {
        Map<String, Object> messageArgs = new HashMap<>();
        messageArgs.put("count", count);

        Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("messageArgs", messageArgs);

        String actual = templateEngine.process(
                "pluralsOffsettingForm",
                new Context(Locale.ENGLISH, contextVariables)
        ).trim();

        assertEquals(expected, actual);
    }

    @Test
    void testNamedArguments() {
        Map<String, Object> args = new HashMap<>();
        args.put("irrelevant", "IRRELEVANT");
        args.put("name", "NAME");
        args.put("also.irrelevant", "ALSO_IRRELEVANT");

        Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("messageArgs", args);

        String actual = templateEngine.process(
            "namedArguments",
            new Context(Locale.ENGLISH, contextVariables)
        ).trim();

        assertEquals("<p>Attachment NAME saved</p>", actual);
    }
}
