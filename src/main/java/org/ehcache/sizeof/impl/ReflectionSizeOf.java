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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.ehcache.sizeof.impl.JvmInformation.CURRENT_JVM_INFORMATION;

/**
 * SizeOf that uses reflection to measure on heap size of object graphs
 * Inspired by Dr. Heinz Kabutz's Java Specialist Newsletter Issue #78
 *
 * @author Alex Snaps
 * @author Chris Dennis
 *
 * @link http://www.javaspecialists.eu/archive/Issue078.html
 */
public class ReflectionSizeOf extends SizeOf {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionSizeOf.class);

    /**
     * Builds a new SizeOf that will not filter fields and will cache reflected fields
     *
     * @see #ReflectionSizeOf(SizeOfFilter, boolean, boolean)
     */
    public ReflectionSizeOf() {
        this(new PassThroughFilter());
    }

    /**
     * Builds a new SizeOf that will filter fields and will cache reflected fields
     *
     * @param fieldFilter The filter to apply
     * @see #ReflectionSizeOf(SizeOfFilter, boolean, boolean)
     * @see SizeOfFilter
     */
    public ReflectionSizeOf(SizeOfFilter fieldFilter) {
        this(fieldFilter, true, true);
    }

    /**
     * Builds a new SizeOf that will filter fields
     *
     * @param fieldFilter       The filter to apply
     * @param caching           Whether to cache reflected fields
     * @param bypassFlyweight   whether "Flyweight Objects" are to be ignored
     * @see SizeOfFilter
     */
    public ReflectionSizeOf(SizeOfFilter fieldFilter, boolean caching, boolean bypassFlyweight) {
        super(fieldFilter, caching, bypassFlyweight);

        if (!CURRENT_JVM_INFORMATION.supportsReflectionSizeOf()) {
            LOGGER.warn("ReflectionSizeOf is not always accurate on the JVM (" + CURRENT_JVM_INFORMATION.getJvmDescription() +
                        ").  Please consider enabling AgentSizeOf.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long sizeOf(Object obj) {
        if (obj == null) {
            return 0;
        }

        Class<?> aClass = obj.getClass();
        if (aClass.isArray()) {
            return guessArraySize(obj);
        } else {
            long size = CURRENT_JVM_INFORMATION.getObjectHeaderSize();

            Deque<Class<?>> classStack = new ArrayDeque<>();
            for (Class<?> klazz = aClass; klazz != null; klazz = klazz.getSuperclass()) {
                classStack.push(klazz);
            }

            while (!classStack.isEmpty()) {
                Class<?> klazz = classStack.pop();

                //assuming default class layout
                int oops = 0;
                int doubles = 0;
                int words = 0;
                int shorts = 0;
                int bytes = 0;
                for (Field f : klazz.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    if (f.getType().isPrimitive()) {
                        switch (PrimitiveType.forType(f.getType())) {
                            case BOOLEAN:
                            case BYTE:
                                bytes++;
                                break;
                            case SHORT:
                            case CHAR:
                                shorts++;
                                break;
                            case INT:
                            case FLOAT:
                                words++;
                                break;
                            case DOUBLE:
                            case LONG:
                                doubles++;
                                break;
                            default:
                                throw new AssertionError();
                        }
                    } else {
                        oops++;
                    }
                }
                if (doubles > 0 && (size % PrimitiveType.LONG.getSize()) != 0) {
                    long length = PrimitiveType.LONG.getSize() - (size % PrimitiveType.LONG.getSize());
                    size += PrimitiveType.LONG.getSize() - (size % PrimitiveType.LONG.getSize());

                    while (length >= PrimitiveType.INT.getSize() && words > 0) {
                        length -= PrimitiveType.INT.getSize();
                        words--;
                    }
                    while (length >= PrimitiveType.SHORT.getSize() && shorts > 0) {
                        length -= PrimitiveType.SHORT.getSize();
                        shorts--;
                    }
                    while (length >= PrimitiveType.BYTE.getSize() && bytes > 0) {
                        length -= PrimitiveType.BYTE.getSize();
                        bytes--;
                    }
                    while (length >= PrimitiveType.getReferenceSize() && oops > 0) {
                        length -= PrimitiveType.getReferenceSize();
                        oops--;
                    }
                }
                size += PrimitiveType.DOUBLE.getSize() * doubles;
                size += PrimitiveType.INT.getSize() * words;
                size += PrimitiveType.SHORT.getSize() * shorts;
                size += PrimitiveType.BYTE.getSize() * bytes;

                if (oops > 0) {
                    if ((size % PrimitiveType.getReferenceSize()) != 0) {
                        size += PrimitiveType.getReferenceSize() - (size % PrimitiveType.getReferenceSize());
                    }
                    size += oops * PrimitiveType.getReferenceSize();
                }

                if ((doubles + words + shorts + bytes + oops) > 0 && (size % PrimitiveType.getReferenceSize()) != 0) {
                    size += PrimitiveType.getReferenceSize() - (size % PrimitiveType.getReferenceSize());
                }
            }
            if ((size % CURRENT_JVM_INFORMATION.getObjectAlignment()) != 0) {
                size += CURRENT_JVM_INFORMATION.getObjectAlignment() - (size % CURRENT_JVM_INFORMATION.getObjectAlignment());
            }
            return Math.max(size, CURRENT_JVM_INFORMATION.getMinimumObjectSize());
        }
    }

    private long guessArraySize(Object obj) {
        long size = PrimitiveType.getArraySize();
        int length = Array.getLength(obj);
        if (length != 0) {
            Class<?> arrayElementClazz = obj.getClass().getComponentType();
            if (arrayElementClazz.isPrimitive()) {
                size += length * PrimitiveType.forType(arrayElementClazz).getSize();
            } else {
                size += length * PrimitiveType.getReferenceSize();
            }
        }
        if ((size % CURRENT_JVM_INFORMATION.getObjectAlignment()) != 0) {
            size += CURRENT_JVM_INFORMATION.getObjectAlignment() - (size % CURRENT_JVM_INFORMATION.getObjectAlignment());
        }
        return Math.max(size, CURRENT_JVM_INFORMATION.getMinimumObjectSize());
    }
}
