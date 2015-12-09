/**
 *  Copyright Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ehcache.sizeof;


import java.util.ServiceLoader;

/**
 * @author Alex Snaps
 */
public final class SizeOfEngineLoader implements SizeOfEngineFactory {

    /**
     * The one and single instance of this class
     */
    public static final SizeOfEngineLoader INSTANCE = new SizeOfEngineLoader();

    private final ServiceLoader<SizeOfEngineFactory> loader;
    private volatile SizeOfEngineFactory factory;

    /**
     * Only there for testing purposes
     * @param classLoader the classLoader the ServiceLoader will use to resolve net.sf.ehcache.pool.SizeOfEngineFactory services
     *                    Should probably always be SizeOfEngineLoader.class.getClassLoader()
     */
    SizeOfEngineLoader(ClassLoader classLoader) {
        loader = ServiceLoader.load(SizeOfEngineFactory.class, classLoader);
        load(SizeOfEngineFactory.class, false);
    }

    private SizeOfEngineLoader() {
        this(SizeOfEngineLoader.class.getClassLoader());
    }

    /**
     * Creates a new instance of a SizeOfEngine
     *
     * @param maxObjectCount the max object graph that will be traversed.
     * @param abort          true if the object traversal should be aborted when the max depth is exceeded
     * @param silent         true if no info log explaining which agent was chosen should be printed
     * @return the new instance
     */
    public static SizeOfEngine newSizeOfEngine(final int maxObjectCount, final boolean abort, final boolean silent) {
        return INSTANCE.createSizeOfEngine(maxObjectCount, abort, silent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SizeOfEngine createSizeOfEngine(final int maxObjectCount, final boolean abort, final boolean silent) {
        SizeOfEngineFactory currentFactory = this.factory;
        if (currentFactory != null) {
            return currentFactory.createSizeOfEngine(maxObjectCount, abort, silent);
        }
        return null;
    }

    /**
     * Reloads the factory using the ServiceLoader
     */
    public void reload() {
        load(SizeOfEngineFactory.class, true);
    }

    /**
     * Tries to find a SizeOfEngineFactory instance that is assignable from clazz
     *
     * @param clazz  the class
     * @param reload whether to force a reload of the ServiceLoader
     * @return true if succeeded otherwise, false
     */
    public synchronized boolean load(Class<? extends SizeOfEngineFactory> clazz, boolean reload) {
        if (reload) {
            loader.reload();
        }
        for (SizeOfEngineFactory sizeOfEngineFactory : loader) {
            if (clazz.isAssignableFrom(sizeOfEngineFactory.getClass())) {
                factory = sizeOfEngineFactory;
                return true;
            }
        }
        factory = null;
        return false;
    }
}
