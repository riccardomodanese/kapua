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
package org.eclipse.kapua.artemis;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtemisTest {

    private static final Logger logger = LoggerFactory.getLogger(ArtemisTest.class);

    private static final String SERVER_URL = "tcp://192.168.33.10:1883";
    private static final String CLIENT_ID = "client-id";

    public ArtemisTest() {
    }

    @Test
    public void testBasicConnectivity() throws Exception {
        MqttClient client = getClient(SERVER_URL, CLIENT_ID);
        MqttClient client2 = getClient(SERVER_URL, CLIENT_ID);
        MqttConnectOptions options = getOptions("user", "password");
        client.connect(options);
        client.subscribe("#");
        client.publish("testTopic", new MqttMessage());
        Assert.assertTrue("Client should be connected!", client.isConnected());
        client.disconnect();
        Thread.sleep(1000);
        Assert.assertTrue("Client should be disconnected!", !client.isConnected());

        client.connect(options);
        client2.connect(getOptions("user2", "password2"));
        Thread.sleep(1000);
        Assert.assertTrue("Client should be disconnected!", !client.isConnected());
        Assert.assertTrue("Client2 should be connected!", client2.isConnected());
        client2.disconnect();
        Thread.sleep(1000);
        Assert.assertTrue("Client should be disconnected!", !client.isConnected());
        Assert.assertTrue("Client2 should be disconnected!", !client2.isConnected());
    }

    private static MqttClient getClient(String serverUrl, String clientId) throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient client = new MqttClient(SERVER_URL, CLIENT_ID, persistence);
        client.setCallback(new MqttCallback());
        return client;
    }

    private static MqttConnectOptions getOptions(String username, String password) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        return options;
    }

}