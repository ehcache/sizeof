package org.ehcache.sizeof.impl;

import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Alex Snaps
 */
public class AgentLoaderSystemPropTest {

    @After
    public void after() {
        System.getProperties().remove("org.ehcache.sizeof.agent.instrumentationSystemProperty");
        System.getProperties().remove(AgentLoader.INSTRUMENTATION_INSTANCE_SYSTEM_PROPERTY_NAME);
    }

    @Test
    public void testLoadsAgentIntoSystemPropsWhenRequired() {
        System.setProperty("org.ehcache.sizeof.agent.instrumentationSystemProperty", "true");
        AgentLoader.loadAgent();
        assertThat(AgentLoader.agentIsAvailable(), is(true));
        assertThat(System.getProperties().get(AgentLoader.INSTRUMENTATION_INSTANCE_SYSTEM_PROPERTY_NAME), notNullValue());
    }
}
