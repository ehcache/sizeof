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
