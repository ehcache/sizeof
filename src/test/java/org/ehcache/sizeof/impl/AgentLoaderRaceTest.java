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

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringStartsWith.startsWith;

/**
 * @author Alex Snaps
 */
public class AgentLoaderRaceTest {

    /*
     * This test tries to expose an agent loading race seen in MNK-3255.
     *
     * To trigger a failure the locking in AgentLoader.loadAgent has to be removed
     * and a sleep can be added after the check but before the load to open the
     * race wider.
     */
    @Test
    public void testAgentLoaderRace() throws InterruptedException, ExecutionException {
        final URL[] urls = ((URLClassLoader)AgentSizeOf.class.getClassLoader()).getURLs();

        Callable<Throwable> agentLoader1 = new Loader(new URLClassLoader(urls, null));
        Callable<Throwable> agentLoader2 = new Loader(new URLClassLoader(urls, null));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<Throwable>> results = executor.invokeAll(Arrays.asList(agentLoader1, agentLoader2));


        for (Future f : results) {
            Assert.assertThat(f.get(), nullValue());
        }
    }

    static class Loader implements Callable<Throwable> {

        private final ClassLoader loader;

        Loader(ClassLoader loader) {
            this.loader = loader;
        }

        public Throwable call() {
            try {
                loader.loadClass(AgentSizeOf.class.getName()).newInstance();
                return null;
            } catch (Throwable t) {
                return t;
            }
        }

    }
}
