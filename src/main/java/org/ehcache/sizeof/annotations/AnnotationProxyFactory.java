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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Allows you to transform the type of your custom annotation to a reference annotation type.
 * It can come handy when you want to allow the consumers of your library not to depend on your API because of the annotations, still allowing them to use the original annotation methods.
 * <p/>
 * Example :
 * <p/>
 * //getting a custom annotation from a class
 * my.Annotation customAnnotation = klazz.getAnnotation(my.Annotation.class);
 * //if this annotation is "similar" (duck-typing, same methods) to the reference one, I can get a proxy to it, whose type is the reference annotation
 * ehcache.Annotation annotation = AnnotationProxyFactory.getAnnotationProxy(customAnnotation, ehcache.Annotation.class);
 * <p/>
 * //so my library can apply the behavior when the default annotation is used
 *
 * @author Anthony Dahanne
 * @ehcache.Annotation(action="true") public class UserClass {}
 * <p/>
 * //or when a custom one is used, since all calls to action() will be caught and redirected to the custom annotation action method, if it exists,
 * //or fall back to the reference action method
 * @my.Annotation(action="true") public class UserClass {}
 */
public final class AnnotationProxyFactory {


    private AnnotationProxyFactory() {
        //not to instantiate
    }

    /**
     * Returns a proxy on the customAnnotation, having the same type than the referenceAnnotation
     *
     * @param customAnnotation annotation proxied
     * @param referenceAnnotation type of the returned annotation
     * @return proxied customAnnotation with the type of referenceAnnotation
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotationProxy(Annotation customAnnotation, Class<T> referenceAnnotation) {
        InvocationHandler handler = new AnnotationInvocationHandler(customAnnotation);
        return (T)Proxy.newProxyInstance(referenceAnnotation.getClassLoader(), new Class[] { referenceAnnotation }, handler);
    }

    /**
     * Invocation handler implementing an invoke method that redirects every method call to the custom annotation method
     * when possible; if not returns the reference annotation method default value
     */
    private static class AnnotationInvocationHandler implements InvocationHandler {

        private final Annotation customAnnotation;

        public AnnotationInvocationHandler(Annotation customAnnotation) {
            this.customAnnotation = customAnnotation;
        }

        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            //trying to call the method on the custom annotation, if it exists
            Method methodOnCustom = getMatchingMethodOnGivenAnnotation(method);
            if (methodOnCustom != null) {
                return methodOnCustom.invoke(customAnnotation, args);
            } else {
                //otherwise getting the default value of the reference annotation method
                Object defaultValue = method.getDefaultValue();
                if (defaultValue != null) {
                    return defaultValue;
                }
                throw new UnsupportedOperationException(
                    "The method \""
                    + method.getName()
                    + "\" does not exist in the custom annotation, and there is no default value for"
                    + " it in the reference annotation, please implement this method in your custom annotation.");
            }
        }

        private Method getMatchingMethodOnGivenAnnotation(Method method) {
            try {
                Method customMethod = customAnnotation.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
                if (customMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
                    return customMethod;
                }
                return null;
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
    }

}
