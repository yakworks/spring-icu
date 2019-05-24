package com.devtrigger.grails.icu;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ICUMessageSourceTest {

    private ICUMessageSource messageSource;

    ICUMessageSourceTest() {
        ICUReloadableResourceBundleMessageSource messageSource = new ICUReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasename("messages");
        this.messageSource = messageSource;
    }

    @Test
    void testSimple() {
        String msg = messageSource.getMessage("simple", new Object[]{}, Locale.ENGLISH);
        assertEquals("Refresh inbox", msg);
    }

    @Test
    void testSimpleLocale() {
        String msg = messageSource.getMessage("simple", new Object[]{}, Locale.FRENCH);
        assertEquals("Actualiser la boîte de réception", msg);
    }

    @Test
    void testVariables() {
        Map<String, Object> args = new HashMap<>();
        args.put("name", "confidential.pdf");
        String msg = messageSource.getMessage("variables", args, Locale.ENGLISH);
        assertEquals("Attachment confidential.pdf saved", msg);
    }

    @Test
    void testPlurals1() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 1);
        String msg = messageSource.getMessage("plurals.language.specific", args, Locale.ENGLISH);
        assertEquals("Message", msg);
    }

    @Test
    void testPlurals2() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 2);
        String msg = messageSource.getMessage("plurals.language.specific", args, Locale.ENGLISH);
        assertEquals("Messages", msg);
    }

    @Test
    void testPluralsExactMatches0() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 0);
        String msg = messageSource.getMessage("plurals.exact.matches", args, Locale.ENGLISH);
        assertEquals("No messages", msg);
    }

    @Test
    void testPluralsExactMatches1() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 1);
        String msg = messageSource.getMessage("plurals.exact.matches", args, Locale.ENGLISH);
        assertEquals("1 message", msg);
    }

    @Test
    void testPluralsExactMatches2() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 2);
        String msg = messageSource.getMessage("plurals.exact.matches", args, Locale.ENGLISH);
        assertEquals("2 messages", msg);
    }

    @Test
    void testPluralsOffsettingForm0() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 0);
        String msg = messageSource.getMessage("plurals.offsetting.form", args, Locale.ENGLISH);
        assertEquals("Nobody read this message", msg);
    }

    @Test
    void testPluralsOffsettingForm1() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 1);
        String msg = messageSource.getMessage("plurals.offsetting.form", args, Locale.ENGLISH);
        assertEquals("Only you read this message", msg);
    }

    @Test
    void testPluralsOffsettingForm2() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 2);
        String msg = messageSource.getMessage("plurals.offsetting.form", args, Locale.ENGLISH);
        assertEquals("You and 1 friend read this message", msg);
    }

    @Test
    void testPluralsOffsettingForm3() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 3);
        String msg = messageSource.getMessage("plurals.offsetting.form", args, Locale.ENGLISH);
        assertEquals("You and 2 friends read this message", msg);
    }

    @Test
    void testSelectMale() {
        Map<String, Object> args = new HashMap<>();
        args.put("gender", "male");
        String msg = messageSource.getMessage("select", args, Locale.ENGLISH);
        assertEquals("He replied to your message", msg);
    }

    @Test
    void testSelectFemale() {
        Map<String, Object> args = new HashMap<>();
        args.put("gender", "female");
        String msg = messageSource.getMessage("select", args, Locale.ENGLISH);
        assertEquals("She replied to your message", msg);
    }

    @Test
    void testSelectOther() {
        Map<String, Object> args = new HashMap<>();
        args.put("gender", "other");
        String msg = messageSource.getMessage("select", args, Locale.ENGLISH);
        assertEquals("They replied to your message", msg);
    }

    @Test
    void testOrdinals1() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 1);
        String msg = messageSource.getMessage("ordinals", args, Locale.ENGLISH);
        assertEquals("1st message", msg);
    }

    @Test
    void testOrdinals2() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 2);
        String msg = messageSource.getMessage("ordinals", args, Locale.ENGLISH);
        assertEquals("2nd message", msg);
    }

    @Test
    void testOrdinals3() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 3);
        String msg = messageSource.getMessage("ordinals", args, Locale.ENGLISH);
        assertEquals("3rd message", msg);
    }

    @Test
    void testOrdinals4() {
        Map<String, Object> args = new HashMap<>();
        args.put("count", 4);
        String msg = messageSource.getMessage("ordinals", args, Locale.ENGLISH);
        assertEquals("4th message", msg);
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
}