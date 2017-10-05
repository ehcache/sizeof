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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Alex Snaps
 */
public class AnnotationProxyFactoryTest {

    @Test(expected = UnsupportedOperationException.class)
    public void NoDefaultValueInReferenceAnnotationAndNoImplementationInCustomThrowsException_Test() {
        CustomAnnotation customAnnotation = UsingCustomAnnotation.class.getAnnotation(CustomAnnotation.class);
        ReferenceAnnotation annotationProxy = AnnotationProxyFactory
            .getAnnotationProxy(customAnnotation, ReferenceAnnotation.class);
        annotationProxy.things();
    }

    @Test
    public void NoImplementationInCustomFallsBackToReferenceImplementation_Test() {
        CustomAnnotation customAnnotation = UsingCustomAnnotation.class.getAnnotation(CustomAnnotation.class);
        ReferenceAnnotation annotationProxy = AnnotationProxyFactory
            .getAnnotationProxy(customAnnotation, ReferenceAnnotation.class);
        assertEquals("hello", annotationProxy.version());
    }


    @Test
    public void IfMethodExistsInCustomAnnotationRedirectsToIt_Test() {
        CustomAnnotation customAnnotation = UsingCustomAnnotation.class.getAnnotation(CustomAnnotation.class);
        ReferenceAnnotation annotationProxy = AnnotationProxyFactory
            .getAnnotationProxy(customAnnotation, ReferenceAnnotation.class);
        assertEquals(true, annotationProxy.deprecated());
    }

    @Test
    public void IfMethodExistsInCustomAnnotationButReturnTypeIsDifferentFallsBackToReferenceImplementation_Test() {
        CustomAnnotation customAnnotation = UsingCustomAnnotation.class.getAnnotation(CustomAnnotation.class);
        ReferenceAnnotation annotationProxy = AnnotationProxyFactory
            .getAnnotationProxy(customAnnotation, ReferenceAnnotation.class);
        assertEquals(5, annotationProxy.differentReturnType());
    }

    @Test
    public void DifferentReturnTypesFromDifferentMethods_Test() {
        CustomAnnotation customAnnotation = UsingCustomAnnotation.class.getAnnotation(CustomAnnotation.class);
        ReferenceAnnotation annotationProxy = org
            .ehcache
            .sizeof
            .annotations
            .AnnotationProxyFactory
            .getAnnotationProxy(customAnnotation, ReferenceAnnotation.class);
        assertEquals(Integer.class, annotationProxy.aClass());
        assertEquals(ExampleEnum.TWO, annotationProxy.anEnum());
        assertEquals(customAnnotation.anAnnotation(), annotationProxy.anAnnotation());
    }


    @CustomAnnotation
    class UsingCustomAnnotation {
    }
}
