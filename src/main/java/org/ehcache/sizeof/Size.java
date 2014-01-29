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
 * Holder for the size calculated by the SizeOf engine
 *
 * @author Ludovic Orban
 */
public final class Size {

    private final long calculated;
    private final boolean exact;

    /**
     * Constructor
     *
     * @param calculated the calculated size
     * @param exact      true if the calculated size is exact, false if it's an estimate or known to be inaccurate in some way
     */
    public Size(long calculated, boolean exact) {
        this.calculated = calculated;
        this.exact = exact;
    }

    /**
     * Get the calculated size
     *
     * @return the calculated size
     */
    public long getCalculated() {
        return calculated;
    }

    /**
     * Check if the calculated size is exact
     *
     * @return true if the calculated size is exact, false if it's an estimate or known to be inaccurate in some way
     */
    public boolean isExact() {
        return exact;
    }

}
