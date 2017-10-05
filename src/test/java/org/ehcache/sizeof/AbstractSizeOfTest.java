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

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import static org.ehcache.sizeof.impl.JvmInformation.CURRENT_JVM_INFORMATION;

/**
 * @author Alex Snaps
 */
abstract class AbstractSizeOfTest {

    protected static final boolean COMPRESSED_OOPS;
    protected static final boolean HOTSPOT_CMS;
    protected static final boolean IS_HOTSPOT;
    protected static final boolean IS_JROCKIT;
    protected static final boolean IS_IBM;
    protected static final boolean IS_64_BIT;

    static {
        String value = getVmOptionValue("UseCompressedOops");
        if (value == null) {
            System.err.println("Could not detect compressed-oops status assuming: false");
            COMPRESSED_OOPS = false;
        } else {
            COMPRESSED_OOPS = Boolean.valueOf(value);
        }

        HOTSPOT_CMS = CURRENT_JVM_INFORMATION.getMinimumObjectSize() > CURRENT_JVM_INFORMATION.getObjectAlignment();

        IS_64_BIT = System.getProperty("sun.arch.data.model").equals("64");

        IS_HOTSPOT = System.getProperty("java.vm.name", "").toLowerCase().contains("hotspot");

        IS_JROCKIT = System.getProperty("jrockit.version") != null ||
                     System.getProperty("java.vm.name", "").toLowerCase().contains("jrockit");

        IS_IBM = System.getProperty("java.vm.name", "").contains("IBM") &&
                 System.getProperty("java.vm.vendor").contains("IBM");
    }

    private static String getVmOptionValue(String name) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Object vmOption = server.invoke(ObjectName.getInstance("com.sun.management:type=HotSpotDiagnostic"), "getVMOption", new Object[] { name }, new String[] { "java.lang.String" });
            return (String)((CompositeData)vmOption).get("value");
        } catch (Throwable t) {
            return null;
        }
    }
}
