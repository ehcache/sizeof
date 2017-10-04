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

import sun.misc.Unsafe;
import org.ehcache.sizeof.SizeOf;
import org.ehcache.sizeof.filters.SizeOfFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.ehcache.sizeof.impl.JvmInformation.CURRENT_JVM_INFORMATION;

/**
 * {@link sun.misc.Unsafe#theUnsafe} based sizeOf measurement
 * All constructors will throw UnsupportedOperationException if theUnsafe isn't accessible on this platform
 *
 * @author Chris Dennis
 */
@SuppressWarnings("restriction")
public class UnsafeSizeOf extends SizeOf {


    private static final Logger LOGGER = LoggerFactory.getLogger(UnsafeSizeOf.class);

    private static final Unsafe UNSAFE;

    static {
        Unsafe unsafe;
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe)unsafeField.get(null);
        } catch (Throwable t) {
            unsafe = null;
        }
        UNSAFE = unsafe;
    }

    /**
     * Builds a new SizeOf that will not filter fields and will cache reflected fields
     *
     * @throws UnsupportedOperationException If Unsafe isn't accessible
     * @see #UnsafeSizeOf(org.ehcache.sizeof.filters.SizeOfFilter, boolean, boolean)
     */
    public UnsafeSizeOf() throws UnsupportedOperationException {
        this(new PassThroughFilter());
    }

    /**
     * Builds a new SizeOf that will filter fields according to the provided filter and will cache reflected fields
     *
     * @param filter The filter to apply
     * @throws UnsupportedOperationException If Unsafe isn't accessible
     * @see #UnsafeSizeOf(org.ehcache.sizeof.filters.SizeOfFilter, boolean, boolean)
     * @see org.ehcache.sizeof.filters.SizeOfFilter
     */
    public UnsafeSizeOf(SizeOfFilter filter) throws UnsupportedOperationException {
        this(filter, true, true);
    }

    /**
     * Builds a new SizeOf that will filter fields according to the provided filter
     *
     * @param filter            The filter to apply
     * @param caching           whether to cache reflected fields
     * @param bypassFlyweight   whether "Flyweight Objects" are to be ignored
     * @throws UnsupportedOperationException If Unsafe isn't accessible
     * @see SizeOfFilter
     */
    public UnsafeSizeOf(SizeOfFilter filter, boolean caching, boolean bypassFlyweight) throws UnsupportedOperationException {
        super(filter, caching, bypassFlyweight);
        if (UNSAFE == null) {
            throw new UnsupportedOperationException("sun.misc.Unsafe instance not accessible");
        }

        if (!CURRENT_JVM_INFORMATION.supportsUnsafeSizeOf()) {
            LOGGER.warn("UnsafeSizeOf is not always accurate on the JVM (" + CURRENT_JVM_INFORMATION.getJvmDescription() +
                        ").  Please consider enabling AgentSizeOf.");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long sizeOf(Object obj) {
        if (obj.getClass().isArray()) {
            Class<?> klazz = obj.getClass();
            int base = UNSAFE.arrayBaseOffset(klazz);
            int scale = UNSAFE.arrayIndexScale(klazz);
            long size = base + (scale * Array.getLength(obj));
            size += CURRENT_JVM_INFORMATION.getFieldOffsetAdjustment();
            if ((size % CURRENT_JVM_INFORMATION.getObjectAlignment()) != 0) {
                size += CURRENT_JVM_INFORMATION.getObjectAlignment() - (size % CURRENT_JVM_INFORMATION.getObjectAlignment());
            }
            return Math.max(CURRENT_JVM_INFORMATION.getMinimumObjectSize(), size);
        } else {
            for (Class<?> klazz = obj.getClass(); klazz != null; klazz = klazz.getSuperclass()) {
                long lastFieldOffset = -1;
                for (Field f : klazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        lastFieldOffset = Math.max(lastFieldOffset, UNSAFE.objectFieldOffset(f));
                    }
                }
                if (lastFieldOffset > 0) {
                    lastFieldOffset += CURRENT_JVM_INFORMATION.getFieldOffsetAdjustment();
                    lastFieldOffset += 1;
                    if ((lastFieldOffset % CURRENT_JVM_INFORMATION.getObjectAlignment()) != 0) {
                        lastFieldOffset += CURRENT_JVM_INFORMATION.getObjectAlignment() -
                                           (lastFieldOffset % CURRENT_JVM_INFORMATION.getObjectAlignment());
                    }
                    return Math.max(CURRENT_JVM_INFORMATION.getMinimumObjectSize(), lastFieldOffset);
                }
            }

            long size = CURRENT_JVM_INFORMATION.getObjectHeaderSize();
            if ((size % CURRENT_JVM_INFORMATION.getObjectAlignment()) != 0) {
                size += CURRENT_JVM_INFORMATION.getObjectAlignment() - (size % CURRENT_JVM_INFORMATION.getObjectAlignment());
            }
            return Math.max(CURRENT_JVM_INFORMATION.getMinimumObjectSize(), size);
        }
    }

}
