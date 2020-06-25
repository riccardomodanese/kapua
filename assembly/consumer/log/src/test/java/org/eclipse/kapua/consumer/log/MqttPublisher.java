/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.consumer.log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttPublisher {

    public static final Logger logger = LoggerFactory.getLogger(MqttPublisher.class);

    private static final String LOGSTASH_3194 = "logstash3194";
    private static final String FLUENTD_3194 = "fluentd3194";
    private static final String FLUENTD_5424 = "fluentd5424";

    //https://tools.ietf.org/html/rfc5424#section-6.5
    private static final String[] MESSAGES_5424 = new String[]{
        "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8",
        "<165>1 2003-08-24T05:14:15.000003-07:00 192.0.2.1 myproc 8710 - - %% It's time to make the do-nuts.",
        "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"] BOMAn application event log entry...",
        "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"][examplePriority@32473 class=\"high\"]"//error
    };

    //https://tools.ietf.org/html/rfc3164#section-5.4
    private static final String[] MESSAGES_3164 = new String[]{
        "<34>Oct 11 22:14:15 mymachine su: 'su root' failed for lonvick on /dev/pts/8",
        "Use the BFG!",//error
        "<13>Feb  5 17:32:18 10.0.0.99 Use the BFG!",
        "<165>Aug 24 05:34:00 CST 1987 mymachine myproc[10]: %% It's time to make the do-nuts.  %%  Ingredients: Mix=OK, Jelly=OK # Devices: Mixer=OK, Jelly_Injector=OK, Frier=OK # Transport: Conveyer1=OK, Conveyer2=OK # %%",
        "<0>1990 Oct 22 10:52:01 TZ-6 scapegoat.dmz.example.org 10.1.2.3 sched[0]: That's All Folks!"//error
    };

    private MqttPublisher() {
    }

    public static void main(String[] argv) throws MqttSecurityException, MqttException {
        String clientId = "log-test-client";
        MqttClient client = new MqttClient("tcp://localhost:1883", clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("log-username");
        options.setPassword("log-password".toCharArray());
        client.connect(options);
        for (int i=0; i<1; i++) {
//            logger.info("Publishing RFC 3164 messages to logstash...");
//            for (int j=0; j<MESSAGES_3164.length; j++) {
//                logger.info("Publishing message: {}", MESSAGES_3164[j]);
//                client.publish(LOGSTASH_3194, getLogMessage(MESSAGES_3164[j], clientId));
//            }
//            logger.info("Publishing RFC 3164 messages to fluentd...");
//            for (int j=0; j<MESSAGES_3164.length; j++) {
//                logger.info("Publishing message: {}", MESSAGES_3164[j]);
//                client.publish(FLUENTD_3194, getLogMessage(MESSAGES_3164[j], clientId));
//            }
            logger.info("Publishing RFC 5424 messages to fluentd...");
            for (int j=0; j<MESSAGES_5424.length; j++) {
                logger.info("Publishing message: {}", MESSAGES_5424[j]);
                client.publish(FLUENTD_5424, getLogMessage(MESSAGES_5424[j], clientId));
            }
        }
        client.disconnect();
    }

    private static MqttMessage getLogMessage(String message, String clientId) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload((message).getBytes());
        mqttMessage.setQos(1);
        mqttMessage.setRetained(false);
        return mqttMessage;
    }
}
