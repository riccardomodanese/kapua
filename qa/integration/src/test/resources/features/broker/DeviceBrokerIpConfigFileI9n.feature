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
@deviceBrokerIpConfig
@integration

Feature: Device Broker connection ip with config file
  Device Service integration scenarios with running broker service.

  Scenario: Set environment variables

    Given System property "commons.settings.hotswap" with value "true"
      And System property "broker.ip" with value "null"
      And System property "kapua.config.url" with value "broker.setting/kapua-broker-setting-1.properties"

  Scenario: Start datastore for all scenarios

    Given Start Datastore

  Scenario: Start event broker for all scenarios

    Given Start Event Broker

  Scenario: Start broker for all scenarios

    Given Start Broker

  Scenario: Start External Consumers for all scenarios

    Given Start External Consumers

  Scenario: Send BIRTH message and then DC message while broker ip is set by config file
    Effectively this is connect and disconnect of Kura device.
    Basic birth - death scenario. Scenario includes check that broker server ip
    is correctly set with config file.

    When I start the Kura Mock
    And Device birth message is sent
    And I wait 5 seconds
    And I login as user with name "kapua-sys" and password "kapua-password"
    Then Device is connected with "localhost" server ip
    And I logout
    And Device death message is sent

  Scenario: Stop External Consumers for all scenarios

    Given Stop External Consumers

  Scenario: Stop broker after all scenarios 

    Given Stop Broker

  Scenario: Stop event broker for all scenarios

    Given Stop Event Broker

  Scenario: Stop datastore after all scenarios

    Given Stop Datastore
