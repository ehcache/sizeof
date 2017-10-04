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

import java.lang.reflect.Field;

/**
 * Filters all the sizing operation performed by a SizeOfEngine instance
 *
 * @author Alex Snaps
 */
public interface Filter {

    /**
     * Adds the class to the ignore list. Can be strict, or include subtypes
     *
     * @param clazz  the class to ignore
     * @param strict true if to be ignored strictly, or false to include sub-classes
     */
    void ignoreInstancesOf(final Class clazz, final boolean strict);

    /**
     * Adds a field to the ignore list. When that field is walked to by the SizeOfEngine, it won't navigate the graph further
     *
     * @param field the field to stop navigating the graph at
     */
    void ignoreField(final Field field);

}
