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

import org.eclipse.kapua.qa.steps.EmbeddedEventBroker;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttCallback implements org.eclipse.paho.client.mqttv3.MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedEventBroker.class);

    @Override
    public void connectionLost(Throwable cause) {
        logger.info("Connection lost: {}", cause.getMessage(), cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.info("Messaage arrived on topic: {}", topic);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.info("Delivery complete!");
    }

}