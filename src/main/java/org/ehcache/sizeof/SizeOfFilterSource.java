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

import org.ehcache.sizeof.filters.AnnotationSizeOfFilter;
import org.ehcache.sizeof.filters.SizeOfFilter;
import org.ehcache.sizeof.filters.TypeFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Alex Snaps
 */
public final class SizeOfFilterSource implements Filter {

    private final CopyOnWriteArrayList<SizeOfFilter> filters = new CopyOnWriteArrayList<>();
    private final TypeFilter typeFilter = new TypeFilter();

    public SizeOfFilterSource(boolean registerAnnotationFilter) {
        filters.add(typeFilter);
        if (registerAnnotationFilter) {
            filters.add(new AnnotationSizeOfFilter());
        }
        applyMutators();
    }

    private void applyMutators() {
        applyMutators(SizeOfFilterSource.class.getClassLoader());
    }

    void applyMutators(final ClassLoader classLoader) {
        final ServiceLoader<FilterConfigurator> loader = ServiceLoader.load(FilterConfigurator.class, classLoader);
        for (FilterConfigurator filterConfigurator : loader) {
            filterConfigurator.configure(this);
        }
    }

    public SizeOfFilter[] getFilters() {
        List<SizeOfFilter> allFilters = new ArrayList<>(filters);
        return allFilters.toArray(new SizeOfFilter[allFilters.size()]);
    }

    public void ignoreInstancesOf(final Class clazz, final boolean strict) {
        typeFilter.addClass(clazz, Modifier.isFinal(clazz.getModifiers()) || strict);
    }

    public void ignoreField(final Field field) {
        typeFilter.addField(field);
    }

}
