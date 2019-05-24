package com.transferwise.icu;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringIcuTemplateEngineTest {

    private SpringIcuTemplateEngine templateEngine;

    SpringIcuTemplateEngineTest() {
        ICUReloadableResourceBundleMessageSource messageSource = new ICUReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasename("messages");

        ApplicationContext applicationContext = new StaticApplicationContext();

        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);

        SpringIcuMessageResolver messageResolver = new SpringIcuMessageResolver();
        messageResolver.setIcuMessageSource(messageSource);

        templateEngine = new SpringIcuTemplateEngine();
        templateEngine.setEnableSpringELCompiler(true);
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.setMessageResolver(messageResolver);
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
        );

        assertEquals(expected, actual);
    }
}