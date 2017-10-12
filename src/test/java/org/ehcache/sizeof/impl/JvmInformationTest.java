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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Basic test used during refactoring of enum to make sure no differences were introduced
 */
public class JvmInformationTest {

    @Test
    public void hotspot32Bits() {
        verifyJvmInfo(JvmInformation.HOTSPOT_32_BIT, 0, 0, 4, 8, 8, 8, 4, true, true, true);
        verifyJvmInfo(JvmInformation.HOTSPOT_32_BIT_WITH_CONCURRENT_MARK_AND_SWEEP, 0, 0, 4, 16, 8, 8, 4, true, true, true);
    }

    @Test
    public void hotspot64Bits() {
        verifyJvmInfo(JvmInformation.HOTSPOT_64_BIT, 0, 0, 8, 8, 8, 16, 8, true, true, true);
        verifyJvmInfo(JvmInformation.HOTSPOT_64_BIT_WITH_CONCURRENT_MARK_AND_SWEEP, 0, 0, 8, 24, 8, 16, 8, true, true, true);
        verifyJvmInfo(JvmInformation.HOTSPOT_64_BIT_WITH_COMPRESSED_OOPS, 0, 0, 4, 8, 8, 12, 8, true, true, true);
        verifyJvmInfo(JvmInformation.HOTSPOT_64_BIT_WITH_COMPRESSED_OOPS_AND_CONCURRENT_MARK_AND_SWEEP, 0, 0, 4, 24, 8, 12, 8, true, true, true);
    }

    @Test
    public void openJdk32Bits() {
        verifyJvmInfo(JvmInformation.OPENJDK_32_BIT, 0, 0, 4, 8, 8, 8, 4, true, true, true);
        verifyJvmInfo(JvmInformation.OPENJDK_32_BIT_WITH_CONCURRENT_MARK_AND_SWEEP, 0, 0, 4, 16, 8, 8, 4, true, true, true);
    }

    @Test
    public void openJdk64Bits() {
        verifyJvmInfo(JvmInformation.OPENJDK_64_BIT, 0, 0, 8, 8, 8, 16, 8, true, true, true);
        verifyJvmInfo(JvmInformation.OPENJDK_64_BIT_WITH_CONCURRENT_MARK_AND_SWEEP, 0, 0, 8, 24, 8, 16, 8, true, true, true);
        verifyJvmInfo(JvmInformation.OPENJDK_64_BIT_WITH_COMPRESSED_OOPS, 0, 0, 4, 8, 8, 12, 8, true, true, true);
        verifyJvmInfo(JvmInformation.OPENJDK_64_BIT_WITH_COMPRESSED_OOPS_AND_CONCURRENT_MARK_AND_SWEEP, 0, 0, 4, 24, 8, 12, 8, true, true, true);
    }

    @Test
    public void ibm32Bits() {
        verifyJvmInfo(JvmInformation.IBM_32_BIT, 0, 0, 4, 8, 8, 16, 4, true, false, true);
    }

    @Test
    public void ibm64Bits() {
        verifyJvmInfo(JvmInformation.IBM_64_BIT, 0, 0, 8, 8, 8, 24, 8, true, false, true);
        verifyJvmInfo(JvmInformation.IBM_64_BIT_WITH_COMPRESSED_REFS, 0, 0, 4, 8, 8, 16, 4, true, false, true);
    }

    @Test
    public void unknown() {
        verifyJvmInfo(JvmInformation.UNKNOWN_32_BIT, 0, 0, 4, 8, 8, 8, 4, true, true, true);
        verifyJvmInfo(JvmInformation.UNKNOWN_64_BIT, 0, 0, 8, 8, 8, 16, 8, true, true, true);
    }

    private void verifyJvmInfo(JvmInformation jvmInfo, int agentSizeOfAdj, int fieldOffsetAdj, int javaPointerSize, int minObjSize, int objAlign, int objHeaderSize, int pointerSize,
                               boolean supportAgentSizeOf, boolean supportReflectionSizeOf, boolean supportUnsafeSizeOf) {
        assertThat(jvmInfo.getAgentSizeOfAdjustment(), is(agentSizeOfAdj));
        assertThat(jvmInfo.getFieldOffsetAdjustment(), is(fieldOffsetAdj));
        assertThat(jvmInfo.getJavaPointerSize(), is(javaPointerSize));
        assertThat(jvmInfo.getMinimumObjectSize(), is(minObjSize));
        assertThat(jvmInfo.getObjectAlignment(), is(objAlign));
        assertThat(jvmInfo.getObjectHeaderSize(), is(objHeaderSize));
        assertThat(jvmInfo.getPointerSize(), is(pointerSize));
        assertThat(jvmInfo.supportsAgentSizeOf(), is(supportAgentSizeOf));
        assertThat(jvmInfo.supportsReflectionSizeOf(), is(supportReflectionSizeOf));
        assertThat(jvmInfo.supportsUnsafeSizeOf(), is(supportUnsafeSizeOf));
    }
}
