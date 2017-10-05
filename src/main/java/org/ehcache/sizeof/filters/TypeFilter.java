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

import org.ehcache.sizeof.util.WeakIdentityConcurrentMap;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Alex Snaps
 */
public class TypeFilter implements SizeOfFilter {

    private final WeakIdentityConcurrentMap<Class<?>, Object> classesIgnored = new WeakIdentityConcurrentMap<>();
    private final WeakIdentityConcurrentMap<Class<?>, Object> superClasses = new WeakIdentityConcurrentMap<>();
    private final WeakIdentityConcurrentMap<Class<?>, ConcurrentMap<Field, Object>> fieldsIgnored = new WeakIdentityConcurrentMap<>();

    @Override
    public Collection<Field> filterFields(final Class<?> klazz, final Collection<Field> fields) {
        final ConcurrentMap<Field, Object> fieldsToIgnore = fieldsIgnored.get(klazz);
        if (fieldsToIgnore != null) {
            fields.removeIf(fieldsToIgnore::containsKey);
        }
        return fields;
    }

    @Override
    public boolean filterClass(final Class<?> klazz) {
        if (!classesIgnored.containsKey(klazz)) {
            for (Class<?> aClass : superClasses.keySet()) {
                if (aClass.isAssignableFrom(klazz)) {
                    classesIgnored.put(klazz, this);
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void addClass(final Class<?> classToFilterOut, final boolean strict) {
        if (!strict) {
            superClasses.putIfAbsent(classToFilterOut, this);
        } else {
            classesIgnored.put(classToFilterOut, this);
        }
    }

    public void addField(final Field fieldToFilterOut) {
        final Class<?> klazz = fieldToFilterOut.getDeclaringClass();
        ConcurrentMap<Field, Object> fields = fieldsIgnored.get(klazz);
        if (fields == null) {
            fields = new ConcurrentHashMap<>();
            final ConcurrentMap<Field, Object> previous = fieldsIgnored.putIfAbsent(klazz, fields);
            if (previous != null) {
                fields = previous;
            }
        }
        fields.put(fieldToFilterOut, this);
    }
}
