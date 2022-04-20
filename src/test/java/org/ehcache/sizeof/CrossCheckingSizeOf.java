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
import org.ehcache.sizeof.impl.AgentSizer;
import org.ehcache.sizeof.impl.JvmInformation;
import org.ehcache.sizeof.impl.PassThroughFilter;
import org.ehcache.sizeof.impl.ReflectionSizer;
import org.ehcache.sizeof.impl.UnsafeSizer;

import java.lang.reflect.Array;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Alex Snaps
 */
public class CrossCheckingSizeOf extends SizeOf {

    private final List<ObjectSizer> engines;

    public static Stream<ObjectSizer> objectSizers() {
        if (JvmInformation.CURRENT_JVM_INFORMATION.supportsReflectionSizeOf()) {
            return Stream.of(new AgentSizer(), new UnsafeSizer(), new ReflectionSizer());
        } else {
            return Stream.of(new AgentSizer(), new UnsafeSizer());
        }
    }

    public CrossCheckingSizeOf() {
        this(new PassThroughFilter());
    }

    public CrossCheckingSizeOf(Stream<ObjectSizer> sizers) {
        this(new PassThroughFilter(), true, true, sizers);
    }

    public CrossCheckingSizeOf(boolean bypassFlyweight) {
        this(new PassThroughFilter(), true, bypassFlyweight, objectSizers());
    }

    public CrossCheckingSizeOf(SizeOfFilter filter) {
        this(filter, true, true, objectSizers());
    }


    private CrossCheckingSizeOf(SizeOfFilter filter, boolean caching, boolean bypassFlyweight, Stream<ObjectSizer> sizerFactories) {
        super(filter, caching, bypassFlyweight);
        this.engines = sizerFactories.collect(toList());
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
