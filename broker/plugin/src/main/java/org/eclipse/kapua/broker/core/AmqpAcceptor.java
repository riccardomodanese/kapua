/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.broker.core;

import java.io.IOException;
import java.util.UUID;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class AmqpAcceptor extends BaseHandler {

    protected static final Logger logger = LoggerFactory.getLogger(AmqpAcceptor.class);
    private Thread thread;
    private final static MqttClient MQTT_CLIENT;

    static {
        try {
            MQTT_CLIENT = new MqttClient("tcp://127.0.0.1:1883", "kapua-bridge-client" + UUID.randomUUID().toString(), new MemoryPersistence());
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public AmqpAcceptor() {
        add(new Handshaker());
        add(new FlowController());
    }

    public void start() throws MqttSecurityException, MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("kapua-sys");
        options.setPassword("kapua-password".toCharArray());
        options.setCleanSession(true);
        // FIXME: Set other connect options!

        MQTT_CLIENT.connect(options);
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Reactor r = Proton.reactor(new AmqpAcceptor());
                    r.run();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void onReactorInit(Event event) {
        try {
            event.getReactor().acceptor("0.0.0.0", 5673);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void onDelivery(Event event) {
        Receiver recv = (Receiver) event.getLink();
        Delivery delivery = recv.current();
        byte[] dataMsg = null;
        Message msg = null;
        if (delivery.isReadable() && !delivery.isPartial()) {
            int size = delivery.pending();
            byte[] buffer = new byte[size];
            int read = recv.recv(buffer, 0, buffer.length);
            recv.advance();
            msg = Proton.message();
            msg.decode(buffer, 0, read);
            Section body = msg.getBody();
            if (body instanceof Data) {
                Binary tmp = ((Data) body).getValue();
                dataMsg = tmp.getArray();
            } else if (body instanceof AmqpValue) {
                String content = (String) ((AmqpValue) body).getValue();
                logger.info("Received message with content: {}", content);
                dataMsg = content.getBytes();
            }
            logger.info("Message received: {}", dataMsg);
        }
        String topic = (String)msg.getProperties().getTo();
        MqttMessage message = new MqttMessage(dataMsg);
        message.setQos(1);
        message.setRetained(false);
        try {
            MQTT_CLIENT.publish(topic, message);
        } catch (MqttException e) {
            logger.error("Error forwarding Mqtt message: {}", e.getMessage(), e);
        }
    }

}
