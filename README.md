# Spring MessageSource ICU Support

FORK info: This used to be a grails plugin, then a fork made it spring based and the yakworks
fork is aimed at making it gorm

Provides the [ICU4J](http://site.icu-project.org/) message formatting features, such as named arguments support, flexible plural formatting,
rule based number format, date interval formats.

## Features

### Named arguments
By default Spring allows you to use only numbered arguments in i18n messages. ICU4j support named arguments using Map,
which are sometimes are more readable. For example:

```
numbered={0}, you have {1} unread messages of {2}
names={username}, you have {unread} unread messages of {total}
```

With ICU4J, you can do this:

```Java
Map<String, Object> args = new HashMap<>();
args.put("username", "John");
args.put("unread", 12);
args.put("total", 200);

System.out.println(messageSource.getMessage("names", args, locale));
```

will output `John, you have 12 unread messages of 200`.

### Plural formatting

Pluralisation in English is pretty simple and can be implemented using embedded `ChoiceFormat`. However, many other
languages have more complex pluralisation rules [described here](http://unicode.org/repos/cldr-tmp/trunk/diff/supplemental/language_plural_rules.html),
which cannot be handled by default. The plugin provides a simple pluralization using a language's rules, e.g. for Polish:

```
plural={0} {0, plural, one {auto} few {auta} many {aut} other{aut}}
```

```Java
System.out.println(messageSource.getMessage("plural", new Object[]{3}, locale));
System.out.println(messageSource.getMessage("plural", new Object[]{7}, locale));
```

will output `3 auta`, `7 aut`.

### Rule based number formatting

```
amount={0, spellout} dollars
```

```Java
System.out.printlin(messageSource.getMessage("amount", new Objcet[]{12045}, locale));
```

will output `twelve thousand forty-five dollars`.

### Other features
- ICU implements a more user-friendly apostrophe quoting syntax. In message text, an apostrophe only begins quoting
literal text if it immediately precedes a syntax character (mostly {curly braces}). By default an apostrophe always
begins quoting, which requires common text like "don't" and "aujourd'hui" to be written with doubled apostrophes like "don''t" and "aujourd''hui".
- Many more date formats: month+day, year+month,...
- Date interval formats: "Dec 15-17, 2009"

# Spring Boot Usage
Define the bean.

```Java
@Bean
public MessageSource messageSource() {
    ICUReloadableResourceBundleMessageSource messageSource = new ICUReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:messages");
    return messageSource;
}
```

Place your message.properties files under `src/main/resources`. See this projects test cases for some examples.

# Explanation

While `com.transferwise.icu.ICUMessageSource` defines methods that take a named argument map, `org.springframework.context.MessageSource` does not. Therefore, in order to support ICU in code that expects a `org.springframework.context.MessageSource` message source, such as Thymeleaf's [#messages](https://www.thymeleaf.org/doc/tutorials/2.1/usingthymeleaf.html#messages-1), we check if the first argument of the `Object[] args` parameter passed to `org.springframework.context.MessageSource#getMessage` is a Map and if so cast that argument to a `Map<String, Object>` and call `com.transferwise.icu.ICUMessageSource#getMessage`

See com.transferwise.icu.ICUAbstractMessageSource.isNamedArgumentsMapPresent

# License
This library is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

(c) All rights reserved
