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
@deviceBrokerIpUndefined
@integration
@env_docker

Feature: Device Broker connection ip not set
  Device Service integration scenarios with running broker service.

  Scenario: Start full docker environment
    Given Reset test shutdown
    And Init Jaxb Context
    And Init Security Context
    And System property "broker.ip" with value "null"
    And System property "kapua.config.url" with value "null"
    And Start full docker environment

  Scenario: Send BIRTH message and then DC message while broker ip is NOT set
  Effectively this is connect and disconnect of Kura device.
  Basic birth - death scenario. Scenario should fail as broker ip is not set
  as it should be.

    When I start the Kura Mock
    And Device birth message is sent
    And I wait 5 seconds
    And Device death message is sent
    And I wait 5 seconds

  Scenario: Stop full docker environment
    Given Set test shutdown
    And Stop full docker environment
