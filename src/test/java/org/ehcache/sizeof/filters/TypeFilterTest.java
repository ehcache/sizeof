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

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Alex Snaps
 */
public class TypeFilterTest {

    @Test
    public void testStoresClassesToFilter() {
        TypeFilter filter = new TypeFilter();
        final Class<String> klazz = String.class;
        assertThat(filter.filterClass(klazz), is(true));
        filter.addClass(klazz, true);
        assertThat(filter.filterClass(klazz), is(false));
    }

    @Test
    public void testStoresFieldsToFilter() throws NoSuchFieldException {
        TypeFilter filter = new TypeFilter();
        final Field field = String.class.getDeclaredField("value");
        final HashSet<Field> fields = new HashSet<>();
        Collections.addAll(fields, String.class.getDeclaredFields());
        assertThat(fields.contains(field), is(true));
        filter.addField(field);
        final Collection<Field> filtered = filter.filterFields(String.class, fields);
        assertThat(filtered.contains(field), is(false));
        assertThat(filtered.isEmpty(), is(false));
    }
}
