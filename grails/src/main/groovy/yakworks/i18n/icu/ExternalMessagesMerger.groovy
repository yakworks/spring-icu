/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n.icu

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.core.io.Resource

/**
 * Copied code in from BinaryGrailsPlugin
 */
@Slf4j
@CompileStatic
class ExternalMessagesMerger {
    public static final String PROPERTIES_EXTENSION = ".properties";
    Resource rootResource
    List<String> messagesPattern = ['*messages*.properties']

    ExternalMessagesMerger(Resource rootResource){
        this.rootResource = rootResource
    }

    long mergeExternalProperties(final Locale locale, Properties mergedProps) {
        final Map propsMap = getExternalMessages(locale)
        if (propsMap.props != null) {
            mergedProps.putAll(propsMap.props as Properties)
        }
        return propsMap.lastTimeStamp as Long
    }

    Map getExternalMessages(final Locale locale) {
        Properties properties = null
        long lastTimeStamp
        if(rootResource != null) {
            try {
                Resource[] resources = MessagesFilter.findResources(rootResource, messagesPattern, PROPERTIES_EXTENSION, locale)
                if(resources.length > 0) {
                    properties = new Properties()
                    MessagesFilter.loadFromResources(properties, resources)
                    lastTimeStamp = (resources*.lastModified()).max() as Long
                }

            } catch (IOException e) {
                log.error("IOException loading i18n yaml messages", e)
            }
        }
        return [props: properties, lastTimeStamp: lastTimeStamp]
    }

}
