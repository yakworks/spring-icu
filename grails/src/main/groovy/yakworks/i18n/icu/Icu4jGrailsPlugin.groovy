/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n.icu

@SuppressWarnings('Indentation')
class Icu4jGrailsPlugin extends grails.plugins.Plugin {

    def loadAfter = ['i18n']

    Closure doWithSpring() { {->

        // GrailsApplication application = grailsApplication

        messageSource(GrailsICUMessageSource, grailsApplication, pluginManager) {
            ymlLocations = ["*messages*.yml"]
            //What to search for in main app to get names
            messageBundleLocations = "classpath*:messages*.properties"
            // useCodeAsDefaultMessage = true
        }

    }}
}
