###############################################################################
# Copyright (c) 2020 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech
###############################################################################
@security
@unit
Feature: Credentials
  This feature file contains Unit tests for Credentials (CRUD tests).

  Scenario: Creating a new PASSWORD Credential meeting the standard length requirement
  Create a new Credential of type PASSWORD that meets the default string length requirements
    Given I create a new PASSWORD credential for the default user with password "Welcome12345!"
    Then No exception was thrown

  Scenario: Creating a new PASSWORD Credential not meeting the standard length requirement
  Create a new Credential of type PASSWORD that does NOT meet the default string length requirements
    Given I expect the exception "KapuaPasswordTooShortException"
    When I create a new PASSWORD credential for the default user with password "badPass"
    Then An exception was thrown

  Scenario: Set a correct minimum for password length
  Set a custom minimum value for password length that must be equal or higher to the default system limit
    Given I configure the credential service
      | type    | name                       | value |
      | boolean | lockoutPolicy.enabled      | true  |
      | integer | lockoutPolicy.maxFailures  | 3     |
      | integer | lockoutPolicy.resetAfter   | 3600  |
      | integer | lockoutPolicy.lockDuration | 10800 |
      | integer | password.minLength         | 15    |
    Then No exception was thrown

  Scenario: Set an incorrect minimum for password length
  Set a custom minimum value for password length that is not equal or higher to the default system limit must result in
  an error
    Given I expect the exception "KapuaConfigurationException"
    And I configure the credential service
      | type    | name                       | value |
      | boolean | lockoutPolicy.enabled      | true  |
      | integer | lockoutPolicy.maxFailures  | 3     |
      | integer | lockoutPolicy.resetAfter   | 3600  |
      | integer | lockoutPolicy.lockDuration | 10800 |
      | integer | password.minLength         | 10    |
    Then An exception was thrown
    When I read credential service configuration
    Then I check that service configuration "password.minLength" has no value

  Scenario: Creating a new PASSWORD Credential meeting a custom length requirement
  Create a new Credential of type PASSWORD that meets a custom string length requirements
    Given I configure the credential service
      | type    | name                       | value |
      | boolean | lockoutPolicy.enabled      | true  |
      | integer | lockoutPolicy.maxFailures  | 3     |
      | integer | lockoutPolicy.resetAfter   | 3600  |
      | integer | lockoutPolicy.lockDuration | 10800 |
      | integer | password.minLength         | 15    |
    And I create a new PASSWORD credential for the default user with password "Welcome12345678!"
    Then No exception was thrown

  Scenario: Creating a new PASSWORD Credential not meeting a custom length requirement
  Create a new Credential of type PASSWORD that does NOT meet a custom string length requirements
    Given I configure the credential service
      | type    | name                       | value |
      | boolean | lockoutPolicy.enabled      | true  |
      | integer | lockoutPolicy.maxFailures  | 3     |
      | integer | lockoutPolicy.resetAfter   | 3600  |
      | integer | lockoutPolicy.lockDuration | 10800 |
      | integer | password.minLength         | 15    |
    And I expect the exception "KapuaPasswordTooShortException"
    When I create a new PASSWORD credential for the default user with password "Welcome12345!"
    Then An exception was thrown
