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
package org.ehcache.sizeof.filters;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Filter combining multiple filters
 *
 * @author Chris Dennis
 */
public class CombinationSizeOfFilter implements SizeOfFilter {

    private final SizeOfFilter[] filters;

    /**
     * Constructs a filter combining multiple ones
     *
     * @param filters the filters to combine
     */
    public CombinationSizeOfFilter(SizeOfFilter... filters) {
        this.filters = filters;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Field> filterFields(Class<?> klazz, Collection<Field> fields) {
        Collection<Field> current = fields;
        for (SizeOfFilter filter : filters) {
            current = filter.filterFields(klazz, current);
        }
        return current;
    }

    /**
     * {@inheritDoc}
     */
    public boolean filterClass(Class<?> klazz) {
        for (SizeOfFilter filter : filters) {
            if (!filter.filterClass(klazz)) {
                return false;
            }
        }
        return true;
    }
}
