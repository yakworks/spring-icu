/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n.icu

import java.nio.charset.Charset

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.core.io.StaticResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.util.AntPathMatcher
import org.springframework.util.StringUtils

/**
 * Helper to load message properties and message yamls from plugins
 */
@Slf4j
@CompileStatic
class MessagesFilter {

    static Resource[] findResources(Resource baseResource , List<String> locationPatterns, String suffix, final Locale locale) {
        Resource[] resources
        StaticResourceLoader resourceLoader = new StaticResourceLoader()
        resourceLoader.baseResource= baseResource
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader)
        (resolver.pathMatcher as AntPathMatcher).setCaseSensitive(false)
        try {
            // first load all ymls
            List<Resource> resourceList = []
            locationPatterns.each{
                resourceList.addAll(resolver.getResources(it))
            }

            resources = resourceList as Resource[]
            //filter them down
            resources = resources.length > 0 ? filterResources(resources, suffix, locale) : resources

        } catch (IOException e) {
            log.error("IOException loading i18n yaml messages", e)
        }

        return resources

    }

    static Resource[] filterResources(Resource[] resources, String suffix, Locale locale) {

        List<Resource> finalResources = []

        for (Resource resource : resources) {
            String fn = resource.getFilename()

            if(fn.indexOf('_') > -1) {
                if(fn.endsWith('_' + locale.toString() + suffix)) {
                    finalResources.add(resource)
                }
                else if(fn.endsWith('_' + locale.getLanguage() + '_' + locale.getCountry() + suffix)) {
                    finalResources.add(resource)
                }
                else if(fn.endsWith('_' + locale.getLanguage() + suffix)) {
                    finalResources.add(resource)
                }
            }
            else {
                finalResources.add(resource)
            }
        }
        return sortResources(finalResources as Resource[])
    }

    /**
     * message bundles are locale specific. The more underscores the locale has the more specific the locale
     * so we order by the number of underscores present so that the most specific appears
     */
    static Resource[] sortResources(Resource[] resources){
        // taken from BinaryGrailsPlugin
        return resources.sort{ o1, o2 ->
            String f1 = o1.getFilename()
            String f2 = o2.getFilename()

            int firstUnderscoreCount = StringUtils.countOccurrencesOf(f1, "_")
            int secondUnderscoreCount = StringUtils.countOccurrencesOf(f2, "_")

            if(firstUnderscoreCount == secondUnderscoreCount) {
                return 0;
            }
            else {
                return firstUnderscoreCount > secondUnderscoreCount ?  1 : -1;
            }
        }
    }

    static void loadFromResources(Properties properties, Resource[] resources) throws IOException {
        for (Resource messageResource : resources) {
            InputStream inputStream = messageResource.getInputStream()
            try {
                properties.load(new InputStreamReader(inputStream, Charset.forName(System.getProperty("file.encoding", "UTF-8"))))
            } finally {
                try {
                    inputStream.close()
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
