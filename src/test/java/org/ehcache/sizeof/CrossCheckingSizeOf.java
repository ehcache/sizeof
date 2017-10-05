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

import org.ehcache.sizeof.filters.SizeOfFilter;
import org.ehcache.sizeof.impl.AgentSizeOf;
import org.ehcache.sizeof.impl.PassThroughFilter;
import org.ehcache.sizeof.impl.ReflectionSizeOf;
import org.ehcache.sizeof.impl.UnsafeSizeOf;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.ehcache.sizeof.impl.JvmInformation.CURRENT_JVM_INFORMATION;

/**
 * @author Alex Snaps
 */
public class CrossCheckingSizeOf extends SizeOf {

    private final List<SizeOf> engines;

    public CrossCheckingSizeOf() {
        this(new PassThroughFilter());
    }

    public CrossCheckingSizeOf(boolean bypassFlyweight) {
        this(new PassThroughFilter(), true, bypassFlyweight);
    }

    public CrossCheckingSizeOf(SizeOfFilter filter) {
        this(filter, true, true);
    }

    public CrossCheckingSizeOf(SizeOfFilter filter, boolean caching, boolean bypassFlyweight) {
        super(filter, caching, bypassFlyweight);
        engines = new ArrayList<>();

        try {
            engines.add(new AgentSizeOf());
        } catch (UnsupportedOperationException usoe) {
            System.err.println("Not using AgentSizeOf: " + usoe);
        }
        try {
            engines.add(new UnsafeSizeOf());
        } catch (UnsupportedOperationException usoe) {
            System.err.println("Not using UnsafeSizeOf: " + usoe);
        }
        if (CURRENT_JVM_INFORMATION.supportsReflectionSizeOf()) {
            try {
                engines.add(new ReflectionSizeOf());
            } catch (UnsupportedOperationException usoe) {
                System.err.println("Not using ReflectionSizeOf: " + usoe);
            }
        } else {
            System.err.println(CURRENT_JVM_INFORMATION.getJvmDescription() + " detected: not using ReflectionSizeOf");
        }

        if (engines.isEmpty()) {
            throw new AssertionError("No SizeOf engines available");
        }
    }

    @Override
    public long sizeOf(Object obj) {
        long[] values = new long[engines.size()];
        for (int i = 0; i < engines.size(); i++) {
            values[i] = engines.get(i).sizeOf(obj);
        }
        for (long value : values) {
            if (values[0] != value) {
                StringBuilder sb = new StringBuilder("Values do not match for ");
                sb.append(obj.getClass());
                if (obj.getClass().isArray()) {
                    sb.append(" length:").append(Array.getLength(obj));
                }
                sb.append(" - ");
                for (int i = 0; i < engines.size(); i++) {
                    sb.append(engines.get(i).getClass().getSimpleName()).append(":").append(values[i]);
                    if (i != engines.size() - 1) {
                        sb.append(", ");
                    }
                }
                throw new AssertionError(sb.toString());
            }
        }

        return values[0];
    }
}
