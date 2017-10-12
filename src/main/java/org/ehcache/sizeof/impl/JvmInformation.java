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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

/**
 * Detects and represents JVM-specific properties that relate to the memory
 * data model for java objects that are useful for size of calculations.
 *
 * @author jhouse
 * @author Chris Dennis
 */
public enum JvmInformation {

    /**
     * Represents Generic 32-bit
     */
    UNKNOWN_32_BIT(null)  {

        /* default values are for this vm */

        @Override
        public String getJvmDescription() {
            return "Unrecognized 32-Bit JVM";
        }

        @Override
        public int getPointerSize() {
            return 4;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }

        @Override
        public int getObjectAlignment() {
            return 8;
        }

        @Override
        public int getFieldOffsetAdjustment() {
            return 0;
        }

        @Override
        public int getAgentSizeOfAdjustment() {
            return 0;
        }

        @Override
        public boolean supportsAgentSizeOf() {
            return true;
        }

        @Override
        public boolean supportsUnsafeSizeOf() {
            return true;
        }

        @Override
        public boolean supportsReflectionSizeOf() {
            return true;
        }
    },

    /**
     * Represents 64-Bit Generic JVM
     */
    UNKNOWN_64_BIT(UNKNOWN_32_BIT) {

        @Override
        public int getPointerSize() {
            return 8;
        }

        @Override
        public int getJavaPointerSize() {
            return 8;
        }

        @Override
        public String getJvmDescription() {
            return "Unrecognized 64-Bit JVM";
        }
    },

    /**
     * Represents HotSpot 32-bit
     */
    HOTSPOT_32_BIT(UNKNOWN_32_BIT)  {

        /* default values are for this vm */

        @Override
        public String getJvmDescription() {
            return "32-Bit HotSpot JVM";
        }
    },

    /**
     * Represents 32-Bit HotSpot JVM with Concurrent Mark-and-Sweep GC
     */
    HOTSPOT_32_BIT_WITH_CONCURRENT_MARK_AND_SWEEP(UNKNOWN_32_BIT) {

        @Override
        public int getMinimumObjectSize() {
            return 16;
        }

        @Override
        public String getJvmDescription() {
            return "32-Bit HotSpot JVM with Concurrent Mark-and-Sweep GC";
        }
    },

    /**
     * Represents 64-Bit HotSpot JVM
     */
    HOTSPOT_64_BIT(UNKNOWN_64_BIT) {

        @Override
        public String getJvmDescription() {
            return "64-Bit HotSpot JVM";
        }
    },

    /**
     * Represents 64-Bit HotSpot JVM with Concurrent Mark-and-Sweep GC
     */
    HOTSPOT_64_BIT_WITH_CONCURRENT_MARK_AND_SWEEP(HOTSPOT_64_BIT) {

        @Override
        public int getMinimumObjectSize() {
            return 24;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit HotSpot JVM with Concurrent Mark-and-Sweep GC";
        }
    },

    /**
     * Represents 64-Bit HotSpot JVM with Compressed OOPs
     */
    HOTSPOT_64_BIT_WITH_COMPRESSED_OOPS(HOTSPOT_64_BIT) {

        @Override
        public int getJavaPointerSize() {
            return 4;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit HotSpot JVM with Compressed OOPs";
        }
    },

    /**
     * Represents 64-Bit HotSpot JVM with Compressed OOPs and Concurrent Mark-and-Sweep GC
     */
    HOTSPOT_64_BIT_WITH_COMPRESSED_OOPS_AND_CONCURRENT_MARK_AND_SWEEP(HOTSPOT_64_BIT_WITH_COMPRESSED_OOPS) {

        @Override
        public int getMinimumObjectSize() {
            return 24;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit HotSpot JVM with Compressed OOPs and Concurrent Mark-and-Sweep GC";
        }
    },

    /**
     * Represents OpenJDK 32-bit
     */
    OPENJDK_32_BIT(HOTSPOT_32_BIT)  {

        @Override
        public String getJvmDescription() {
            return "32-Bit OpenJDK JVM";
        }
    },

    /**
     * Represents 32-Bit OpenJDK JVM with Concurrent Mark-and-Sweep GC
     */
    OPENJDK_32_BIT_WITH_CONCURRENT_MARK_AND_SWEEP(HOTSPOT_32_BIT) {
        @Override
        public int getMinimumObjectSize() {
            return 16;
        }

        @Override
        public String getJvmDescription() {
            return "32-Bit OpenJDK JVM with Concurrent Mark-and-Sweep GC";
        }
    },

    /**
     * Represents 64-Bit OpenJDK JVM
     */
    OPENJDK_64_BIT(HOTSPOT_64_BIT) {

        @Override
        public String getJvmDescription() {
            return "64-Bit OpenJDK JVM";
        }
    },

    /**
     * Represents 64-Bit OpenJDK JVM with Concurrent Mark-and-Sweep GC
     */
    OPENJDK_64_BIT_WITH_CONCURRENT_MARK_AND_SWEEP(OPENJDK_64_BIT) {

        @Override
        public int getMinimumObjectSize() {
            return 24;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit OpenJDK JVM with Concurrent Mark-and-Sweep GC";
        }
    },

    /**
     * Represents 64-Bit OpenJDK JVM with Compressed OOPs
     */
    OPENJDK_64_BIT_WITH_COMPRESSED_OOPS(OPENJDK_64_BIT) {

        @Override
        public int getJavaPointerSize() {
            return 4;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit OpenJDK JVM with Compressed OOPs";
        }
    },

    /**
     * Represents 64-Bit OpenJDK JVM with Compressed OOPs and Concurrent Mark-and-Sweep GC
     */
    OPENJDK_64_BIT_WITH_COMPRESSED_OOPS_AND_CONCURRENT_MARK_AND_SWEEP(OPENJDK_64_BIT_WITH_COMPRESSED_OOPS) {

        @Override
        public int getMinimumObjectSize() {
            return 24;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit OpenJDK JVM with Compressed OOPs and Concurrent Mark-and-Sweep GC";
        }
    },

    /**
     * Represents IBM 32-bit
     */
    IBM_32_BIT(UNKNOWN_32_BIT) {

        @Override
        public String getJvmDescription() {
            return "IBM 32-Bit JVM";
        }

        @Override
        public int getObjectHeaderSize() {
            return 16;
        }

        @Override
        public boolean supportsReflectionSizeOf() {
            return false;
        }
    },

    /**
     * Represents 64-Bit IBM JVM (with no reference compression)
     */
    IBM_64_BIT(UNKNOWN_64_BIT) {

        @Override
        public int getObjectHeaderSize() {
            return 24;
        }

        @Override
        public boolean supportsReflectionSizeOf() {
            return false;
        }

        @Override
        public String getJvmDescription() {
            return "IBM 64-Bit JVM (with no reference compression)";
        }
    },

    /**
     * Represents 64-Bit IBM JVM (with reference compression)
     */
    IBM_64_BIT_WITH_COMPRESSED_REFS(IBM_32_BIT) {

        @Override
        public int getObjectHeaderSize() {
            return 16;
        }

        @Override
        public String getJvmDescription() {
            return "IBM 64-Bit JVM with Compressed References";
        }
    };


    /**
     * The JvmInformation instance representing the current JVM
     */
    public static final JvmInformation CURRENT_JVM_INFORMATION;

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmInformation.class);

    private static final long TWENTY_FIVE_GB = 25L * 1024L * 1024L * 1024L;
    private static final long FIFTY_SEVEN_GB = 57L * 1024L * 1024L * 1024L;


    static {
        CURRENT_JVM_INFORMATION = getJvmInformation();
        LOGGER.info("Detected JVM data model settings of: " + CURRENT_JVM_INFORMATION.getJvmDescription());
    }

    private JvmInformation parent;

    JvmInformation(JvmInformation parent) {
        this.parent = parent;
    }

    /**
     * Size of a pointer in bytes on this runtime
     */
    public int getPointerSize() {
        return parent.getPointerSize();
    }

    /**
     * Size of a java pointer in bytes on this runtime (that differs when compressedOops are being used)
     */
    public int getJavaPointerSize() {
        return parent.getJavaPointerSize();
    }

    /**
     * Minimal size an object will occupy on the heap in bytes.
     */
    public int getMinimumObjectSize() {
        return getObjectAlignment();
    }

    /**
     * Object alignment / padding in bytes
     */
    public int getObjectAlignment() {
        return parent.getObjectAlignment();
    }

    /**
     * The size of an object header in bytes.
     */
    public int getObjectHeaderSize() {
        return getPointerSize() + getJavaPointerSize();
    }

    /**
     * The size of the jvm-specific field offset adjustment in bytes.
     */
    public int getFieldOffsetAdjustment() {
        return parent.getFieldOffsetAdjustment();
    }

    /**
     * The size of the jvm-specific agent result adjustment in bytes.
     */
    public int getAgentSizeOfAdjustment() {
        return parent.getAgentSizeOfAdjustment();
    }

    /**
     * Whether the jvm can support AgentSizeOf implementation.
     */
    public boolean supportsAgentSizeOf() {
        return parent.supportsAgentSizeOf();
    }

    /**
     * Whether the jvm can support UnsafeSizeOf implementation.
     */
    public boolean supportsUnsafeSizeOf() {
        return parent.supportsUnsafeSizeOf();
    }

    /**
     * Whether the jvm can support ReflectionSizeOf implementation.
     */
    public boolean supportsReflectionSizeOf() {
        return parent.supportsReflectionSizeOf();
    }

    /**
     * A human-readable description of the JVM and its relevant enabled options.Os
     */
    public abstract String getJvmDescription();

    /**
     * Determine the JvmInformation for the current JVM.
     */
    private static JvmInformation getJvmInformation() {
        JvmInformation jif = detectHotSpot();

        if (jif == null) {
            jif = detectOpenJDK();
        }

        if (jif == null) {
            jif = detectIBM();
        }

        if (jif == null && is64Bit()) {
            // unknown 64-bit JVMs
            jif = UNKNOWN_64_BIT;
        } else if (jif == null) {
            // unknown 32-bit JVMs
            jif = UNKNOWN_32_BIT;
        }

        return jif;
    }

    private static JvmInformation detectHotSpot() {
        JvmInformation jif = null;

        if (isHotspot()) {
            if (is64Bit()) {
                if (isHotspotCompressedOops() && isHotspotConcurrentMarkSweepGC()) {
                    jif = HOTSPOT_64_BIT_WITH_COMPRESSED_OOPS_AND_CONCURRENT_MARK_AND_SWEEP;
                } else if (isHotspotCompressedOops()) {
                    jif = HOTSPOT_64_BIT_WITH_COMPRESSED_OOPS;
                } else if (isHotspotConcurrentMarkSweepGC()) {
                    jif = HOTSPOT_64_BIT_WITH_CONCURRENT_MARK_AND_SWEEP;
                } else {
                    jif = HOTSPOT_64_BIT;
                }
            } else {
                if (isHotspotConcurrentMarkSweepGC()) {
                    jif = HOTSPOT_32_BIT_WITH_CONCURRENT_MARK_AND_SWEEP;
                } else {
                    jif = HOTSPOT_32_BIT;
                }
            }
        }

        return jif;
    }

    private static JvmInformation detectOpenJDK() {
        JvmInformation jif = null;

        if (isOpenJDK()) {
            if (is64Bit()) {
                if (isHotspotCompressedOops() && isHotspotConcurrentMarkSweepGC()) {
                    jif = OPENJDK_64_BIT_WITH_COMPRESSED_OOPS_AND_CONCURRENT_MARK_AND_SWEEP;
                } else if (isHotspotCompressedOops()) {
                    jif = OPENJDK_64_BIT_WITH_COMPRESSED_OOPS;
                } else if (isHotspotConcurrentMarkSweepGC()) {
                    jif = OPENJDK_64_BIT_WITH_CONCURRENT_MARK_AND_SWEEP;
                } else {
                    jif = OPENJDK_64_BIT;
                }
            } else {
                if (isHotspotConcurrentMarkSweepGC()) {
                    jif = OPENJDK_32_BIT_WITH_CONCURRENT_MARK_AND_SWEEP;
                } else {
                    jif = OPENJDK_32_BIT;
                }
            }
        }

        return jif;
    }

    private static JvmInformation detectIBM() {
        JvmInformation jif = null;

        if (isIBM()) {
            if (is64Bit()) {
                if (isIBMCompressedRefs()) {
                    jif = IBM_64_BIT_WITH_COMPRESSED_REFS;
                } else {
                    jif = IBM_64_BIT;
                }
            } else {
                jif = IBM_32_BIT;
            }
        }

        return jif;
    }

    private static boolean isJRockit64GBCompression() {
        if (getJRockitVmArgs().contains("-XXcompressedRefs:enable=false")) {
            return false;
        }
        if (getJRockitVmArgs().contains("-XXcompressedRefs:size=4GB") ||
            getJRockitVmArgs().contains("-XXcompressedRefs:size=32GB")) {
            return false;
        }

        if (getJRockitVmArgs().contains("-XXcompressedRefs:size=64GB")) {
            return true;
        }
        long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory > TWENTY_FIVE_GB && maxMemory <= FIFTY_SEVEN_GB
            && getJRockitVmArgs().contains("-XXcompressedRefs:enable=true")) {
            return true;
        }

        return false;
    }

    /**
     * Returns true if VM is JRockit
     *
     * @return true, if JRockit
     */
    public static boolean isJRockit() {
        return System.getProperty("jrockit.version") != null
               || System.getProperty("java.vm.name", "").toLowerCase().contains("jrockit");
    }

    /**
     * Return true if the VM's vendor is Apple
     *
     * @return true, if OS X
     */
    public static boolean isOSX() {
        final String vendor = System.getProperty("java.vm.vendor");
        return vendor != null && vendor.startsWith("Apple");
    }

    /**
     * Returns true if VM vendor is Hotspot
     *
     * @return true, if Hotspot
     */
    public static boolean isHotspot() {
        return System.getProperty("java.vm.name", "").toLowerCase().contains("hotspot");
    }

    /**
     * Returns true if VM vendor is OpenJDK
     *
     * @return true, if OpenJDK
     */
    public static boolean isOpenJDK() {
        return System.getProperty("java.vm.name", "").toLowerCase().contains("openjdk");
    }

    /**
     * Returns true if VM vendor is IBM
     *
     * @return true, if IBM
     */
    public static boolean isIBM() {
        return System.getProperty("java.vm.name", "").contains("IBM") && System.getProperty("java.vm.vendor").contains("IBM");
    }

    private static boolean isIBMCompressedRefs() {
        return System.getProperty("com.ibm.oti.vm.bootstrap.library.path", "").contains("compressedrefs");
    }

    private static boolean isHotspotCompressedOops() {
        String value = getHotSpotVmOptionValue("UseCompressedOops");
        if (value == null) {
            return false;
        } else {
            return Boolean.valueOf(value);
        }
    }

    private static String getHotSpotVmOptionValue(String name) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName beanName = ObjectName.getInstance("com.sun.management:type=HotSpotDiagnostic");
            Object vmOption = server.invoke(beanName, "getVMOption", new Object[] { name }, new String[] { "java.lang.String" });
            return (String)((CompositeData)vmOption).get("value");
        } catch (Throwable t) {
            return null;
        }
    }

    private static String getPlatformMBeanAttribute(String beanName, String attrName) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance(beanName);
            Object attr = server.getAttribute(name, attrName);
            if (attr != null) {
                return attr.toString();
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static String getJRockitVmArgs() {
        return getPlatformMBeanAttribute("oracle.jrockit.management:type=PerfCounters", "java.rt.vmArgs");
    }

    private static boolean isHotspotConcurrentMarkSweepGC() {
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if ("ConcurrentMarkSweep".equals(bean.getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean is64Bit() {
        String systemProp;
        systemProp = System.getProperty("com.ibm.vm.bitmode");
        if (systemProp != null) {
            return systemProp.equals("64");
        }
        systemProp = System.getProperty("sun.arch.data.model");
        if (systemProp != null) {
            return systemProp.equals("64");
        }
        systemProp = System.getProperty("java.vm.version");
        if (systemProp != null) {
            return systemProp.contains("_64");
        }
        return false;
    }
}
