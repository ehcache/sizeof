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
package org.ehcache.sizeof.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to ignore a field, type or entire package while doing a SizeOf measurement
 *
 * @author Chris Dennis
 * @see org.ehcache.sizeof.SizeOf
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE, ElementType.PACKAGE })
public @interface IgnoreSizeOf {

    /**
     * Controls whether the annotation, when applied to a {@link ElementType#TYPE type} is to be applied to all its subclasses
     * as well or solely on that type only. true if inherited by subtypes, false otherwise
     */
    boolean inherited() default false;
}
