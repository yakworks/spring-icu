package com.transferwise.icu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ICUMessageSourceTest {

    private ICUMessageSource messageSource;

    ICUMessageSourceTest() {
        ICUReloadableResourceBundleMessageSource messageSource = new ICUReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasename("messages");
        this.messageSource = messageSource;
    }

    private static Stream<Arguments> localesArgs() {
        return Stream.of(
                Arguments.of(Locale.ENGLISH, "Refresh inbox"),
                Arguments.of(Locale.FRENCH, "Actualiser la boîte de réception")
        );
    }

    @ParameterizedTest
    @MethodSource("localesArgs")
    void testLocales(Locale locale, String expected) {
        String msg = messageSource.getMessage("simple", new Object[]{}, locale);
        assertEquals(expected, msg);
    }

    @Test
    void testNamedArguments() {
        Map<String, Object> args = new HashMap<>();
        args.put("name", "confidential.pdf");
        String msg = messageSource.getMessage("named.arguments", args, Locale.ENGLISH);
        assertEquals("Attachment confidential.pdf saved", msg);
    }

    @Test
    void testUnnamedArguments() {
        String msg = messageSource.getMessage("unnamed.arguments", new Object[]{"confidential.pdf"}, Locale.ENGLISH);
        assertEquals("Attachment confidential.pdf saved", msg);
    }

    private static Stream<Arguments> pluralsArgs() {
        return Stream.of(
                Arguments.of(1, "Message"),
                Arguments.of(2, "Messages")
        );
    }

    @ParameterizedTest
    @MethodSource("pluralsArgs")
    void testPlurals(int count, String expected) {
        Map<String, Object> args = new HashMap<>();
        args.put("count", count);
        String msg = messageSource.getMessage("plurals.language.specific", args, Locale.ENGLISH);
        assertEquals(expected, msg);
    }

    private static Stream<Arguments> pluralsExactMatchesArgs() {
        return Stream.of(
                Arguments.of(0, "No messages"),
                Arguments.of(1, "1 message"),
                Arguments.of(2, "2 messages")
        );
    }

    @ParameterizedTest
    @MethodSource("pluralsExactMatchesArgs")
    void testPluralsExactMatches(int count, String expected) {
        Map<String, Object> args = new HashMap<>();
        args.put("count", count);
        String msg = messageSource.getMessage("plurals.exact.matches", args, Locale.ENGLISH);
        assertEquals(expected, msg);
    }

    private static Stream<Arguments> pluralsOffsettingFormArgs() {
        return Stream.of(
                Arguments.of(0, "Nobody read this message"),
                Arguments.of(1, "Only you read this message"),
                Arguments.of(2, "You and 1 friend read this message"),
                Arguments.of(3, "You and 2 friends read this message")
        );
    }

    @ParameterizedTest
    @MethodSource("pluralsOffsettingFormArgs")
    void testPluralsOffsettingForm(int count, String expected) {
        Map<String, Object> args = new HashMap<>();
        args.put("count", count);
        String msg = messageSource.getMessage("plurals.offsetting.form", args, Locale.ENGLISH);
        assertEquals(expected, msg);
    }

    private static Stream<Arguments> selectArgs() {
        return Stream.of(
                Arguments.of("male", "He replied to your message"),
                Arguments.of("female", "She replied to your message"),
                Arguments.of("other", "They replied to your message")
        );
    }

    @ParameterizedTest
    @MethodSource("selectArgs")
    void testSelect(String gender, String expected) {
        Map<String, Object> args = new HashMap<>();
        args.put("gender", gender);
        String msg = messageSource.getMessage("select", args, Locale.ENGLISH);
        assertEquals(expected, msg);
    }

    private static Stream<Arguments> ordinalsArgs() {
        return Stream.of(
                Arguments.of(1, "1st message"),
                Arguments.of(2, "2nd message"),
                Arguments.of(3, "3rd message"),
                Arguments.of(4, "4th message"),
                Arguments.of(5, "5th message")
        );
    }

    @ParameterizedTest
    @MethodSource("ordinalsArgs")
    void testOrdinals(int count, String expected) {
        Map<String, Object> args = new HashMap<>();
        args.put("count", count);
        String msg = messageSource.getMessage("ordinals", args, Locale.ENGLISH);
        assertEquals(expected, msg);
    }

    @Test
    void testNumbers() {
        Map<String, Object> args = new HashMap<>();
        args.put("size", 0.9);
        String msg = messageSource.getMessage("numbers", args, Locale.ENGLISH);
        assertEquals("You're using 90% of your quota", msg);
    }

    @Test
    void testDates() {
        Map<String, Object> args = new HashMap<>();

        java.util.Date date = Date.from(
                LocalDate.of(1970, 1, 1)
                        .atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        args.put("epoch", date);
        String msg = messageSource.getMessage("dates", args, Locale.ENGLISH);
        assertEquals("The unix epoch is Jan 1, 1970", msg);
    }

    @Test
    void testDefaultMessage() {
        String msg = messageSource.getMessage("nonexistent.message", new Object[]{"not used"}, "default", Locale.ENGLISH);
        assertEquals("default", msg);
    }

    @Test
    void testDefaultMessageWithNamedArguments() {
        Map<String, Object> args = new HashMap<>();
        args.put("unimportant", "not used");

        String msg = messageSource.getMessage("nonexistent.message", args, "default", Locale.ENGLISH);
        assertEquals("default", msg);
    }
}
