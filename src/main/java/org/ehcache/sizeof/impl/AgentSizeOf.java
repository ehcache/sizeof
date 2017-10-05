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
package org.ehcache.sizeof.impl;

import org.ehcache.sizeof.SizeOf;
import org.ehcache.sizeof.filters.SizeOfFilter;

import static org.ehcache.sizeof.impl.JvmInformation.CURRENT_JVM_INFORMATION;

/**
 * SizeOf implementation that relies on a Java agent to be loaded to do the measurement
 * It will try to load the agent through the JDK6 Attach API if available
 * All it's constructor do throw UnsupportedOperationException if the agent isn't present or couldn't be loaded dynamically
 *
 * Inspired by Dr. Heinz Kabutz's Java Specialist Newsletter Issue #142
 *
 * @author Chris Dennis
 * @author Alex Snaps
 *
 * @link http://www.javaspecialists.eu/archive/Issue142.html
 */
public class AgentSizeOf extends SizeOf {

    /**
     * System property name to bypass attaching to the VM and loading of Java agent to measure Object sizes.
     */
    public static final String BYPASS_LOADING = "org.ehcache.sizeof.AgentSizeOf.bypass";

    private static final boolean AGENT_LOADED = !Boolean.getBoolean(BYPASS_LOADING) && AgentLoader.loadAgent();

    /**
     * Builds a new SizeOf that will not filter fields and will cache reflected fields
     *
     * @throws UnsupportedOperationException If agent couldn't be loaded or isn't present
     * @see #AgentSizeOf(SizeOfFilter, boolean, boolean)
     */
    public AgentSizeOf() throws UnsupportedOperationException {
        this(new PassThroughFilter());
    }

    /**
     * Builds a new SizeOf that will filter fields according to the provided filter and will cache reflected fields
     *
     * @param filter The filter to apply
     * @throws UnsupportedOperationException If agent couldn't be loaded or isn't present
     * @see #AgentSizeOf(SizeOfFilter, boolean, boolean)
     * @see org.ehcache.sizeof.filters.SizeOfFilter
     */
    public AgentSizeOf(SizeOfFilter filter) throws UnsupportedOperationException {
        this(filter, true, true);
    }

    /**
     * Builds a new SizeOf that will filter fields according to the provided filter
     *
     * @param filter            The filter to apply
     * @param caching           whether to cache reflected fields
     * @param bypassFlyweight   whether "Flyweight Objects" are to be ignored
     * @throws UnsupportedOperationException If agent couldn't be loaded or isn't present
     * @see SizeOfFilter
     */
    public AgentSizeOf(SizeOfFilter filter, boolean caching, boolean bypassFlyweight) throws UnsupportedOperationException {
        super(filter, caching, bypassFlyweight);
        if (!AGENT_LOADED) {
            throw new UnsupportedOperationException("Agent not available or loadable");
        }
    }

    @Override
    public long sizeOf(Object obj) {
        final long measuredSize = AgentLoader.agentSizeOf(obj);
        return Math.max(CURRENT_JVM_INFORMATION.getMinimumObjectSize(),
            measuredSize + CURRENT_JVM_INFORMATION.getAgentSizeOfAdjustment());
    }
}
