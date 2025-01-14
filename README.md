# Spring MessageSource ICU With Yaml Support

- Provides the [ICU4J](http://site.icu-project.org/) message formatting features, such as named arguments support, flexible plural formatting,
rule based number format, date interval formats.

- YAML resources for the messages sources. 

- Simple base `MsgService` interface that's independent of any framework, makes it easy to use
  in libraries and then share across spring, grails and micronaut etc...

- Allows placeHolder replacement. So a message can look like 
  ``` 
  company: Yakworks
  hello: Hey {name}, from ${company}
  ```
  then  `msgService.get('hello', [name: 'Bob']) == 'Hey Bob, from Yakworks'`

## Usage

While some of the helpful classes are using groovy, groovy is not required to the bulk 
of the core functionality

### SpringBoot

Gradle deps:
```
implementation 'org.yakworks:spring-icu4j:0.3.0'
```

Define the bean.

```Java
// override the default messageSource bean, simple example, might want to build further
@Bean
public ICUMessageSource messageSource() {
    DefaultICUMessageSource messageSource = new DefaultICUMessageSource();
    messageSource.setBasename("messages");
    return messageSource;
```

Place your messages.yaml (or messages.properties) files under `src/main/resources`. 
See this projects test cases for some examples.

### Grails 4.x +

`implementation 'org.yakworks:grails-icu4j:0.3.0'

messageSource bean will be setup and overrides the default. Should be fully backword compatible.

Works with messages.properties or messages.yaml in grails-app/i18n. Can also place your message files under `src/main/resources`. 

### Grails External Properties

can set 2 config properties

`yakworks.i18n.externalLocation="'file:///app/external-resources'`

to set how long to cache
`yakworks.i18n.cacheSeconds=60`

### Project layout and running tests

can use `make check` or `./gradlew check` to run the full monty

- spring dir is for the spring source
- grails dir depends on spring project and is for plugin
- i18n is for the yakworks.i18n common model files such as 'MsgKey' class that dont require spring or grails to compile


## Features

### Named arguments

By default Spring allows you to use only numbered arguments in i18n messages. ICU4j support named arguments using Map,
which are sometimes are more readable. For example:

```
numbered={0}, you have {1} unread messages of {2}
user.status={username}, you have {unread} unread messages of {total}
```

Exmaple with this lib:

```grooovy
Map args = [username: "John", unread: 12, total: 200)
println messageSource.getMessage("user.status", args)

->John, you have 12 unread messages of 200
```

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
System.out.printlin(messageSource.getMessage("amount", new Object[]{12045}, locale));
```

will output `twelve thousand forty-five dollars`.

### Other features

- ICU implements a more user-friendly apostrophe quoting syntax. In message text, an apostrophe only begins quoting
literal text if it immediately precedes a syntax character (mostly {curly braces}). By default an apostrophe always
begins quoting, which requires common text like "don't" and "aujourd'hui" to be written with doubled apostrophes like "don''t" and "aujourd''hui".
- Many more date formats: month+day, year+month,...
- Date interval formats: "Dec 15-17, 2009"


## Spring MessageSource vs ICUMessageSource

While `yakworks.i18n.icu.ICUMessageSource` defines methods that take a named argument map, 
`org.springframework.context.MessageSource` does not. 
Therefore, in order to support ICU in code that expects a `org.springframework.context.MessageSource` message source, 
such as Thymeleaf's [#messages](https://www.thymeleaf.org/doc/tutorials/2.1/usingthymeleaf.html#messages-1), 
we check if the first argument of the `Object[] args` parameter passed to `org.springframework.context.MessageSource#getMessage` 
is a Map and if so cast that argument to a `Map<String, Object>` and call `ICUMessageSource#getMessage`

## Yaml

- https://github.com/akkinoc/yaml-resource-bundle
- https://stackoverflow.com/questions/43655895/spring-boot-yml-resourcebundle-file
- https://gist.github.com/composite/eb3a3fd85d7ba9891f860e4a9fed06b9
