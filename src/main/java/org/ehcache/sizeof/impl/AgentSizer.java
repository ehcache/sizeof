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

import net.bytebuddy.agent.ByteBuddyAgent;
import org.ehcache.sizeof.ObjectSizer;

import java.lang.instrument.Instrumentation;

import static org.ehcache.sizeof.impl.JvmInformation.CURRENT_JVM_INFORMATION;

/**
 * Object sizer that relies on a Java agent to be loaded to do the measurement.
 *
 * Inspired by Dr. Heinz Kabutz's Java Specialist Newsletter Issue #142
 *
 * @author Chris Dennis
 * @author Alex Snaps
 *
 * @link http://www.javaspecialists.eu/archive/Issue142.html
 */
public class AgentSizer implements ObjectSizer {

    static {
        ByteBuddyAgent.install();
    }

    private final Instrumentation instrumentation;

    /**
     * Create a new agent-based object sizer.
     *
     * @throws UnsupportedOperationException If the agent couldn't be loaded
     */
    public AgentSizer() throws UnsupportedOperationException {
        instrumentation = getInstrumentation();
    }

    @Override
    public long sizeOf(Object obj) {
        final long measuredSize = instrumentation.getObjectSize(obj);
        return Math.max(CURRENT_JVM_INFORMATION.getMinimumObjectSize(),
            measuredSize + CURRENT_JVM_INFORMATION.getAgentSizeOfAdjustment());
    }

    private static Instrumentation getInstrumentation() {
        try {
            return ByteBuddyAgent.getInstrumentation();
        } catch (IllegalStateException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
