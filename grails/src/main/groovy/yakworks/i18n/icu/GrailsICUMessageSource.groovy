/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n.icu

import groovy.transform.CompileStatic

import org.grails.core.io.CachingPathMatchingResourcePatternResolver
import org.grails.core.support.internal.tools.ClassRelativeResourcePatternResolver
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.util.AntPathMatcher

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
    //What to search for in app
    String messageBundleLocations = "classpath*:messages*.properties"
    List<String> ymlLocations = ["*messages*.yml"]

    @Value('${yakworks.i18n.externalLocation}')
    Resource externalRoot

    @Value('${yakworks.i18n.cacheSeconds:0}')
    Integer cacheSecondsConfig

    GrailsICUMessageSource() {
        super()
    }

    GrailsICUMessageSource(GrailsApplication application, GrailsPluginManager pluginManager) {
        super()
        this.grailsApplication = application
        this.pluginManager = pluginManager
    }

    void afterPropertiesSet() throws Exception {
        if(cacheSecondsConfig){
            super.setCacheSeconds(cacheSecondsConfig)
        }
        if (localResourceLoader == null) {
            return;
        }

        Resource[] resources;

        // assert externalRoot.exists()

        // make it so its not case sensitive and wild card matching is a bit easier
        //for example we can overriide and do "classpath*:*messages*.properties" to pick up ValidationMessages and messages
        (resourceResolver.pathMatcher as AntPathMatcher).setCaseSensitive(false)

        if(searchClasspath) {
            resources = resourceResolver.getResources(messageBundleLocations);
        }
        else {
            DefaultGrailsApplication defaultGrailsApplication = (DefaultGrailsApplication) grailsApplication;
            if(defaultGrailsApplication != null) {
                GrailsApplicationClass applicationClass = defaultGrailsApplication.getApplicationClass();
                if(applicationClass != null) {
                    ResourcePatternResolver resourcePatternResolver = new ClassRelativeResourcePatternResolver(applicationClass.getClass());
                    resources = resourcePatternResolver.getResources(messageBundleLocations);
                }
            }
        }

        if(!resources) return

        // sets the basenames to used based on whats in main project
        // so if no fresh files are used then it doesn't
        // TODO not clear if we need this
        List<String> basenames = [] as List<String>
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            String baseName = GrailsStringUtils.getFileBasename(filename);
            int i = baseName.indexOf('_');
            if(i > -1) {
                baseName = baseName.substring(0, i);
            }
            if(!basenames.contains(baseName) && baseName != "")
                basenames.add(baseName);
        }

        setBasenames(basenames as String[]);

    }

    PluginMessagesMerger getPluginMessagesMerger(){
        def pluginMessagesMerger = new PluginMessagesMerger(pluginManager)
        pluginMessagesMerger.ymlLocationPatterns = ymlLocations
        return pluginMessagesMerger
    }

    /**
     * Called at start and merges in the props  from anywhere
     *
     * @param locale the locale
     * @param mergedProps the base properties to merge into
     */
    @Override //implement the empty mergePluginProperties which gets called first
    protected void mergePluginProperties(final Locale locale, Properties mergedProps) {
        final GrailsPlugin[] allPlugins = pluginManager.getAllPlugins();
        getPluginMessagesMerger().mergePluginProperties(locale, mergedProps)
    }

    @Override //implement the empty mergePluginProperties which gets called first
    protected long mergeExternalProperties(final Locale locale, Properties mergedProps) {
        if(externalRoot){
            def emm = new ExternalMessagesMerger(externalRoot)
            return emm.mergeExternalProperties(locale, mergedProps)
        } else {
            return -1
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
