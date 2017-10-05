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

import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Alex Snaps
 */
public class AgentLoaderTest {

    @After
    public void after() {
        System.getProperties().remove("org.ehcache.sizeof.agent.instrumentationSystemProperty");
        System.getProperties().remove(AgentLoader.INSTRUMENTATION_INSTANCE_SYSTEM_PROPERTY_NAME);
    }

    @Test
    public void testLoadsAgentProperly() {
        assertThat(Boolean.getBoolean("org.ehcache.sizeof.agent.instrumentationSystemProperty"), is(false));
        AgentLoader.loadAgent();
        assertThat(AgentLoader.agentIsAvailable(), is(true));
        assertThat(System.getProperties().get(AgentLoader.INSTRUMENTATION_INSTANCE_SYSTEM_PROPERTY_NAME), nullValue());
    }
}
