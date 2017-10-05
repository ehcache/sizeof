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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.Proxy;
import java.nio.charset.CodingErrorAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.namespace.QName;

/**
 * Enum with all the flyweight types that we check for sizeOf measurements
 *
 * @author Alex Snaps
 */
@SuppressWarnings("BoxingBoxedValue")
    // Don't be smart IDE, these _ARE_ required, we want access to the instances in the cache.
enum FlyweightType {

    /**
     * java.lang.Enum
     */
    ENUM(Enum.class) {
        @Override
        boolean isShared(final Object obj) { return true; }
    },
    /**
     * java.lang.Class
     */
    CLASS(Class.class) {
        @Override
        boolean isShared(final Object obj) { return true; }
    },
    // XXX There is no nullipotent way of determining the interned status of a string
    // There are numerous String constants within the JDK (see list at http://docs.oracle.com/javase/7/docs/api/constant-values.html),
    // but enumerating all of them would lead to lots of == tests.
    //STRING(String.class) {
    //    @Override
    //    boolean isShared(final Object obj) { return obj == ((String)obj).intern(); }
    //},
    /**
     * java.lang.Boolean
     */
    BOOLEAN(Boolean.class) {
        @Override
        boolean isShared(final Object obj) { return obj == Boolean.TRUE || obj == Boolean.FALSE; }
    },
    /**
     * java.lang.Integer
     */
    INTEGER(Integer.class) {
        @Override
        boolean isShared(final Object obj) {
            int value = (Integer)obj;
            return value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE && obj == Integer.valueOf(value);
        }
    },
    /**
     * java.lang.Short
     */
    SHORT(Short.class) {
        @Override
        boolean isShared(final Object obj) {
            short value = (Short)obj;
            return value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE && obj == Short.valueOf(value);
        }
    },
    /**
     * java.lang.Byte
     */
    BYTE(Byte.class) {
        @Override
        boolean isShared(final Object obj) { return obj == Byte.valueOf((Byte)obj); }
    },
    /**
     * java.lang.Long
     */
    LONG(Long.class) {
        @Override
        boolean isShared(final Object obj) {
            long value = (Long)obj;
            return value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE && obj == Long.valueOf(value);
        }
    },
    /**
     * java.math.BigInteger
     */
    BIGINTEGER(BigInteger.class) {
        @Override
        boolean isShared(final Object obj) {
            return obj == BigInteger.ZERO || obj == BigInteger.ONE || obj == BigInteger.TEN;
        }
    },
    /**
     * java.math.BigDecimal
     */
    BIGDECIMAL(BigDecimal.class) {
        @Override
        boolean isShared(final Object obj) {
            return obj == BigDecimal.ZERO || obj == BigDecimal.ONE || obj == BigDecimal.TEN;
        }
    },
    /**
     * java.math.MathContext
     */
    MATHCONTEXT(MathContext.class) {
        @Override
        boolean isShared(final Object obj) {
            return obj == MathContext.UNLIMITED || obj == MathContext.DECIMAL32 || obj == MathContext.DECIMAL64 || obj == MathContext.DECIMAL128;
        }
    },
    /**
     * java.lang.Character
     */
    CHARACTER(Character.class) {
        @Override
        boolean isShared(final Object obj) { return (Character)obj <= Byte.MAX_VALUE && obj == Character.valueOf((Character)obj); }
    },
    /**
     * java.lang.Locale
     */
    LOCALE(Locale.class) {
        @Override
        boolean isShared(final Object obj) {
            return obj instanceof Locale && GLOBAL_LOCALES.contains(obj);
        }
    },
    /**
     * java.util.Logger
     */
    LOGGER(Logger.class) {
        @Override
        @SuppressWarnings("deprecation")
        boolean isShared(final Object obj) { return obj == Logger.global; }
    },
    /**
     * java.net.Proxy
     */
    PROXY(Proxy.class) {
        @Override
        boolean isShared(final Object obj) { return obj == Proxy.NO_PROXY; }
    },
    /**
     * java.nio.charset.CodingErrorAction
     */
    CODINGERRORACTION(CodingErrorAction.class) {
        @Override
        boolean isShared(final Object obj) { return true; }
    },
    /**
     * javax.xml.datatype.DatatypeConstants.Field
     */
    DATATYPECONSTANTS_FIELD(DatatypeConstants.Field.class) {
        @Override
        boolean isShared(final Object obj) { return true; }
    },
    /**
     * javax.xml.namespace.QName
     */
    QNAME(QName.class) {
        @Override
        boolean isShared(final Object obj) {
            return obj == DatatypeConstants.DATETIME
                   || obj == DatatypeConstants.TIME
                   || obj == DatatypeConstants.DATE
                   || obj == DatatypeConstants.GYEARMONTH
                   || obj == DatatypeConstants.GMONTHDAY
                   || obj == DatatypeConstants.GYEAR
                   || obj == DatatypeConstants.GMONTH
                   || obj == DatatypeConstants.GDAY
                   || obj == DatatypeConstants.DURATION
                   || obj == DatatypeConstants.DURATION_DAYTIME
                   || obj == DatatypeConstants.DURATION_YEARMONTH;
        }
    },
    /**
     * misc comparisons that can not rely on the object's class.
     */
    MISC(Void.class) {
        @Override
        boolean isShared(final Object obj) {
            boolean emptyCollection = obj == Collections.EMPTY_SET || obj == Collections.EMPTY_LIST || obj == Collections.EMPTY_MAP;
            boolean systemStream = obj == System.in || obj == System.out || obj == System.err;
            return emptyCollection || systemStream || obj == String.CASE_INSENSITIVE_ORDER;
        }
    };

    private static final Map<Class<?>, FlyweightType> TYPE_MAPPINGS = new HashMap<>();

    static {
        for (FlyweightType type : FlyweightType.values()) {
            TYPE_MAPPINGS.put(type.clazz, type);
        }
    }

    private static final Set<Locale> GLOBAL_LOCALES;

    static {
        Map<Locale, Void> locales = new IdentityHashMap<>();
        for (Field f : Locale.class.getFields()) {
            int modifiers = f.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Locale.class.equals(f.getType())) {
                try {
                    locales.put((Locale)f.get(null), null);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    //
                }
            }
        }
        GLOBAL_LOCALES = locales.keySet();
    }

    private final Class<?> clazz;

    FlyweightType(final Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * Whether this is a shared object
     *
     * @param obj the object to check for
     * @return true, if shared
     */
    abstract boolean isShared(Object obj);

    /**
     * Will return the Flyweight enum instance for the flyweight Class, or null if type isn't flyweight
     *
     * @param aClazz the class we need the FlyweightType instance for
     * @return the FlyweightType, or null
     */
    static FlyweightType getFlyweightType(final Class<?> aClazz) {
        if (aClazz.isEnum() || (aClazz.getSuperclass() != null && aClazz.getSuperclass().isEnum())) {
            return ENUM;
        } else {
            FlyweightType flyweightType = TYPE_MAPPINGS.get(aClazz);
            return flyweightType != null ? flyweightType : MISC;
        }
    }
}
