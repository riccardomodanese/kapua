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
public class ConnectorConfigurationLoader {

    public static final String NETWORK_CONNECTOR_NAME_1 = "brokerNetwork-1";
    private static final String NETWORK_CONNECTOR_URI_1 = "tcp://localhost:61616";
    public static final String NETWORK_CONNECTOR_NAME_2 = "brokerNetwork-2";
    private static final String NETWORK_CONNECTOR_URI_2 = "tcp://localhost:61618";
    public static final String NETWORK_CONNECTOR_NAME_3 = "brokerNetwork-3";
    private static final String NETWORK_CONNECTOR_URI_3 = "tcp://localhost:61620";

    private static final Map<String, String> DEFAULT_CONNECTORS_MAP = new HashMap<>();

    private ConnectorConfigurationLoader() {
        DEFAULT_CONNECTORS_MAP.put(NETWORK_CONNECTOR_NAME_1, NETWORK_CONNECTOR_URI_1);
        DEFAULT_CONNECTORS_MAP.put(NETWORK_CONNECTOR_NAME_2, NETWORK_CONNECTOR_URI_2);
        DEFAULT_CONNECTORS_MAP.put(NETWORK_CONNECTOR_NAME_3, NETWORK_CONNECTOR_URI_3);
    }

    public static String getConnector(String name) {
        return DEFAULT_CONNECTORS_MAP.get(name);
    }

    public static String addConnectorConfiguration(String name) throws ActiveMQException {
        if (NETWORK_CONNECTOR_NAME_1.equals(name)) {
            return DEFAULT_CONNECTORS_MAP.put(name, NETWORK_CONNECTOR_URI_1);
        }
        else if (NETWORK_CONNECTOR_NAME_2.equals(name)) {
            return DEFAULT_CONNECTORS_MAP.put(name, NETWORK_CONNECTOR_URI_2);
        }
        else if (NETWORK_CONNECTOR_NAME_3.equals(name)) {
            return DEFAULT_CONNECTORS_MAP.put(name, NETWORK_CONNECTOR_URI_3);
        }
        //TODO find more appropriate exception
        throw new ActiveMQException("Unknown connector name " + name);
    }

    public static String removeConnectorConfiguration(String name) {
        return DEFAULT_CONNECTORS_MAP.remove(name);
    }
}
