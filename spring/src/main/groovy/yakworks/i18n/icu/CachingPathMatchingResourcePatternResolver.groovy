/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.i18n.icu

import groovy.transform.CompileStatic
import groovy.transform.Memoized

import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

/**
 * Caching for PathMatching.
 * @author Graeme Rocher
 * @since 3.0.12
 */
@CompileStatic
class CachingPathMatchingResourcePatternResolver extends PathMatchingResourcePatternResolver {
    public static final CachingPathMatchingResourcePatternResolver INSTANCE = new CachingPathMatchingResourcePatternResolver();

    private CachingPathMatchingResourcePatternResolver(){}

    CachingPathMatchingResourcePatternResolver(ResourceLoader parent) {
        super(parent)
    }

    @Memoized(maxCacheSize = 20)
    protected Set<Resource> doFindAllClassPathResources(String path) throws IOException {
        return super.doFindAllClassPathResources(path)
    }

    @Memoized(maxCacheSize = 20)
    protected Resource[] findPathMatchingResources(String locationPattern) throws IOException {
        return super.findPathMatchingResources(locationPattern)
    }
}
