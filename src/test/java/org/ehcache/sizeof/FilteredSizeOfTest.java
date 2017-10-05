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

import org.ehcache.sizeof.annotations.IgnoreSizeOf;
import org.ehcache.sizeof.filteredtest.AnnotationFilteredPackage;
import org.ehcache.sizeof.filteredtest.custom.CustomAnnotationFilteredPackage;
import org.ehcache.sizeof.filters.AnnotationSizeOfFilter;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Alex Snaps
 */
public class FilteredSizeOfTest extends AbstractSizeOfTest {
    private static long deepSizeOf(SizeOf sizeOf, Object... obj) {
        return sizeOf.deepSizeOf(obj);
    }

    @BeforeClass
    public static void setup() {
        deepSizeOf(new CrossCheckingSizeOf(), new Object());
        System.out.println("Testing for a " + System.getProperty("java.version") + " JDK "
                           + ") on a " + System.getProperty("sun.arch.data.model") + "-bit VM "
                           + "(compressed-oops: " + COMPRESSED_OOPS
                           + ", Hotspot CMS: " + HOTSPOT_CMS
                           + ")");
    }

    @Test
    public void testAnnotationFiltering() throws Exception {
        SizeOf sizeOf = new CrossCheckingSizeOf(new AnnotationSizeOfFilter());

        assertThat(deepSizeOf(sizeOf, new AnnotationFilteredField()), allOf(greaterThan(128L), lessThan(16 * 1024L)));
        assertThat(deepSizeOf(sizeOf, new AnnotationFilteredClass()), equalTo(0L));
        assertThat(deepSizeOf(sizeOf, new AnnotationFilteredPackage()), equalTo(0L));

        assertThat(deepSizeOf(sizeOf, new AnnotationFilteredFieldSubclass()), allOf(greaterThan(128L), lessThan(16 * 1024L)));

        long emptyReferrerSize = deepSizeOf(sizeOf, new Referrer(null));
        assertThat(deepSizeOf(sizeOf, new Referrer(new AnnotationFilteredClass())), equalTo(emptyReferrerSize));
        assertThat(deepSizeOf(sizeOf, new Referrer(new AnnotationFilteredPackage())), equalTo(emptyReferrerSize));
        assertThat(deepSizeOf(sizeOf, new Parent()), equalTo(0L));
        assertThat(deepSizeOf(sizeOf, new Child()), equalTo(0L));
        assertThat(deepSizeOf(sizeOf, new ChildChild()), equalTo(0L));
        assertThat(deepSizeOf(sizeOf, new ChildChildChild()), equalTo(0L));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCustomAnnotationFiltering() throws Exception {
        SizeOf sizeOf = new CrossCheckingSizeOf(new AnnotationSizeOfFilter());
        assertThat(deepSizeOf(sizeOf, new MatchingPatternOrNotAnnotationFilteredField()), allOf(greaterThan(128L), lessThan(16 * 1024L)));
        assertThat(deepSizeOf(sizeOf, new MatchingPatternAnnotation()), equalTo(0L));
        assertThat(deepSizeOf(sizeOf, new MatchingPatternAnnotationChild()), equalTo(0L));
        assertThat(deepSizeOf(sizeOf, new MatchingPatternAnnotationNoInheritedChild()), allOf(greaterThan(4L)));
        assertThat(deepSizeOf(sizeOf, new NonMatchingPatternAnnotation1()), allOf(greaterThan(4L)));
        assertThat(deepSizeOf(sizeOf, new NonMatchingPatternAnnotation2()), allOf(greaterThan(4L)));
        assertThat(deepSizeOf(sizeOf, new CustomAnnotationFilteredPackage()), equalTo(0L));
    }

    @Test(expected = IllegalStateException.class)
    public void testNotPossibleToHaveTwoIgnoreSizeOfAnnotations() throws Exception {
        SizeOf sizeOf = new CrossCheckingSizeOf(new AnnotationSizeOfFilter());
        deepSizeOf(sizeOf, new AnnotatedTwice());
    }


    public static class AnnotationFilteredField {

        @IgnoreSizeOf
        private final byte[] bigArray = new byte[16 * 1024];
        private final byte[] smallArray = new byte[128];
    }

    public static class AnnotationFilteredFieldSubclass extends AnnotationFilteredField {
    }

    @IgnoreSizeOf
    public static class AnnotationFilteredClass {

        private final byte[] bigArray = new byte[16 * 1024];
    }

    @IgnoreSizeOf(inherited = true)
    public static class Parent {
    }

    public static class Child extends Parent {
    }

    @IgnoreSizeOf
    public static class ChildChild extends Child {
    }

    public static class ChildChildChild extends ChildChild {
    }

    @com.terracotta.ehcache.special.annotation.IgnoreSizeOf
    public static class MatchingPatternAnnotation {
    }

    public static class MatchingPatternAnnotationChild extends MatchingPatternAnnotation {
    }

    @com.terracotta.ehcache.special.annotation.no.inherited.IgnoreSizeOf
    public static class MatchingPatternAnnotationNoInherited {
    }

    public static class MatchingPatternAnnotationNoInheritedChild extends MatchingPatternAnnotationNoInherited {
    }

    @com.terracotta.ehcache.special.annotation.IgnoreSizeOffff
    public static class NonMatchingPatternAnnotation1 {
    }

    @com.terracotta.special.annotation.IgnoreSizeOf
    public static class NonMatchingPatternAnnotation2 {
    }

    @com.terracotta.ehcache.special.annotation.IgnoreSizeOf
    @IgnoreSizeOf
    public static class AnnotatedTwice {
    }

    public static class MatchingPatternOrNotAnnotationFilteredField {
        @com.terracotta.ehcache.special.annotation.IgnoreSizeOf
        private final byte[] matchingBigArray = new byte[16 * 1024];
        @com.terracotta.special.annotation.IgnoreSizeOf
        private final byte[] nonMatchingSmallArray = new byte[128];
    }

    public static class ResourceFilteredField {

        private final byte[] bigArray = new byte[16 * 1024];
        private final byte[] smallArray = new byte[128];
    }

    public static class ResourceFilteredFieldSubclass extends ResourceFilteredField {
    }

    public static class ResourceFilteredClass {

        private final byte[] bigArray = new byte[6 * 1024];
    }

    public static class Referrer {

        private final Object reference;

        public Referrer(Object obj) {
            this.reference = obj;
        }
    }
}
