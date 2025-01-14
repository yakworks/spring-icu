/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yakworks.i18n.icu;

import com.ibm.icu.text.MessageFormat;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ICU4j Overrides, Lots of copy/paste horseplay as so much in ReloadableResourceBundleMessageSource is private and final.
 * Also instead of supporting XML like it does, we support YAMl instead.
 * The core issue here is that we need to return com.ibm.icu.text.MessageFormat and not java.text.MessageFormat.
 *
 * TODO: Need to confirm what wins if defined in both places.
 */
public class ICUBundleMessageSource extends ReloadableResourceBundleMessageSource {

    private static final String PROPERTIES_SUFFIX = ".properties";

    private static final String YML_SUFFIX = ".yml";

    @Nullable
    private Properties fileEncodings;

    private boolean concurrentRefresh = true;

    private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();

    private ResourceLoader resourceLoader = new DefaultResourceLoader();
    private final YamlPropertiesFactoryBean yamlProcessor = new YamlPropertiesFactoryBean();

    // Cache to hold already loaded properties per filename
    private final ConcurrentMap<String, PropertiesHolder> cachedProperties = new ConcurrentHashMap<>();

    // Cache to hold already loaded properties per filename
    private final ConcurrentMap<Locale, PropertiesHolder> cachedMergedProperties = new ConcurrentHashMap<>();

    // Cache to hold filename lists per Locale
    private final ConcurrentMap<String, Map<Locale, List<String>>> cachedFilenames = new ConcurrentHashMap<>();

    PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper(
        PropertySourcesPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX, PropertySourcesPlaceholderConfigurer.DEFAULT_PLACEHOLDER_SUFFIX,
        PropertySourcesPlaceholderConfigurer.DEFAULT_VALUE_SEPARATOR, true);

    @Override
    @Nullable
    protected java.text.MessageFormat resolveCode(String code, Locale locale) {
        throw new UnsupportedOperationException("Use resolveCodeICU for ibm.icu");
    }

    /**
     * Resolves the given message code as key in the retrieved bundle files,
     * using a cached MessageFormat instance per message code.
     */
    @Nullable
    protected MessageFormat resolveCodeICU(String code, Locale locale) {
        if (getCacheMillis() < 0) {
            PropertiesHolder propHolder = getMergedProperties(locale);
            MessageFormat result = propHolder.getMessageFormatICU(code, locale);
            if (result != null) {
                return result;
            }
        }
        else {
            for (String basename : getBasenameSet()) {
                List<String> filenames = calculateAllFilenames(basename, locale);
                for (String filename : filenames) {
                    PropertiesHolder propHolder = getProperties(filename);
                    MessageFormat result = propHolder.getMessageFormatICU(code, locale);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    protected void mergePluginProperties(final Locale locale, Properties mergedProps) {
        //empty, can be overridden for loading defualts first
    }

    protected long mergeExternalProperties(final Locale locale, Properties mergedProps) {
        //empty, can be overridden for loading external overrides last
        return -1;
    }

    @Override
    protected List<String> calculateAllFilenames(String basename, Locale locale) {
        Map<Locale, List<String>> localeMap = this.cachedFilenames.get(basename);
        if (localeMap != null) {
            List<String> filenames = localeMap.get(locale);
            if (filenames != null) {
                return filenames;
            }
        }
        List<String> filenames = super.calculateAllFilenames(basename, locale);

        StringBuilder temp = new StringBuilder(basename);
        temp.append('_').append(locale.getLanguage()).append(YML_SUFFIX);
        filenames.add(0, temp.toString());
        //the default from super should be last one so insert right before it
        filenames.add(filenames.size()-1, basename + YML_SUFFIX);

        if (localeMap == null) {
            localeMap = new ConcurrentHashMap<>();
            Map<Locale, List<String>> existing = this.cachedFilenames.putIfAbsent(basename, localeMap);
            if (existing != null) {
                localeMap = existing;
            }
        }
        localeMap.put(locale, filenames);
        return filenames;
    }

    @Override
    protected PropertiesHolder getMergedProperties(Locale locale) {
        PropertiesHolder mergedHolder = this.cachedMergedProperties.get(locale);
        if (mergedHolder != null) {
            return mergedHolder;
        }

        Properties mergedProps = newProperties();
        //this method does nothing here but is called for users to override. for example in grails we want to merge plugins first
        mergePluginProperties(locale, mergedProps);

        long latestTimestamp = -1;
        String[] basenames = StringUtils.toStringArray(getBasenameSet());
        for (int i = basenames.length - 1; i >= 0; i--) {
            List<String> filenames = calculateAllFilenames(basenames[i], locale);
            for (int j = filenames.size() - 1; j >= 0; j--) {
                String filename = filenames.get(j);
                PropertiesHolder propHolder = getProperties(filename);
                if (propHolder.getProperties() != null) {
                    mergedProps.putAll(propHolder.getProperties());
                    if (propHolder.getFileTimestamp() > latestTimestamp) {
                        latestTimestamp = propHolder.getFileTimestamp();
                    }
                }
            }
        }

        // merge externals if method is implemented
        long latestExternalTimestamp = mergeExternalProperties(locale, mergedProps);
        if(latestExternalTimestamp != -1){
            latestTimestamp = latestExternalTimestamp;
        }

        mergedHolder = new PropertiesHolder(mergedProps, latestTimestamp);
        PropertiesHolder existing = this.cachedMergedProperties.putIfAbsent(locale, mergedHolder);
        if (existing != null) {
            mergedHolder = existing;
        }
        return mergedHolder;
    }

    @Override
    protected PropertiesHolder getProperties(String filename) {
        PropertiesHolder propHolder = this.cachedProperties.get(filename);
        long originalTimestamp = -2;

        if (propHolder != null) {
            originalTimestamp = propHolder.getRefreshTimestamp();
            if (originalTimestamp == -1 || originalTimestamp > System.currentTimeMillis() - getCacheMillis()) {
                // Up to date
                return propHolder;
            }
        }
        else {
            propHolder = new PropertiesHolder();
            PropertiesHolder existingHolder = this.cachedProperties.putIfAbsent(filename, propHolder);
            if (existingHolder != null) {
                propHolder = existingHolder;
            }
        }

        // At this point, we need to refresh...
        if (this.concurrentRefresh && propHolder.getRefreshTimestamp() >= 0) {
            // A populated but stale holder -> could keep using it.
            if (!propHolder.refreshLock.tryLock()) {
                // Getting refreshed by another thread already ->
                // let's return the existing properties for the time being.
                return propHolder;
            }
        }
        else {
            propHolder.refreshLock.lock();
        }
        try {
            PropertiesHolder existingHolder = this.cachedProperties.get(filename);
            if (existingHolder != null && existingHolder.getRefreshTimestamp() > originalTimestamp) {
                return existingHolder;
            }
            return refreshPropertiesICU(filename, propHolder);
        }
        finally {
            propHolder.refreshLock.unlock();
        }
    }

    public PropertiesHolder refreshPropertiesICU(String filename, @Nullable PropertiesHolder propHolder) {
        long refreshTimestamp = (getCacheMillis() < 0 ? -1 : System.currentTimeMillis());

        Resource resource;
        //yaml files are passed in with the suffix already.
        if(filename.endsWith(YML_SUFFIX)) {
            resource = this.resourceLoader.getResource(filename);
        } else {
            resource = this.resourceLoader.getResource(filename + PROPERTIES_SUFFIX);
        }

        if (resource.exists()) {
            long fileTimestamp = -1;
            if (getCacheMillis() >= 0) {
                // Last-modified timestamp of file will just be read if caching with timeout.
                try {
                    fileTimestamp = resource.lastModified();
                    if (propHolder != null && propHolder.getFileTimestamp() == fileTimestamp) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Re-caching properties for filename [" + filename + "] - file hasn't been modified");
                        }
                        propHolder.setRefreshTimestamp(refreshTimestamp);
                        return propHolder;
                    }
                }
                catch (IOException ex) {
                    // Probably a class path resource: cache it forever.
                    if (logger.isDebugEnabled()) {
                        logger.debug(resource + " could not be resolved in the file system - assuming that it hasn't changed", ex);
                    }
                    fileTimestamp = -1;
                }
            }
            try {
                Properties props = loadProperties(resource, filename);
                propHolder = new PropertiesHolder(props, fileTimestamp);
            }
            catch (IOException ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Could not parse properties file [" + resource.getFilename() + "]", ex);
                }
                // Empty holder representing "not valid".
                propHolder = new PropertiesHolder();
            }
        }

        else {
            // Resource does not exist.
            if (logger.isDebugEnabled()) {
                logger.debug("No properties file found for [" + filename + "] - neither plain properties nor XML");
            }
            // Empty holder representing "not found".
            propHolder = new PropertiesHolder();
        }

        propHolder.setRefreshTimestamp(refreshTimestamp);
        this.cachedProperties.put(filename, propHolder);
        return propHolder;
    }

    @Override
    protected Properties loadProperties(Resource resource, String filename) throws IOException {
        Properties props = newProperties();
        try (InputStream is = resource.getInputStream()) {
            String resourceFilename = resource.getFilename();
            if (resourceFilename != null && (resourceFilename.endsWith(YML_SUFFIX) )) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Loading properties [" + resource.getFilename() + "]");
                }
                yamlProcessor.setResources(resource);
                return yamlProcessor.getObject();
            } else {
                //otherwise use the default properties loader from super
                return super.loadProperties(resource, filename);
            }

        }
    }

    public void clearCache() {
        logger.debug("Clearing entire resource bundle cache");
        this.cachedProperties.clear();
        this.cachedMergedProperties.clear();
    }

    /**
     * Override
     */
    protected class PropertiesHolder extends ReloadableResourceBundleMessageSource.PropertiesHolder {

        private final ReentrantLock refreshLock = new ReentrantLock();

        /** Cache to hold already generated MessageFormats per message code. */
        private final ConcurrentMap<String, Map<Locale, com.ibm.icu.text.MessageFormat>> cachedMessageFormats =
            new ConcurrentHashMap<>();

        public PropertiesHolder() {
            super();
        }

        public PropertiesHolder(Properties properties, long fileTimestamp) {
            super(properties, fileTimestamp);
        }

        @Override
        @Nullable
        public java.text.MessageFormat getMessageFormat(String code, Locale locale) {
            throw new UnsupportedOperationException("Use getMessageFormatICU for ibm.icu");
        }

        @Nullable
        public com.ibm.icu.text.MessageFormat getMessageFormatICU(String code, Locale locale) {
            if (this.getProperties() == null) {
                return null;
            }
            Map<Locale, com.ibm.icu.text.MessageFormat> localeMap = this.cachedMessageFormats.get(code);
            if (localeMap != null) {
                com.ibm.icu.text.MessageFormat result = localeMap.get(locale);
                if (result != null) {
                    return result;
                }
            }
            String msg = this.getProperties().getProperty(code);
            if (msg != null) {
                //with placeholderHelper, if string has ${..} then it will use placeholderResolver,
                // otherwise it just returns the string msg that was passed in.
                PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver = (String prop) -> {
                    return this.getProperties().getProperty(prop);
                };
                msg =  placeholderHelper.replacePlaceholders(msg, placeholderResolver);

                if (localeMap == null) {
                    localeMap = new ConcurrentHashMap<>();
                    Map<Locale, com.ibm.icu.text.MessageFormat> existing = this.cachedMessageFormats.putIfAbsent(code, localeMap);
                    if (existing != null) {
                        localeMap = existing;
                    }
                }
                com.ibm.icu.text.MessageFormat result = createMessageFormatICU(msg, locale);
                localeMap.put(locale, result);
                return result;
            }
            return null;
        }
    }

    protected com.ibm.icu.text.MessageFormat createMessageFormatICU(String msg, Locale locale) {
        return new com.ibm.icu.text.MessageFormat(msg, locale);
    }

    @Override
    protected java.text.MessageFormat createMessageFormat(String msg, Locale locale) {
        throw new UnsupportedOperationException("Use createMessageFormatICU for ibm.icu");
    }
}
