/*
* Copyright 2004-2005 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n.icu;

import java.io.IOException

import groovy.transform.CompileStatic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.util.Assert

/**
 * A ResourceLoader that loads resources from a statically defined base resource.
 *
 * @author Graeme Rocher
 * @since 0.5
 */
@CompileStatic
public class StaticResourceLoader implements ResourceLoader {
    private static final Logger LOG = LoggerFactory.getLogger(StaticResourceLoader.class);
    private Resource baseResource;

    public void setBaseResource(Resource baseResource) {
        this.baseResource = baseResource;
    }

    public Resource getResource(String location) {
        Assert.state(baseResource != null, "Property [baseResource] not set!");

        if(LOG.isDebugEnabled()) {
            LOG.debug("Loading resource for path {} from base resource {}", location, baseResource);
        }
        try {
            Resource resource = baseResource.createRelative(location);
            if(LOG.isDebugEnabled() && resource.exists()) {
                LOG.debug("Found resource for path {} from base resource {}", location, baseResource);
            }
            return resource;
        }
        catch (IOException e) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("Error loading resource for path: " + location, e);
            }
            return null;
        }
    }

    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
