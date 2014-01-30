package org.ehcache.sizeof;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import static net.sf.ehcache.pool.sizeof.JvmInformation.CURRENT_JVM_INFORMATION;

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
                     System.getProperty("java.vm.name", "").toLowerCase().indexOf("jrockit") >= 0;

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