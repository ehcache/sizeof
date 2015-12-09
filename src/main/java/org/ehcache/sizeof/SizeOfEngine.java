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
 * SizeOf engines are used to calculate the size of elements stored in poolable stores.
 *
 * @author Ludovic Orban
 */
public interface SizeOfEngine {

    /**
     * Size an element
     *
     * @param key the key of the element
     * @param value the value of the element
     * @param container the container of the element, ie: element object + eventual overhead
     * @return the size of the element in bytes
     */
    Size sizeOf(Object key, Object value, Object container);

    /**
     * Make a copy of the SizeOf engine, preserving all of its internal state but overriding the specified parameters
     *
     * @param maxDepth maximum depth of the object graph to traverse
     * @param abortWhenMaxDepthExceeded true if the object traversal should be aborted when the max depth is exceeded
     * @return a copy of the SizeOf engine using the specified parameters
     */
    SizeOfEngine copyWith(int maxDepth, boolean abortWhenMaxDepthExceeded);

}
