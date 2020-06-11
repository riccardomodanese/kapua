/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kapua.integration.misc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.kapua.service.device.call.message.kura.KuraPayload;
import org.eclipse.kapua.transport.message.mqtt.MqttMessage;
import org.eclipse.kapua.transport.message.mqtt.MqttPayload;
import org.eclipse.kapua.transport.message.mqtt.MqttTopic;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.junit.Test;

public class ExtConsumerTest {

    protected static final String UPTIME = "uptime";
    protected static final String DISPLAY_NAME = "display_name";
    protected static final String MODEL_NAME = "model_name";
    protected static final String MODEL_ID = "model_id";
    protected static final String PART_NUMBER = "part_number";
    protected static final String SERIAL_NUMBER = "serial_number";
    protected static final String AVAILABLE_PROCESSORS = "available_processors";
    protected static final String TOTAL_MEMORY = "total_memory";
    protected static final String FIRMWARE = "firmware";
    protected static final String FIRMWARE_VERSION = "firmware_version";
    protected static final String BIOS = "bios";
    protected static final String BIOS_VERSION = "bios_version";
    protected static final String OS = "os";
    protected static final String OS_VERSION = "os_version";
    protected static final String OS_ARCH = "os_arch";
    protected static final String JVM_NAME = "jvm_name";
    protected static final String JVM_VERSION = "jvm_version";
    protected static final String JVM_PROFILE = "jvm_profile";
    protected static final String ESF_VERSION = "esf_version";
    protected static final String KURA_VERSION = "kura_version";
    protected static final String ESF_KURA_VERSION = "esf_kura_version";
    protected static final String APPLICATION_FRAMEWORK = "application_framework";
    protected static final String APPLICATION_FRAMEWORK_VERSION = "application_framework_version";
    protected static final String OSGI_FRAMEWORK = "osgi_framework";
    protected static final String OSGI_FRAMEWORK_VERSION = "osgi_framework_version";
    protected static final String CONNECTION_INTERFACE = "connection_interface";
    protected static final String CONNECTION_IP = "connection_ip";
    protected static final String ACCEPT_ENCODING = "accept_encoding";
    protected static final String APPLICATION_IDS = "application_ids";
    protected static final String MODEM_IMEI = "modem_imei";
    protected static final String MODEM_IMSI = "modem_imsi";
    protected static final String MODEM_ICCID = "modem_iccid";

    @Test
    public void test() throws MqttException {
        String clientId = "paho-client";
        String accountName = "kapua-sys";
        MqttClient client = new MqttClient("tcp://localhost:1883", clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("kapua-broker");
        options.setPassword("kapua-password".toCharArray());
        client.connect(options);
        String topic = "$EDC/" + accountName + "/" + clientId + "/MQTT/BIRTH";
        client.publish(topic, buildBirthMessage(accountName, topic, clientId).toByteArray(), 1, false);
        topic = accountName + "/" + clientId + "/topic1";
        client.publish(topic, buildTelemetryMessage(accountName, topic, clientId).toByteArray(), 1, false);
        client.disconnect();
    }

    public KuraPayload buildTelemetryMessage(String accountName, String topic, String clientId) throws MqttPersistenceException, MqttException {
        MqttTopic mqttTopic = new MqttTopic(topic);
        KuraPayload payloadKura = new KuraPayload();
        payloadKura.setBody(("Telemetry message").getBytes());
        payloadKura.setMetrics(buildTelemetryMessageMetrics());
        MqttPayload payload = new MqttPayload(payloadKura.toByteArray());
        MqttMessage mqttMessage = new MqttMessage(mqttTopic, new Date(), payload);
        return payloadKura;
    }

    public Map<String, Object> buildTelemetryMessageMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("metric1", "test-1");
        metrics.put("metric2", new byte[64]);
        metrics.put("metric3", new Long(123));
        return metrics;
    }

    public KuraPayload buildBirthMessage(String accountName, String topic, String clientId) throws MqttPersistenceException, MqttException {
        MqttTopic mqttTopic = new MqttTopic(topic);
        KuraPayload payloadKura = new KuraPayload();
        payloadKura.setBody(("Birth message").getBytes());
        payloadKura.setMetrics(buildBirthMessageMetrics("", ""));
        MqttPayload payload = new MqttPayload(payloadKura.toByteArray());
        MqttMessage mqttMessage = new MqttMessage(mqttTopic, new Date(), payload);
        return payloadKura;
    }

    public Map<String, Object> buildBirthMessageMetrics(String prefix, String suffix) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put(UPTIME, prefix + "uptime" + suffix);
        metrics.put(DISPLAY_NAME, prefix + "displayName" + suffix);
        metrics.put(MODEL_NAME, prefix + "modelName" + suffix);
        metrics.put(MODEL_ID, prefix + "modelId" + suffix);
        metrics.put(PART_NUMBER, prefix + "partNumber" + suffix);
        metrics.put(SERIAL_NUMBER, prefix + "serialNumber" + suffix);
        metrics.put(FIRMWARE, prefix + "firmware" + suffix);
        metrics.put(FIRMWARE_VERSION, prefix + "firmwareVersion" + suffix);
        metrics.put(BIOS, prefix + "bios" + suffix);
        metrics.put(BIOS_VERSION, prefix + "biosVersion" + suffix);
        metrics.put(OS, prefix + "os" + suffix);
        metrics.put(OS_VERSION, prefix + "osVersion" + suffix);
        metrics.put(JVM_NAME, prefix + "jvmName" + suffix);
        metrics.put(JVM_VERSION, prefix + "jvmVersion" + suffix);
        metrics.put(JVM_PROFILE, prefix + "jvmProfile" + suffix);
        metrics.put(APPLICATION_FRAMEWORK, prefix + "applicationFramework" + suffix);
        metrics.put(APPLICATION_FRAMEWORK_VERSION, prefix + "applicationFrameworkVersion" + suffix);
        metrics.put(CONNECTION_INTERFACE, prefix + "connectionInterface" + suffix);
        metrics.put(CONNECTION_IP, prefix + "connectionIp" + suffix);
        metrics.put(ACCEPT_ENCODING, prefix + "acceptEncoding" + suffix);
        metrics.put(APPLICATION_IDS, prefix + "applicationIdentifiers" + suffix);
        metrics.put(AVAILABLE_PROCESSORS, prefix + "availableProcessors" + suffix);
        metrics.put(TOTAL_MEMORY, prefix + "totalMemory" + suffix);
        metrics.put(OS_ARCH, prefix + "osArch" + suffix);
        metrics.put(OSGI_FRAMEWORK, prefix + "osgiFramework" + suffix);
        metrics.put(OSGI_FRAMEWORK_VERSION, prefix + "osgiFrameworkVersion" + suffix);
        metrics.put(MODEM_IMEI, prefix + " modemImei" + suffix);
        metrics.put(MODEM_IMSI, prefix + "modemImsi" + suffix);
        metrics.put(MODEM_ICCID, prefix + "modemIccid" + suffix);
        return metrics;
    }
}
