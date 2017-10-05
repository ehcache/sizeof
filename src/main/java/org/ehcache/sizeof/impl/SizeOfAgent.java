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

import java.lang.instrument.Instrumentation;

/**
 * @author Alex Snaps
 */
public class SizeOfAgent {

    private static volatile Instrumentation instrumentation;
    private static final String NO_INSTRUMENTATION_SYSTEM_PROPERTY_NAME = "org.ehcache.sizeof.agent.instrumentationSystemProperty";

    public static void premain(String options, Instrumentation inst) {
        SizeOfAgent.instrumentation = inst;
        registerSystemProperty();
    }

    public static void agentmain(String options, Instrumentation inst) {
        SizeOfAgent.instrumentation = inst;
        registerSystemProperty();
    }

    private static void registerSystemProperty() {
        if (Boolean.getBoolean(NO_INSTRUMENTATION_SYSTEM_PROPERTY_NAME)) {
            System.getProperties().put(AgentLoader.INSTRUMENTATION_INSTANCE_SYSTEM_PROPERTY_NAME, instrumentation);
        }
    }

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    private SizeOfAgent() {
        //not instantiable
    }
}
