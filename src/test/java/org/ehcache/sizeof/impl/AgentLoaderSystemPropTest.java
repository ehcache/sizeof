package org.ehcache.sizeof.impl;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Alex Snaps
 */
public class AgentLoaderSystemPropTest {
    @Test
    public void testLoadsAgentIntoSystemPropsWhenRequired() {
        System.setProperty("org.ehcache.sizeof.agent.instrumentationSystemProperty", "true");
        AgentLoader.loadAgent();
        if (AgentLoader.agentIsAvailable()) {
            assertThat(System.getProperties().get(AgentLoader.INSTRUMENTATION_INSTANCE_SYSTEM_PROPERTY_NAME), notNullValue());
        }
    }
}
