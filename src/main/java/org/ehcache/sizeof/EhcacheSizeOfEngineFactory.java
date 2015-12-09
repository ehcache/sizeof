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


/**
 * @author Alex Snaps
 */
public class EhcacheSizeOfEngineFactory implements SizeOfEngineFactory {

    private final EhcacheFilterSource ehcacheFilterSource;

    public EhcacheSizeOfEngineFactory() {
        this(new EhcacheFilterSource(true));
    }

    public EhcacheSizeOfEngineFactory(final EhcacheFilterSource ehcacheFilterSource) {
        this.ehcacheFilterSource = ehcacheFilterSource;
    }

    @Override
    public EhcacheSizeOfEngine createSizeOfEngine(final int maxDepth, final boolean abort, final boolean silent) {
        return new EhcacheSizeOfEngine(new Configuration(maxDepth, abort, silent, ehcacheFilterSource.getFilters()));
    }

    public Filter getFilter() {
        return ehcacheFilterSource;
    }
}
