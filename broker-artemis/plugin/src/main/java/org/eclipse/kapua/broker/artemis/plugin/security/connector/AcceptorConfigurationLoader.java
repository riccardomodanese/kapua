/*******************************************************************************
 * Copyright (c) 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.broker.artemis.plugin.security.connector;

import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.artemis.api.core.ActiveMQException;

/**
 * Helper class to load acceptor configuration
 *
 */
public class AcceptorConfigurationLoader {

    public static final String MQTT_1883_NAME = "mqtt";
    private static final String MQTT_1883_URI = "tcp://0.0.0.0:1883?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=MQTT;useEpoll=true";
    public static final String MQTT_1884_NAME = "mqtt2";
    private static final String MQTT_1884_URI = "tcp://0.0.0.0:1884?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=MQTT;useEpoll=true";

    private static final Map<String, String> DEFAULT_ACCEPTORS_MAP = new HashMap<>();

    private AcceptorConfigurationLoader() {
    }

    public static Map<String, String> getAvailableAcceptors() {
        return DEFAULT_ACCEPTORS_MAP;
    }

    public static String addAcceptorConfiguration(String name) throws ActiveMQException {
        if (MQTT_1883_NAME.equals(name)) {
            return DEFAULT_ACCEPTORS_MAP.put(name, MQTT_1883_URI);
        }
        else if (MQTT_1884_NAME.equals(name)) {
            return DEFAULT_ACCEPTORS_MAP.put(name, MQTT_1884_URI);
        }
        //TODO find more appropriate exception
        throw new ActiveMQException("Unknown acceptor name " + name);
    }

    public static String removeAcceptorConfiguration(String name) {
        return DEFAULT_ACCEPTORS_MAP.remove(name);
    }
}
