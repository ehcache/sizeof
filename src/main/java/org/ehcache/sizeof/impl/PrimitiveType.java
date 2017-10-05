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

import static org.ehcache.sizeof.impl.JvmInformation.CURRENT_JVM_INFORMATION;

/**
 * Primitive types in the VM type system and their sizes
 *
 * @author Alex Snaps
 */
enum PrimitiveType {

    /**
     * boolean.class
     */
    BOOLEAN(boolean.class, 1),
    /**
     * byte.class
     */
    BYTE(byte.class, 1),
    /**
     * char.class
     */
    CHAR(char.class, 2),
    /**
     * short.class
     */
    SHORT(short.class, 2),
    /**
     * int.class
     */
    INT(int.class, 4),
    /**
     * float.class
     */
    FLOAT(float.class, 4),
    /**
     * double.class
     */
    DOUBLE(double.class, 8),
    /**
     * long.class
     */
    LONG(long.class, 8);

    private final Class<?> type;
    private final int size;


    PrimitiveType(Class<?> type, int size) {
        this.type = type;
        this.size = size;
    }

    /**
     * Returns the size in memory this type occupies
     *
     * @return size in bytes
     */
    public int getSize() {
        return size;
    }

    /**
     * The representing type
     *
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * The size of a pointer
     *
     * @return size in bytes
     */
    public static int getReferenceSize() {
        return CURRENT_JVM_INFORMATION.getJavaPointerSize();
    }

    /**
     * The size on an array
     *
     * @return size in bytes
     */
    public static long getArraySize() {
        return CURRENT_JVM_INFORMATION.getObjectHeaderSize() + INT.getSize();
    }

    /**
     * Finds the matching PrimitiveType for a type
     *
     * @param type the type to find the PrimitiveType for
     * @return the PrimitiveType instance or null if none found
     */
    public static PrimitiveType forType(final Class<?> type) {
        for (PrimitiveType primitiveType : values()) {
            if (primitiveType.getType() == type) {
                return primitiveType;
            }
        }
        return null;
    }
}
