/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.icu

@SuppressWarnings('Indentation')
class Icu4jGrailsPlugin extends grails.plugins.Plugin {

    def loadAfter = ['i18n']

    Closure doWithSpring() { {->
        messageSource(ICUReloadableResourceBundleMessageSource) {
            basename = "classpath:messages"
        }
    }}
}
