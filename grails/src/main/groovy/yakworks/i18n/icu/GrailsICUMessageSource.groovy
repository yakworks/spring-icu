/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n.icu

import groovy.transform.CompileStatic

import org.grails.core.io.CachingPathMatchingResourcePatternResolver
import org.grails.core.support.internal.tools.ClassRelativeResourcePatternResolver
import org.grails.plugins.BinaryGrailsPlugin
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver

import grails.core.DefaultGrailsApplication
import grails.core.GrailsApplication
import grails.core.GrailsApplicationClass
import grails.core.support.GrailsApplicationAware
import grails.plugins.GrailsPlugin
import grails.plugins.GrailsPluginManager
import grails.plugins.PluginManagerAware
import grails.util.GrailsStringUtils

/**
 * Based in part on PluginAwareResourceBundleMessageSource but most of that is not needed.
 * Mostly implements the mergePluginProperties so those can get loaded before the main ones
 */
@CompileStatic
class GrailsICUMessageSource extends DefaultICUMessageSource implements GrailsApplicationAware, PluginManagerAware, InitializingBean {
    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager
    PathMatchingResourcePatternResolver resourceResolver
    private ResourceLoader localResourceLoader

    boolean searchClasspath = false
    String messageBundleLocationPattern = "classpath*:*.properties"

    GrailsICUMessageSource() {
        super()
    }

    GrailsICUMessageSource(GrailsApplication application, GrailsPluginManager pluginManager) {
        super()
        this.grailsApplication = application
        this.pluginManager = pluginManager
    }

    void afterPropertiesSet() throws Exception {

        if (localResourceLoader == null) {
            return;
        }

        Resource[] resources;

        if(searchClasspath) {
            resources = resourceResolver.getResources(messageBundleLocationPattern);
        }
        else {
            DefaultGrailsApplication defaultGrailsApplication = (DefaultGrailsApplication) grailsApplication;
            if(defaultGrailsApplication != null) {
                GrailsApplicationClass applicationClass = defaultGrailsApplication.getApplicationClass();
                if(applicationClass != null) {
                    ResourcePatternResolver resourcePatternResolver = new ClassRelativeResourcePatternResolver(applicationClass.getClass());
                    resources = resourcePatternResolver.getResources(messageBundleLocationPattern);
                }
                else {
                    resources = resourceResolver.getResources(messageBundleLocationPattern);
                }
            }
            else {
                resources = resourceResolver.getResources(messageBundleLocationPattern);
            }
        }

        // sets the basenames to used based on whats in main project
        List<String> basenames = [] as List<String>
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            String baseName = GrailsStringUtils.getFileBasename(filename);
            int i = baseName.indexOf('_');
            if(i > -1) {
                baseName = baseName.substring(0, i);
            }
            if(!basenames.contains(baseName) && !baseName == "")
                basenames.add(baseName);
        }

        setBasenames(basenames as String[]);

    }

    @Override //implement the empty mergePluginProperties which gets called first
    protected void mergePluginProperties(final Locale locale, Properties mergedProps) {
        final GrailsPlugin[] allPlugins = pluginManager.getAllPlugins();
        for (GrailsPlugin plugin : allPlugins) {
            if (plugin instanceof BinaryGrailsPlugin) {
                BinaryGrailsPlugin binaryPlugin = (BinaryGrailsPlugin) plugin;
                final Properties binaryPluginProperties = binaryPlugin.getProperties(locale);
                if (binaryPluginProperties != null) {
                    mergedProps.putAll(binaryPluginProperties);
                }
            }
        }
    }

    @Override
    void setResourceLoader(ResourceLoader resourceLoader) {
        super.setResourceLoader(resourceLoader);

        this.localResourceLoader = resourceLoader;
        if (resourceResolver == null) {
            resourceResolver = new CachingPathMatchingResourcePatternResolver(localResourceLoader);
        }
    }

}