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
public interface SizeOfEngineFactory {

    /**
     * Creates a new instance of a SizeOfEngine
     *
     * @param maxObjectCount the max object graph that will be traversed.
     * @param abort          true if the object traversal should be aborted when the max depth is exceeded
     * @param silent         true if no info log explaining which agent was chosen should be printed
     * @return the new instance
     */
    SizeOfEngine createSizeOfEngine(int maxObjectCount, boolean abort, boolean silent);
}
