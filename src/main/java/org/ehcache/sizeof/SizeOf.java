/**
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * @param fieldFilter       The filter to apply
     * @param caching           whether to cache reflected fields
     * @param bypassFlyweight   whether "Flyweight Objects" are to be ignored
     * @see org.ehcache.sizeof.filters.SizeOfFilter
     */
    public SizeOf(SizeOfFilter fieldFilter, boolean caching, boolean bypassFlyweight) {
        ObjectGraphWalker.Visitor visitor;
        if (caching) {
            visitor = new CachingSizeOfVisitor();
        } else {
            visitor = new SizeOfVisitor();
        }
        this.walker = new ObjectGraphWalker(visitor, fieldFilter, bypassFlyweight);
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
     * @param listener                  A listener to visited objects
     * @param obj                       the root objects of the graphs to measure
     * @return the total size in bytes for these objects
     * @see #sizeOf(Object)
     */
    public long deepSizeOf(VisitorListener listener, Object... obj) {
        return walker.walk(listener, obj);
    }

    public long deepSizeOf(Object... obj) {
        return walker.walk(null, obj);
    }

    public static SizeOf newInstance(final SizeOfFilter... filters) {
        return newInstance(true, true, filters);
    }

    public static SizeOf newInstance(boolean bypassFlyweight, boolean cache, final SizeOfFilter... filters) {
        final SizeOfFilter filter = new CombinationSizeOfFilter(filters);
        try {
            return new AgentSizeOf(filter, cache, bypassFlyweight);
        } catch (UnsupportedOperationException e) {
            try {
                return new UnsafeSizeOf(filter, cache, bypassFlyweight);
            } catch (UnsupportedOperationException f) {
                try {
                    return new ReflectionSizeOf(filter, cache, bypassFlyweight);
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
        private final WeakIdentityConcurrentMap<Class<?>, Long> cache = new WeakIdentityConcurrentMap<>();

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
                return cachedSize;
            }
        }
    }
}
