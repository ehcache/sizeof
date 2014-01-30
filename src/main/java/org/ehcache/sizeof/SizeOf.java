/**
 *  Copyright Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ehcache.sizeof;

import org.ehcache.sizeof.filters.CombinationSizeOfFilter;
import org.ehcache.sizeof.filters.SizeOfFilter;
import org.ehcache.sizeof.impl.AgentSizeOf;
import org.ehcache.sizeof.impl.ReflectionSizeOf;
import org.ehcache.sizeof.impl.UnsafeSizeOf;
import org.ehcache.sizeof.util.WeakIdentityConcurrentMap;

/**
 * Abstract sizeOf for Java. It will rely on a proper sizeOf to measure sizes of entire object graphs
 *
 * @author Alex Snaps
 */
public abstract class SizeOf {

    private final ObjectGraphWalker walker;

    /**
     * Builds a new SizeOf that will filter fields according to the provided filter
     *
     * @param fieldFilter The filter to apply
     * @param caching     whether to cache reflected fields
     * @see net.sf.ehcache.pool.sizeof.filter.SizeOfFilter
     */
    public SizeOf(SizeOfFilter fieldFilter, boolean caching) {
        ObjectGraphWalker.Visitor visitor;
        if (caching) {
            visitor = new CachingSizeOfVisitor();
        } else {
            visitor = new SizeOfVisitor();
        }
        this.walker = new ObjectGraphWalker(visitor, fieldFilter);
    }

    /**
     * Calculates the size in memory (heap) of the instance passed in, not navigating the down graph
     *
     * @param obj the object to measure the size of
     * @return the object size in memory in bytes
     */
    public abstract long sizeOf(Object obj);

    /**
     * Measures the size in memory (heap) of the objects passed in, walking their graph down
     * Any overlap of the graphs being passed in will be recognized and only measured once
     *
     * @param maxDepth                  maximum depth of the object graph to traverse
     * @param abortWhenMaxDepthExceeded true if the object traversal should be aborted when the max depth is exceeded
     * @param obj                       the root objects of the graphs to measure
     * @return the total size in bytes for these objects
     * @see #sizeOf(Object)
     */
    public Size deepSizeOf(int maxDepth, boolean abortWhenMaxDepthExceeded, Object... obj) {
        return new Size(walker.walk(maxDepth, abortWhenMaxDepthExceeded, obj), true);
    }

    public static SizeOf newInstance(final SizeOfFilter... filters) {
        final SizeOfFilter filter = new CombinationSizeOfFilter(filters);
        try {
            return new AgentSizeOf(filter);
        } catch (UnsupportedOperationException e) {
            try {
                return new UnsafeSizeOf(filter);
            } catch (UnsupportedOperationException f) {
                try {
                    return new ReflectionSizeOf(filter);
                } catch (UnsupportedOperationException g) {
                    throw new UnsupportedOperationException("A suitable SizeOf engine could not be loaded: " + e + ", " + f + ", " + g);
                }
            }
        }
    }

    /**
     * Will return the sizeOf each instance
     */
    private class SizeOfVisitor implements ObjectGraphWalker.Visitor {

        /**
         * {@inheritDoc}
         */
        public long visit(Object object) {
            return sizeOf(object);
        }
    }

    /**
     * Will Cache already visited types
     */
    private class CachingSizeOfVisitor implements ObjectGraphWalker.Visitor {
        private final WeakIdentityConcurrentMap<Class<?>, Long> cache = new WeakIdentityConcurrentMap<Class<?>, Long>();

        /**
         * {@inheritDoc}
         */
        public long visit(final Object object) {
            Class<?> klazz = object.getClass();
            Long cachedSize = cache.get(klazz);
            if (cachedSize == null) {
                if (klazz.isArray()) {
                    return sizeOf(object);
                } else {
                    long size = sizeOf(object);
                    cache.put(klazz, size);
                    return size;
                }
            } else {
                return cachedSize.longValue();
            }
        }
    }
}