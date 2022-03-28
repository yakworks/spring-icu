/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n.icu

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.plugins.BinaryGrailsPlugin
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource

import grails.io.IOUtils
import grails.plugins.GrailsPlugin
import grails.plugins.GrailsPluginManager
import grails.plugins.exceptions.PluginException

/**
 * Helper to load message properties and message yamls from plugins
 */
@Slf4j
@CompileStatic
class PluginMessagesMerger {

    GrailsPluginManager pluginManager
    String ymlSuffix = ".yml"
    List<String> ymlLocationPatterns
    private final YamlPropertiesFactoryBean yamlProcessor = new YamlPropertiesFactoryBean()

    PluginMessagesMerger(GrailsPluginManager pluginManager){
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
            try {
                Resource[] resources = MessagesFilter.findResources(url, ymlLocationPatterns, ymlSuffix, locale)
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

}
