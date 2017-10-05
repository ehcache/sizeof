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

import org.ehcache.sizeof.filters.SizeOfFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Alex Snaps
 */
public final class Configuration {
    private final int maxDepth;
    private final boolean abort;
    private final boolean silent;
    private final SizeOfFilter[] filters;

    public Configuration(final int maxDepth, final boolean abort, final boolean silent, final SizeOfFilter... filters) {
        this.maxDepth = maxDepth;
        this.abort = abort;
        this.silent = silent;
        this.filters = filters;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public boolean isAbort() {
        return abort;
    }

    public boolean isSilent() {
        return silent;
    }

    public SizeOfFilter[] getFilters() {
        return filters;
    }

    public static final class Builder {

        private int maxDepth;
        private boolean silent;
        private boolean abort;
        private final List<SizeOfFilter> filters = new ArrayList<>();

        public Builder() {
        }

        public Builder(Configuration cfg) {
            maxDepth(cfg.maxDepth);
            silent(cfg.silent);
            abort(cfg.abort);
            Collections.addAll(filters, cfg.filters);
        }

        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public Builder silent(boolean silent) {
            this.silent = silent;
            return this;
        }

        public Builder abort(boolean abort) {
            this.abort = abort;
            return this;
        }

        public Builder addFilter(SizeOfFilter filter) {
            if (!filters.contains(filter)) {
                filters.add(filter);
            }
            return this;
        }

        public Builder addFilters(SizeOfFilter... filters) {
            for (SizeOfFilter filter : filters) {
                addFilter(filter);
            }
            return this;
        }

        public Builder removeFilter(SizeOfFilter filter) {
            filters.remove(filter);
            return this;
        }

        public Builder removeFilters(SizeOfFilter... filters) {
            this.filters.removeAll(Arrays.asList(filters));
            return this;
        }

        public Builder clearlFilters() {
            this.filters.clear();
            return this;
        }

        public Configuration build() {
            return new Configuration(maxDepth, abort, silent, filters.toArray(new SizeOfFilter[filters.size()]));
        }
    }
}
