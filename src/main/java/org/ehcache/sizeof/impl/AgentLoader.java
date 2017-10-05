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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;

/**
 * This will try to load the agent using the Attach API of JDK6.
 * If you are on an older JDK (v5) you can still use the agent by adding the -javaagent:[pathTojar] to your VM
 * startup script
 *
 * @author Alex Snaps
 */
final class AgentLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentLoader.class);

    private static final String SIZEOF_AGENT_CLASSNAME = "org.ehcache.sizeof.impl.SizeOfAgent";
    private static final String VIRTUAL_MACHINE_CLASSNAME = "com.sun.tools.attach.VirtualMachine";
    private static final Method VIRTUAL_MACHINE_ATTACH;
    private static final Method VIRTUAL_MACHINE_DETACH;
    private static final Method VIRTUAL_MACHINE_LOAD_AGENT;

    private static volatile Instrumentation instrumentation;

    static final String INSTRUMENTATION_INSTANCE_SYSTEM_PROPERTY_NAME = "org.ehcache.sizeof.agent.instrumentation";

    static {
        Method attach = null;
        Method detach = null;
        Method loadAgent = null;
        try {
            Class<?> virtualMachineClass = getVirtualMachineClass();
            attach = virtualMachineClass.getMethod("attach", String.class);
            detach = virtualMachineClass.getMethod("detach");
            loadAgent = virtualMachineClass.getMethod("loadAgent", String.class);
        } catch (Throwable e) {
            LOGGER.info("Unavailable or unrecognised attach API : {}", e.toString());
        }
        VIRTUAL_MACHINE_ATTACH = attach;
        VIRTUAL_MACHINE_DETACH = detach;
        VIRTUAL_MACHINE_LOAD_AGENT = loadAgent;
    }

    private static Class<?> getVirtualMachineClass() throws ClassNotFoundException {
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>) () -> {
                try {
                    return ClassLoader.getSystemClassLoader().loadClass(VIRTUAL_MACHINE_CLASSNAME);
                } catch (ClassNotFoundException cnfe) {
                    for (File jar : getPossibleToolsJars()) {
                        try {
                            Class<?> vmClass = new URLClassLoader(new URL[] { jar.toURI().toURL() }).loadClass(VIRTUAL_MACHINE_CLASSNAME);
                            LOGGER.info("Located valid 'tools.jar' at '{}'", jar);
                            return vmClass;
                        } catch (Throwable t) {
                            LOGGER.info("Exception while loading tools.jar from '{}': {}", jar, t);
                        }
                    }
                    throw new ClassNotFoundException(VIRTUAL_MACHINE_CLASSNAME);
                }
            });
        } catch (PrivilegedActionException pae) {
            Throwable actual = pae.getCause();
            if (actual instanceof ClassNotFoundException) {
                throw (ClassNotFoundException)actual;
            }
            throw new AssertionError("Unexpected checked exception : " + actual);
        }
    }

    private static List<File> getPossibleToolsJars() {
        List<File> jars = new ArrayList<>();

        File javaHome = new File(System.getProperty("java.home"));
        File jreSourced = new File(javaHome, "lib/tools.jar");
        if (jreSourced.exists()) {
            jars.add(jreSourced);
        }
        if ("jre".equals(javaHome.getName())) {
            File jdkHome = new File(javaHome, "../");
            File jdkSourced = new File(jdkHome, "lib/tools.jar");
            if (jdkSourced.exists()) {
                jars.add(jdkSourced);
            }
        }
        return jars;
    }

    /**
     * Attempts to load the agent through the Attach API
     *
     * @return true if agent was loaded (which could have happened thought the -javaagent switch)
     */
    static boolean loadAgent() {
        synchronized (AgentLoader.class.getName().intern()) {
            if (!agentIsAvailable() && VIRTUAL_MACHINE_LOAD_AGENT != null) {
                try {
                    warnIfOSX();
                    String name = ManagementFactory.getRuntimeMXBean().getName();
                    Object vm = VIRTUAL_MACHINE_ATTACH.invoke(null, name.substring(0, name.indexOf('@')));
                    try {
                        File agent = getAgentFile();
                        LOGGER.info("Trying to load agent @ {}", agent);
                        if (agent != null) {
                            VIRTUAL_MACHINE_LOAD_AGENT.invoke(vm, agent.getAbsolutePath());
                        }
                    } finally {
                        VIRTUAL_MACHINE_DETACH.invoke(vm);
                    }
                } catch (InvocationTargetException ite) {
                    Throwable cause = ite.getCause();
                    LOGGER.info("Failed to attach to VM and load the agent: {}: {}", cause.getClass(), cause.getMessage());
                } catch (Throwable t) {
                    LOGGER.info("Failed to attach to VM and load the agent: {}: {}", t.getClass(), t.getMessage());
                }
            }
            final boolean b = agentIsAvailable();
            if (b) {
                LOGGER.info("Agent successfully loaded and available!");
            }

            return b;
        }
    }

    private static void warnIfOSX() {
        if (JvmInformation.isOSX() && System.getProperty("java.io.tmpdir") != null) {
            LOGGER.warn("Loading the SizeOfAgent will probably fail, as you are running on Apple OS X and have a value set for java.io.tmpdir\n" +
                        "They both result in a bug, not yet fixed by Apple, that won't let us attach to the VM and load the agent.\n" +
                        "Most probably, you'll also get a full thread-dump after this because of the failure... Nothing to worry about!\n" +
                        "You can bypass trying to load the Agent entirely by setting the System property '"
                        + AgentSizeOf.BYPASS_LOADING + "'  to true");
        }
    }

    private static File getAgentFile() throws IOException {
        URL agent = AgentLoader.class.getResource("sizeof-agent.jar");
        if (agent == null) {
            return null;
        } else if (agent.getProtocol().equals("file")) {
            return new File(agent.getFile());
        } else {
            File temp = File.createTempFile("ehcache-sizeof-agent", ".jar");
            try (FileOutputStream fout = new FileOutputStream(temp); InputStream in = agent.openStream()) {
                byte[] buffer = new byte[1024];
                while (true) {
                    int read = in.read(buffer);
                    if (read < 0) {
                        break;
                    } else {
                        fout.write(buffer, 0, read);
                    }
                }
            } finally {
                temp.deleteOnExit();
            }
            LOGGER.info("Extracted agent jar to temporary file {}", temp);
            return temp;
        }
    }

    /**
     * Checks whether the agent is available
     *
     * @return true if available
     */
    static boolean agentIsAvailable() {
        try {
            if (instrumentation == null) {
                instrumentation = (Instrumentation)System.getProperties().get(INSTRUMENTATION_INSTANCE_SYSTEM_PROPERTY_NAME);
            }
            if (instrumentation == null) {
                Class<?> sizeOfAgentClass = ClassLoader.getSystemClassLoader().loadClass(SIZEOF_AGENT_CLASSNAME);
                Method getInstrumentationMethod = sizeOfAgentClass.getMethod("getInstrumentation");
                instrumentation = (Instrumentation)getInstrumentationMethod.invoke(sizeOfAgentClass);
            }
            return instrumentation != null;
        } catch (SecurityException e) {
            LOGGER.warn("Couldn't access the system classloader because of the security policies applied by " +
                        "the security manager. You either want to loosen these, so ClassLoader.getSystemClassLoader() and " +
                        "reflection API calls are permitted or the sizing will be done using some other mechanism.\n" +
                        "Alternatively, set the system property org.ehcache.sizeof.agent.instrumentationSystemProperty to true " +
                        "to have the agent put the required instances in the System Properties for the loader to access.");
            return false;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Returns the size of this Java object as calculated by the loaded agent.
     *
     * @param obj object to be sized
     * @return size of the object in bytes
     */
    static long agentSizeOf(Object obj) {
        if (instrumentation == null) {
            throw new UnsupportedOperationException("Sizeof agent is not available");
        }
        return instrumentation.getObjectSize(obj);
    }
}
