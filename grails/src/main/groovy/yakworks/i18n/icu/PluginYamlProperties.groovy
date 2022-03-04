/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n.icu

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.core.io.StaticResourceLoader
import org.grails.plugins.BinaryGrailsPlugin
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.util.StringUtils

import grails.io.IOUtils
import grails.plugins.GrailsPlugin
import grails.plugins.GrailsPluginManager
import grails.plugins.exceptions.PluginException

/**
 * Helper to load message properties and message yamls from plugins
 */
@Slf4j
@CompileStatic
class PluginYamlProperties {

    GrailsPluginManager pluginManager
    String ymlSuffix = ".yml"
    String messagesYmlPattern = "message*.yml"
    private final YamlPropertiesFactoryBean yamlProcessor = new YamlPropertiesFactoryBean()

    PluginYamlProperties(GrailsPluginManager pluginManager){
        this.pluginManager = pluginManager
    }

    void mergePluginProperties(final Locale locale, Properties mergedProps) {
        final GrailsPlugin[] allPlugins = pluginManager.getAllPlugins()

        for (GrailsPlugin plugin : allPlugins) {
            if (plugin instanceof BinaryGrailsPlugin) {
                BinaryGrailsPlugin binaryPlugin = (BinaryGrailsPlugin) plugin
                //stock properties
                final Properties binaryPluginProperties = binaryPlugin.getProperties(locale)
                if (binaryPluginProperties != null) {
                    mergedProps.putAll(binaryPluginProperties)
                }
                //add yamls if any
                final Properties yamlProperties = getPluginMessageYaml(binaryPlugin, locale)
                if (yamlProperties != null) {
                    mergedProps.putAll(yamlProperties)
                }
            }
        }
    }

    Properties getPluginMessageYaml(BinaryGrailsPlugin binaryPlugin , final Locale locale) {
        def pluginClass = binaryPlugin.getPluginClass()
        URL rootResourcesURL = IOUtils.findRootResourcesURL(pluginClass)
        if(rootResourcesURL == null) {
            throw new PluginException("Cannot evaluate plugin location for plugin " + pluginClass)
        }
        Resource url = new UrlResource(rootResourcesURL)

        Properties properties = null;
        if(url != null) {
            StaticResourceLoader resourceLoader = new StaticResourceLoader();
            resourceLoader.baseResource= url
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
            try {
                // first load all ymls
                Resource[] resources = resolver.getResources(messagesYmlPattern)
                //filter them down
                resources = resources.length > 0 ? filterResources(resources, locale) : resources

                if(resources.length > 0) {
                    yamlProcessor.setResources(resources)
                    properties = yamlProcessor.getObject()
                }
            } catch (IOException e) {
                log.error("IOException loading i18n yaml messages", e)
            }
        }
        return properties;
    }

    Resource[] filterResources(Resource[] resources, Locale locale) {

        List<Resource> finalResources = []

        for (Resource resource : resources) {
            String fn = resource.getFilename()

            if(fn.indexOf('_') > -1) {
                if(fn.endsWith('_' + locale.toString() + ymlSuffix)) {
                    finalResources.add(resource)
                }
                else if(fn.endsWith('_' + locale.getLanguage() + '_' + locale.getCountry() + ymlSuffix)) {
                    finalResources.add(resource)
                }
                else if(fn.endsWith('_' + locale.getLanguage() + ymlSuffix)) {
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
    Resource[] sortResources(Resource[] resources){
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
}
