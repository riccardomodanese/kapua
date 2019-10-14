###############################################################################
# Copyright (c) 2017, 2019 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech - initial API and implementation
###############################################################################
@broker
@stealingLink
@integration

Feature: Device Broker Cluster tests
    Test functionality for distributed Stealing link scenarios. This is case for
    cluster of brokers where CONNECT messages get forwarded form one broker to
    others in cluster and by this disconnecting client form other brokers.
    Tests also include connecting client with same id.

    Scenario: Set environment variables

        Given System property "commons.settings.hotswap" with value "true"
        And System property "broker.ip" with value "localhost"
        And System property "kapua.config.url" with value "null"

  Scenario: Start datastore for all scenarios

    Given Start Datastore

  Scenario: Start event broker for all scenarios

    Given Start Event Broker

  Scenario: Start broker for all scenarios

    Given Start Broker

  Scenario: Start external consumers for all scenario

    Given Start External Consumers

    Scenario: Positive scenario without stealing link
        Connect first client and send BIRTH message. Then connect two more
        clients with different client ids. After that all clients should be connected.
        This is pure positive scenario.

    Given Client with name "client-1" with client id "client-1" user "kapua-broker" password "kapua-password" is connected
        And topic "$EDC/kapua-sys/client-1/MQTT/BIRTH" content "/mqtt/rpione3_MQTT_BIRTH.mqtt" is published by client named "client-1"
        And I wait 2 seconds
        And Client with name "client-2" with client id "client-2" user "kapua-broker" password "kapua-password" is connected
        And Client with name "client-3" with client id "client-3" user "kapua-broker" password "kapua-password" is connected
    Then Client named "client-1" is connected
        And Client named "client-2" is connected
        And Client named "client-3" is connected
    Then Disconnect client with name "client-1"
        And Disconnect client with name "client-2"
        And Disconnect client with name "client-3"

    Scenario: Stealing link scenario
        Connect first client and send BIRTH message. Then connect two more
        clients with different client ids.After that all clients should be connected.
        then connect another client under admin account for simulating other broker and
        send CONNECT messages for all three clients with their client ids. This disconnects
        all clients locally. This emulates that those clients were connected on another broker.

    Given Client with name "client-1" with client id "client-1" user "kapua-broker" password "kapua-password" is connected
        And Client with name "client-sys" with client id "client-sys" user "kapua-sys" password "kapua-password" is connected
        And topic "$EDC/kapua-sys/client-1/MQTT/BIRTH" content "/mqtt/rpione3_MQTT_BIRTH.mqtt" is published by client named "client-1"
        And I wait 2 seconds
        And Client with name "client-2" with client id "client-2" user "kapua-broker" password "kapua-password" is connected
        And Client with name "client-3" with client id "client-3" user "kapua-broker" password "kapua-password" is connected
    Then Client named "client-1" is connected
        And Client named "client-2" is connected
        And Client named "client-3" is connected
    Given topic "$EDC/kapua-sys/client-2/MQTT/CONNECT" content "/mqtt/rpione3_MQTT_BIRTH.mqtt" is published by client named "client-sys"
        And I wait 2 seconds
    Then Client named "client-1" is connected
        And Client named "client-2" is not connected
        And Client named "client-3" is connected
    Given topic "$EDC/kapua-sys/client-1/MQTT/CONNECT" content "/mqtt/rpione3_MQTT_BIRTH.mqtt" is published by client named "client-sys"
        And I wait 2 seconds
    Then Client named "client-1" is not connected
        And Client named "client-2" is not connected
        And Client named "client-3" is connected
    Given topic "$EDC/kapua-sys/client-3/MQTT/CONNECT" content "/mqtt/rpione3_MQTT_BIRTH.mqtt" is published by client named "client-sys"
        And I wait 2 seconds
    Then Client named "client-1" is not connected
        And Client named "client-2" is not connected
        And Client named "client-3" is not connected
    Then Disconnect client with name "client-1"
        And Disconnect client with name "client-2"
        And Disconnect client with name "client-3"

    Scenario: Negative scenario when client connects twice with same client id
        Connect first client and send BIRTH message and CONNECT message. Then
        connect another client with same client id and send CONNECT message.
        This disconnects first client.

    Given Client with name "client-1-1" with client id "client-1" user "kapua-broker" password "kapua-password" is connected
        And topic "$EDC/kapua-sys/client-1/MQTT/BIRTH" content "/mqtt/rpione3_MQTT_BIRTH.mqtt" is published by client named "client-1-1"
        And topic "$EDC/kapua-sys/client-1/MQTT/CONNECT" content "/mqtt/rpione3_MQTT_BIRTH.mqtt" is published by client named "client-1-1"
        And I wait 2 seconds
        And Client with name "client-1-2" with client id "client-1" user "kapua-broker" password "kapua-password" is connected
        And I wait 2 seconds
    Then Client named "client-1-1" is not connected
        And Client named "client-1-2" is connected
    Then Disconnect client with name "client-1-1"
        And Disconnect client with name "client-1-2"

  Scenario: Stop external consumers for all scenario 

    Given Stop External Consumers

  Scenario: Stop broker after all scenarios 

    Given Stop Broker

  Scenario: Stop event broker for all scenarios

    Given Stop Event Broker

  Scenario: Stop datastore after all scenarios

    Given Stop Datastore
