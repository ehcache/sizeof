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

import net.sf.ehcache.pool.Size;
import net.sf.ehcache.pool.SizeOfEngine;
import net.sf.ehcache.pool.sizeof.MaxDepthExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Snaps
 */
public class EhcacheSizeOfEngine implements SizeOfEngine {

    private static final Logger LOG = LoggerFactory.getLogger(EhcacheSizeOfEngine.class.getName());
    private static final String VERBOSE_DEBUG_LOGGING = "net.sf.ehcache.sizeof.verboseDebugLogging";

    private static final boolean USE_VERBOSE_DEBUG_LOGGING = Boolean.getBoolean(VERBOSE_DEBUG_LOGGING);

    private final Configuration cfg;
    private final SizeOf sizeOf;

    public EhcacheSizeOfEngine(Configuration cfg) {
        this.cfg = cfg;
        this.sizeOf = SizeOf.newInstance(cfg.getFilters());
    }

    public Configuration getConfiguration() {
        return cfg;
    }

    /**
     * {@inheritDoc}
     */
    public SizeOfEngine copyWith(int maxDepth, boolean abortWhenMaxDepthExceeded) {
        return new EhcacheSizeOfEngine(this.cfg);
    }

    /**
     * {@inheritDoc}
     */
    public Size sizeOf(final Object key, final Object value, final Object container) {
        Size size;
        try {
            org.ehcache.sizeof.Size ourSize = sizeOf.deepSizeOf(cfg.getMaxDepth(), cfg.isAbort(), key, value, container);
            size = new Size(ourSize.getCalculated(), ourSize.isExact());
        } catch (MaxDepthExceededException e) {
            LOG.warn(e.getMessage());
            LOG.warn("key type: " + key.getClass().getName());
            LOG.warn("key: " + key);
            LOG.warn("value type: " + value.getClass().getName());
            LOG.warn("value: " + value);
            LOG.warn("container: " + container);
            size = new Size(e.getMeasuredSize(), false);
        }

        if (USE_VERBOSE_DEBUG_LOGGING && LOG.isDebugEnabled()) {
            LOG.debug("size of {}/{}/{} -> {}", new Object[] { key, value, container, size.getCalculated() });
        }
        return size;
    }
}
