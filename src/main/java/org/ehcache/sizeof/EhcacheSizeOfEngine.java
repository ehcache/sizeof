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
    public Size sizeOf(final Object... objects) {
        Size size;
        try {
            size = sizeOf.deepSizeOf(cfg.getMaxDepth(), cfg.isAbort(), objects);
        } catch (MaxDepthExceededException e) {
            logMaxDepthExceededException(e);
            size = new Size(e.getMeasuredSize(), false);
        }

        if (USE_VERBOSE_DEBUG_LOGGING && LOG.isDebugEnabled()) {
              LOG.debug("size of {} -> {}", new Object[] { objects, size.getCalculated() });
        }
        return size;
    }

    protected void logMaxDepthExceededException(MaxDepthExceededException e)
    {
        LOG.warn(e.getMessage());
    }
}
