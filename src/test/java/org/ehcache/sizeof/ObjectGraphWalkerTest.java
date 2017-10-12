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
import org.ehcache.sizeof.impl.PassThroughFilter;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Alex Snaps
 */
public class ObjectGraphWalkerTest {

    @Test
    public void testWalksAGraph() {

        final Map<String, Long> map = new HashMap<>();

        ObjectGraphWalker walker = new ObjectGraphWalker(
            new ObjectGraphWalker.Visitor() {
                public long visit(final Object object) {
                    increment(object.getClass().getName());
                    return 1;
                }

                public void increment(String value) {
                    if (value != null) {
                        Long previousValue = map.get(value);
                        if (previousValue == null) {
                            previousValue = 0L;
                        }
                        map.put(value, ++previousValue);
                    }
                }
            }, new SizeOfFilter() {

            /**
             * {@inheritDoc}
             */
            public Collection<Field> filterFields(Class<?> klazz, Collection<Field> fields) {
                return fields;
            }

            /**
             * {@inheritDoc}
             */
            public boolean filterClass(Class<?> klazz) {
                return true;
            }
        },
            true);

        assertThat(walker.walk(new ReentrantReadWriteLock()), is(5L));
        assertThat(map.remove("java.util.concurrent.locks.ReentrantReadWriteLock$Sync$ThreadLocalHoldCounter"), is(1L));

        assertThat(map.remove(ReentrantReadWriteLock.class.getName()), is(1L));
        assertThat(map.remove("java.util.concurrent.locks.ReentrantReadWriteLock$NonfairSync"), is(1L));
        assertThat(map.remove(ReentrantReadWriteLock.ReadLock.class.getName()), is(1L));
        assertThat(map.remove(ReentrantReadWriteLock.WriteLock.class.getName()), is(1L));
        assertThat(map.isEmpty(), is(true));

        assertThat(walker.walk(new SomeInnerClass()), is(14L));
        assertThat(map.remove("java.util.concurrent.locks.ReentrantReadWriteLock$Sync$ThreadLocalHoldCounter"), is(1L));

        assertThat(map.remove(SomeInnerClass.class.getName()), is(1L));
        assertThat(map.remove(this.getClass().getName()), is(1L));
        assertThat(map.remove(Object.class.getName()), is(5L));
        assertThat(map.remove(ReentrantReadWriteLock.class.getName()), is(1L));
        assertThat(map.remove("java.util.concurrent.locks.ReentrantReadWriteLock$NonfairSync"), is(1L));
        assertThat(map.remove(ReentrantReadWriteLock.ReadLock.class.getName()), is(1L));
        assertThat(map.remove(ReentrantReadWriteLock.WriteLock.class.getName()), is(1L));
        assertThat(map.remove(Object[].class.getName()), is(1L));
        // auto-boxed '0' is a flyweight - it doesn't get walked
        assertThat(map.remove(Integer.class.getName()), nullValue());
        assertThat(map.remove(int[].class.getName()), is(1L));
        assertThat(map.isEmpty(), is(true));

        assertThat(walker.walk((Object)null), is(0L));
        assertThat(walker.walk(), is(0L));
    }

    public class SomeInnerClass {

        private int value;
        private Object one;
        private final Object two = new Object();
        private final Object three = new Object();
        private final Object four = new ReentrantReadWriteLock();
        private final Object[] anArray = new Object[] { new Object(), new Object(), new Object(), one, two, two, three, four, value };
        private final int[] anIntArray = new int[] { 1, 2, 1300 };

    }

    @Test
    public void testUsingListenerToLimit() {

        final IllegalStateException illegalArgumentException = new IllegalStateException();
        final int maxDepth = 2;

        final AtomicInteger visited = new AtomicInteger();
        ObjectGraphWalker walker = new ObjectGraphWalker(object -> {
            visited.incrementAndGet();
            return -1;
        }, new PassThroughFilter(), true);

        final AtomicInteger counter = new AtomicInteger();
        try {
            walker.walk((object, size) -> {
                if (counter.incrementAndGet() >= maxDepth) {
                    throw illegalArgumentException;
                }
                assertThat(size, is(-1L));
            }, new Object(), new Object(), new Object(), new Object());
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, sameInstance(illegalArgumentException));
        }
        assertThat(visited.get(), is(maxDepth));
    }
}
