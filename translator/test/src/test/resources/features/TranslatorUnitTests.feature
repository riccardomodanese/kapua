###############################################################################
# Copyright (c) 2020, 2021 Eurotech and/or its affiliates and others
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#     Eurotech
###############################################################################
@translator
@env_none

Feature: Translator Service
  The Translator Service is responsible for messaging operations between Kura and Kapua.

  #KapuaTranslatorApi

@setup
@KapuaProperties("locator.class.impl=org.eclipse.kapua.qa.common.MockedLocator")
  Scenario: Initialize test environment
    Given Init Jaxb Context
    And Init Security Context

  Scenario: Translating "CommandRequestMessage" to "KuraRequestMessage"
  Trying to make translation from CommandRequestMessage to KuraRequestMessage.

    When I try to translate from "org.eclipse.kapua.service.device.management.command.message.internal.CommandRequestMessage" to "org.eclipse.kapua.service.device.call.message.kura.app.request.KuraRequestMessage"
    Then Translator "TranslatorAppCommandKapuaKura" is found
    And No exception was thrown

  Scenario: Translating CommandRequestMessage to null
  Trying to make translation from CommandRequestMessage to null message.
  NullPointerException should be thrown.

    Given I expect the exception "NullPointerException" with the text "*"
    When I try to translate from "org.eclipse.kapua.service.device.management.command.message.internal.CommandRequestMessage" to ""
    Then An exception was thrown

  Scenario: Translating null to KuraRequestMessage
  Trying to make translation from null to KuraRequestMessage message.
  NullPointerException should be thrown.

    Given I expect the exception "NullPointerException" with the text "*"
    When I try to translate from "" to "org.eclipse.kapua.service.device.call.message.kura.app.request.KuraRequestMessage"
    Then An exception was thrown

  Scenario: Translating empty message to empty message
  Trying to do translation without messages. NPE exception should be thrown.

    Given I expect the exception "NullPointerException" with the text "*"
    And I try to translate from "" to ""
    Then An exception was thrown

  Scenario: Translating from "AssetRequestMessage" to "AssetResponseMessage"
  Trying to make translation from AssetRequestMessage to AssetResponseMessage.
  A translator not found exception should be thrown.

    Given I expect the exception "TranslatorNotFoundException" with the text "*"
    And I try to translate from "org.eclipse.kapua.service.device.management.asset.message.internal.AssetRequestMessage" to "org.eclipse.kapua.service.device.management.asset.message.internal.AssetResponseMessage"
    Then An exception was thrown

  #TranslatorDataAmqpKura

  Scenario: Translation of amqp message with invalid payload and valid topic into kura data message
  Creating amqp message with invalid payload and valid topic. Trying to translate it into kura data message.
  Checking if kura data message with valid channel and byte[] payload is received.

    Given I create amqp message with invalid payload "invalidPayload" and valid topic "kapua-sys.rpione3.DEPLOY-V2.GET.packages"
    And I try to translate amqp message to kura data message
    Then I got kura data message with "byte[]" payload body
    And I got kura data message channel with "kapua-sys" and "rpione3" data
    And No exception was thrown

  Scenario: Translation of amqp message with valid payload and valid topic into kura data message
  Creating amqp message with valid payload and valid topic. Trying to translate it into kura data message.
  Checking if amqp message with valid channel and proper payload metrics is received.

    Given I create amqp message with valid payload "response.code" and valid topic "kapua-sys.rpione3.DEPLOY-V2.GET.packages"
    And I try to translate amqp message to kura data message
    Then I got kura data message with proper payload metrics response code 200
    And I got kura data message channel with "kapua-sys" and "rpione3" data
    And No exception was thrown

  Scenario: Translation of amqp message with empty payload into kura data message
  Creating amqp message with empty payload and valid topic. Trying to translate it into kura data message.
  Checking if amqp message with valid channel and empty payload is received.

    Given I create amqp message with empty payload "" and valid topic "kapua-sys.rpione3.DEPLOY-V2.GET.packages"
    And I try to translate amqp message to kura data message
    Then I got kura data message with empty payload
    And I got kura data message channel with "kapua-sys" and "rpione3" data
    And No exception was thrown

  #TranslatorResponseAmqpKura

  Scenario: Translation of amqp message with invalid payload and invalid topic into kura response message
  Creating amqp message with invalid payload and invalid topic. Trying to translate it into kura response message.

    Given I expect the exception "InvalidChannelException" with the text "Invalid channel: DEPLOY-V2.GET.packages"
    And I create amqp message with invalid payload "invalidPayload" and invalid topic "DEPLOY-V2.GET.packages"
    When I try to translate amqp response
    Then An exception was thrown

  Scenario: Translation of amqp message with invalid payload and valid topic into kura response message
  Creating amqp message with invalid payload and valid topic. Trying to translate it into kura response message.
  Check if kura response with byte[] body and correct channel is received.

    Given I create amqp message with invalid payload "invalidPayload" and valid topic "$EDC.kapua-sys.rpione3.DEPLOY-V2.GET.packages"
    When I try to translate amqp response
    Then I got kura response message with "byte[]" payload body
    And I got kura response message channel with "GET", "packages", "DEPLOY-V2", "$EDC", "kapua-sys" and "rpione3" data
    And No exception was thrown

  Scenario: Translation of amqp message with valid payload and invalid topic into kura response message
  Creating amqp message with valid payload and invalid topic. Trying to translate it into kura response message.

    Given I expect the exception "InvalidChannelException" with the text "Invalid channel: DEPLOY-V2.GET.packages"
    And I create amqp message with valid payload "response.code" and invalid topic "DEPLOY-V2.GET.packages"
    When I try to translate amqp response
    Then An exception was thrown

  Scenario: Translation of amqp message with valid payload and valid topic into kura response message
  Creating amqp message with valid payload and valid topic. Trying to translate it into kura response message.
  Check if kura response message with proper payload, metrics and correct channel is received.

    Given I create amqp message with valid payload "response.code" and valid topic "$EDC.kapua-sys.rpione3.DEPLOY-V2.GET.packages"
    When I try to translate amqp response
    Then I got kura response message with proper payload metrics
    And I got kura response message channel with "GET", "packages", "DEPLOY-V2", "$EDC", "kapua-sys" and "rpione3" data
    And No exception was thrown

  #TranslatorDataKuraAmqp

  Scenario: Translation of kura data message with valid channel and without body and metrics into amqp message
  Creating kura data message with valid channel and without body and metrics. Trying to translate it into amqp message.
  Checking if amqp message with valid topic and empty body is received.

    Given I create kura data message with channel with scope "kapua-sys", client id "rpione3" and payload without body and metrics
    When I try to translate kura data message to amqp message
    Then I get amqp message with channel with scope "kapua-sys", client id "rpione3" and empty body
    And No exception was thrown

  Scenario: Translation of kura data message with valid channel, metrics and without body into amqp message
  Creating kura data message with valid channel, metrics but without body. Trying to translate it into amqp message.
  Check if amqp message with valid topic and encoded body is received.

    Given I create kura data message with channel with scope "kapua-sys", client id "rpione3", valid payload and metrics but without body
    When I try to translate kura data message to amqp message
    Then I get amqp message with channel with scope "kapua-sys", client id "rpione3" and non empty body
    And No exception was thrown

  Scenario: Translation of kura data message with valid channel, body and metrics into amqp message
  Creating kura data message with valid channel, body and metrics. Trying to translate it into amqp message.
  Check if amqp message with valid topic and encoded body is received.

    Given I create kura data message with channel with scope "kapua-sys", client id "rpione3" and payload with body and metrics
    And I try to translate kura data message to amqp message
    Then I get amqp message with channel with scope "kapua-sys", client id "rpione3" and non empty body
    And No exception was thrown

    #TranslatorDataAmqpKura

  Scenario: Translating of amqp message with invalid payload and invalid topic into kura data message
  Creating amqp message with invalid payload and invalid topic that contain accountName only. Trying to translate it into kura data message.
  Invalid channel exception should be thrown.

    Given I create amqp message with valid payload "invalidPayload" and invalid topic "kapua-sys"
    And I expect the exception "InvalidChannelException" with the text "Invalid channel: kapua-sys"
    When I try to translate amqp message to kura data message
    Then An exception was thrown

  Scenario: Translating of amqp message with invalid payload and with null topic into kura data message
  Creating amqp message with invalid payload and invalid "null" AmqpMessage. Trying to translate it into kura data message.
  Invalid message exception should be thrown.

    Given I create amqp message with valid payload "invalidPayload" and valid topic "kapua-sys.rpione3.DEPLOY-V2.GET"
    And I expect the exception "InvalidMessageException" with the text "Invalid message: null"
    When I try to translate amqp null message to kura data message
    Then An exception was thrown

  #TranslatorDataKuraAmqp

  Scenario: Translating kura data message with valid channel and with null payload
  Creating kura data message with valid channel and with null payload. Trying to translate it into amqp message.
  Invalid payload exception should be thrown.

    Given I create kura data message with channel with scope "kapua-sys", client id "rpione3" and null payload
    And I expect the exception "InvalidPayloadException" with the text "Invalid payload: null"
    When I try to translate kura data message to amqp message
    Then An exception was thrown


  Scenario: Translating kura data message with valid channel, metrics and without body into amqp message
  Creating kura data message with valid channel, metrics and without body. Trying to translate it into amqp message.
  Invalid channel exception should be thrown.

    Given I create kura data message with null channel and payload without body and with metrics
    And I expect the exception "InvalidChannelException" with the text "Invalid channel: null"
    When I try to translate kura data message to amqp message
    Then An exception was thrown

  Scenario: Translating invalid kura data message with valid channel, body and metrics into amqp message
  Creating null kura data message with valid channel, body and metrics. Trying to translate it into invalid amqp message.
  Check if amqp message with valid topic and encoded body is received.
  Invalid message exception should be thrown

    Given I create kura data message with channel with scope "kapua-sys", client id "rpione3" and payload with body and metrics
    And I expect the exception "InvalidMessageException" with the text "Invalid message: null"
    When I try to translate invalid kura data message to amqp message
    Then An exception was thrown

  #TranslatorDataJmsKura

  Scenario: Translating of jms message with invalid payload and valid topic into kura data message
  Creating jms message with invalid payload and valid topic. Trying to translate it into kura data message.
  Check if jms message with valid topic and encoded payload body is received.

    Given I create jms message with invalid payload "invalidPayload" and valid topic "kapua-sys.rpione3.DEPLOY-V2.GET.packages"
    When I try to translate jms message to kura data message
    Then I got kura data message channel with "kapua-sys" scope, "rpione3" client id and proper semanticPart
      | DEPLOY-V2    |
      | GET          |
      | packages     |
    And I got kura data message with "byte[]" payload body
    And No exception was thrown

  Scenario: Translating of jms message with valid payload and valid topic into kura data message
  Creating jms message with valid payload and valid topic. Trying to translate it into kura data message.
  Check if jms message with valid topic and encoded payload body is received.

    Given I create jms message with valid payload "response.code" and valid topic "kapua-sys.rpione3.DEPLOY-V2.GET.packages"
    When I try to translate jms message to kura data message
    Then I got kura data message channel with "kapua-sys" scope, "rpione3" client id and proper semanticPart
      | DEPLOY-V2    |
      | GET          |
      | packages     |
    And I got kura data message with proper payload metrics response code 200
    And No exception was thrown

  Scenario: Translating of jms message with empty payload and valid topic into kura data message
  Creating jms message with empty payload and valid topic. Trying to translate it into kura data message.
  Check if jms message with valid topic and empty payload body is received.

    Given I create jms message with empty payload "" and valid topic "kapua-sys.rpione3.DEPLOY-V2.GET.packages"
    And I try to translate jms message to kura data message
    And I got kura data message channel with "kapua-sys" scope, "rpione3" client id and proper semanticPart
      | DEPLOY-V2    |
      | GET          |
      | packages     |
    Then I got kura data message with empty payload
    And No exception was thrown

  Scenario: Translating of jms message with empty payload and invalid topic that contain only userName into kura data message
  Creating jms message with empty payload and invalid topic. Trying to translate it into kura data message.
  Invalid channel exception should be thrown.

    Given I create jms message with empty payload "" and valid topic "kapua-sys"
    Then I expect the exception "InvalidChannelException" with the text " Invalid channel: kapua-sys"
    When I try to translate jms message to kura data message
    And An exception was thrown

  Scenario: Translating invalid jms data message with valid channel, body and metrics into kura data message
  Creating null jms message with valid channel, body and metrics. Trying to translate it into invalid jms message.
  Check if amqp message with valid topic and encoded body is received.
  Invalid message exception should be thrown

    Given I create jms message with valid payload "response.code" and valid topic "kapua-sys.rpione3.DEPLOY-V2.GET.packages"
    Then I expect the exception "InvalidMessageException" with the text "Invalid message: null"
    When I try to translate invalid jms message to kura data message
    And An exception was thrown

  #TranslatorDataKuraJms

  Scenario: Translating kura data message with valid channel and without body and metrics into jms message
  Creating kura data message with valid channel and without body and metrics. Trying to translate it into jms message.
  Check if jms message with valid topic and empty payload body is received.

    Given I create kura data message with channel with scope "kapua-sys", client id "rpione3" and payload without body and metrics
    When I try to translate kura data message to jms message
    Then I got jms message with topic "kapua-sys.rpione3" and empty body
    And No exception was thrown

  Scenario: Translating kura data message with valid channel, metrics and without body into jms message
  Creating kura data message with valid channel, metrics and without body. Trying to translate it into jms message.
  Check if jms message with valid topic and encoded payload body is received.

    Given I create kura data message with channel with scope "kapua-sys", client id "rpione3" and payload without body and metrics
    When I try to translate kura data message to jms message
    Then I got jms message with topic "kapua-sys.rpione3" and non empty body
    And No exception was thrown

  Scenario: Translating kura data message with valid channel, body and metrics into jms message
  Creating kura data message with valid channel, body and metrics. Trying to translate it into jms message.
  Check if jms message with valid topic and encoded payload body is received.

    Given I create kura data message with channel with scope "kapua-sys", client id "rpione3" and payload with body and metrics
    When I try to translate kura data message to jms message
    Then I got jms message with topic "kapua-sys.rpione3" and non empty body
    And No exception was thrown

  Scenario: Translating kura data message with valid channel, and with null payload
  Creating kura data message with valid channel and with "null" payload Trying to translate it into jms message.
  Invalid payload exception should be thrown.

    Given I create kura data message with channel with scope "kapua-sys", client id "rpione3" and null payload
    Then I expect the exception "InvalidPayloadException" with the text "Invalid payload: null"
    When I try to translate kura data message to jms message
    And An exception was thrown

  Scenario: Translating kura data message with null channel, and payload without body and with metrics
  Creating kura data message with "null" channel and with valid payload without body and metrics. Trying to translate it into jms message.
  Invalid channel exception should be thrown.

    Given I create kura data message with null channel and payload without body and with metrics
    Then I expect the exception "InvalidChannelException" with the text "Invalid channel: null"
    When I try to translate kura data message to jms message
    And An exception was thrown

  Scenario: Translating invalid kura data message with valid channel, body and metrics into jms message
  Creating "null" kura data message with valid channel, body and metrics. Trying to translate it into jms message.
  Invalid message exception should be thrown.

    Given I create kura data message with channel with scope "kapua-sys", client id "rpione3" and payload with body and metrics
    Then I expect the exception "InvalidMessageException" with the text "Invalid message: null"
    When I try to translate invalid kura data message to jms message

@teardown
  Scenario: Reset Security Context for all scenarios
    Given Reset Security Context
    And An exception was thrown
