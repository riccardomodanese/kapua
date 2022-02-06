/*******************************************************************************
 * Copyright (c) 2020, 2021 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kapua.translator.test.steps;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.qa.common.StepData;
import org.eclipse.kapua.qa.common.TestBase;
import org.eclipse.kapua.service.device.call.message.kura.KuraPayload;
import org.eclipse.kapua.service.device.call.message.kura.app.response.KuraResponseMessage;
import org.eclipse.kapua.service.device.call.message.kura.data.KuraDataChannel;
import org.eclipse.kapua.service.device.call.message.kura.data.KuraDataMessage;
import org.eclipse.kapua.service.device.call.message.kura.data.KuraDataPayload;
import org.eclipse.kapua.translator.Translator;
import org.eclipse.kapua.translator.amqp.kura.TranslatorDataAmqpKura;
import org.eclipse.kapua.translator.amqp.kura.TranslatorResponseAmqpKura;
import org.eclipse.kapua.translator.jms.kura.TranslatorDataJmsKura;
import org.eclipse.kapua.translator.kura.amqp.TranslatorDataKuraAmqp;
import org.eclipse.kapua.translator.kura.jms.TranslatorDataKuraJms;
import org.eclipse.kapua.transport.amqp.message.AmqpMessage;
import org.eclipse.kapua.transport.amqp.message.AmqpPayload;
import org.eclipse.kapua.transport.amqp.message.AmqpTopic;
import org.eclipse.kapua.transport.amqp.setting.AmqpClientSetting;
import org.eclipse.kapua.transport.amqp.setting.AmqpClientSettingKeys;
import org.eclipse.kapua.transport.message.jms.JmsMessage;
import org.eclipse.kapua.transport.message.jms.JmsPayload;
import org.eclipse.kapua.transport.message.jms.JmsTopic;
import org.junit.Assert;

import com.google.inject.Singleton;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Implementation of Gherkin steps used in TranslatorUnitTests.feature scenarios.
 */
@Singleton
public class TranslatorSteps extends TestBase {

    private static final String TOPIC_SEPARATOR = AmqpClientSetting.getInstance().getString(AmqpClientSettingKeys.TRANSPORT_TOPIC_SEPARATOR);

    private ExampleTranslator exampleTranslator;
    private TranslatorDataAmqpKura translatorDataAqmpKura;
    private TranslatorResponseAmqpKura translatorResponseAmqpKura;
    private TranslatorDataKuraAmqp translatorDataKuraAmqp;
    private TranslatorDataJmsKura translatorDataJmsKura;
    private TranslatorDataKuraJms translatorDataKuraJms;

    @Inject
    public TranslatorSteps(StepData stepData) {
        super(stepData);
        exampleTranslator = new ExampleTranslator();
        translatorDataAqmpKura = new TranslatorDataAmqpKura();
        translatorResponseAmqpKura = new TranslatorResponseAmqpKura();
        translatorDataKuraAmqp = new TranslatorDataKuraAmqp();
        translatorDataJmsKura = new TranslatorDataJmsKura();
        translatorDataKuraJms = new TranslatorDataKuraJms();
    }

    // *************************************
    // Definition of Cucumber scenario steps
    // *************************************

    @Before
    public void beforeScenarioDockerFull(Scenario scenario) {
        updateScenario(scenario);
    }

    @Given("I try to translate from {string} to {string}")
    public void iFindTranslator(String from, String to) throws Exception {
        Class fromClass;
        Class toClass;
        try {
            if (!from.equals("") && !to.equals("")) {
                fromClass = Class.forName(from);
                toClass = Class.forName(to);
            } else {
                fromClass = null;
                toClass = null;
            }
            Translator translator = Translator.getTranslatorFor(exampleTranslator.getClass(fromClass), exampleTranslator.getClass(toClass));
            stepData.put("Translator", translator);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    @Then("Translator {string} is found")
    public void translatorIsFound(String translatorName) {
        Translator translator = (Translator) stepData.get("Translator");
        Assert.assertEquals(translatorName, translator.getClass().getSimpleName());
    }

    @Given("I create amqp message with (valid/invalid/empty) payload {string} and (valid/invalid) topic {string}")
    public void creatingAmqpMessage(String payload, String topic) throws Exception{
        try {
            Date date = new Date();
            AmqpTopic amqpTopic = new AmqpTopic(topic);
            KuraPayload kuraPayload = new KuraPayload();
            if (payload.equals("invalidPayload") || payload.equals("")) {
                kuraPayload.setBody(payload.getBytes());
            } else {
                kuraPayload.getMetrics().put(payload, 200);
            }
            AmqpPayload amqpPayload = new AmqpPayload(kuraPayload.toByteArray());
            AmqpMessage amqpMessage = new AmqpMessage(amqpTopic, date, amqpPayload);
            stepData.put("AmqpMessage", amqpMessage);
        } catch (Exception ex){
            verifyException(ex);
        }
    }

    @When("I try to translate amqp response")
    public void iTryToTranslateAmqpResponse() throws Exception {
        AmqpMessage amqpMessage = (AmqpMessage) stepData.get("AmqpMessage");
        try {
            KuraResponseMessage kuraResponseMessage = translatorResponseAmqpKura.translate(amqpMessage);
            stepData.put("KuraResponseMessage", kuraResponseMessage);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("I got kura response message with {string} payload body")
    public void kuraResponseMessageWithPayloadBody(String payloadType) {
        KuraResponseMessage kuraResponseMessage = (KuraResponseMessage) stepData.get("KuraResponseMessage");
        Assert.assertTrue(kuraResponseMessage.getPayload().getBody().getClass().getSimpleName().equals(payloadType));
    }

    @Then("I got kura response message with proper payload metrics")
    public void kuraResponseMessageWithPayloadAndChannelAndData() {
        KuraResponseMessage kuraResponseMessage = (KuraResponseMessage) stepData.get("KuraResponseMessage");
        Assert.assertTrue(kuraResponseMessage.getPayload().getMetrics() != null);
    }

    @Given("I create kura data message with channel with scope {string}, client id {string} and payload without body and metrics")
    public void iCreateKuraDataMessage(String scope, String clientId) throws Exception {
        try {
            KuraDataChannel kuraDataChannel = new KuraDataChannel(scope, clientId);
            Date date = new Date();
            KuraDataPayload kuraDataPayload = new KuraDataPayload();
            KuraDataMessage kuraDataMessage = new KuraDataMessage(kuraDataChannel, date, kuraDataPayload);
            stepData.put("KuraDataMessage", kuraDataMessage);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    @And("I try to translate kura data message to amqp message")
    public void iTryToTranslateKuraDataMessageToAmqpMessage() throws Exception {
        try {
            KuraDataMessage kuraDataMessage = (KuraDataMessage) stepData.get("KuraDataMessage");
            AmqpMessage amqpMessage = translatorDataKuraAmqp.translate(kuraDataMessage);
            stepData.put("AmqpMessage", amqpMessage);
        } catch (Exception ex) {
            verifyException(ex);
        }

    }

    @Then("I get amqp message with channel with scope {string}, client id {string} and (empty body|non empty body)")
    public void amqpMessageWithChannelScopeClientIDAndBody(String scope, String clientId) {
        AmqpMessage amqpMessage = (AmqpMessage) stepData.get("AmqpMessage");
        String requestTopic = scope.concat(TOPIC_SEPARATOR + clientId);
        Assert.assertEquals(requestTopic, amqpMessage.getRequestTopic().getTopic());
        if (amqpMessage.getPayload().getBody().length == 0) {
            Assert.assertTrue(amqpMessage.getPayload().getBody().length == 0);
        } else {
            Assert.assertTrue(amqpMessage.getPayload().getBody().length != 0);
        }
    }

    @And("I got kura response message channel with {string}, {string}, {string}, {string}, {string} and {string} data")
    public void kuraResponseMessageWithChannelAndData(String replyPart, String requestId, String appId, String messageClassification, String scope, String clientId) {
        KuraResponseMessage kuraResponseMessage = (KuraResponseMessage) stepData.get("KuraResponseMessage");
        Assert.assertTrue(kuraResponseMessage.getChannel().getReplyPart().equals(replyPart));
        Assert.assertTrue(kuraResponseMessage.getChannel().getRequestId().equals(requestId));
        Assert.assertTrue(kuraResponseMessage.getChannel().getAppId().equals(appId));
        Assert.assertTrue(kuraResponseMessage.getChannel().getMessageClassification().equals(messageClassification));
        Assert.assertTrue(kuraResponseMessage.getChannel().getScope().equals(scope));
        Assert.assertTrue(kuraResponseMessage.getChannel().getClientId().equals(clientId));
    }

    @Given("I create kura data message with channel with scope {string}, client id {string}, valid payload and metrics but without body")
    public void kuraDataMessageWithoutBodyAndMetrics(String scope, String clientId) throws Exception {
        try {
            Date date = new Date();
            KuraDataChannel kuraDataChannel = new KuraDataChannel(scope, clientId);
            KuraDataPayload kuraDataPayload = new KuraDataPayload();
            kuraDataPayload.getMetrics().put("response.code", 200);
            KuraDataMessage kuraDataMessage = new KuraDataMessage(kuraDataChannel, date, kuraDataPayload);
            stepData.put("KuraDataMessage", kuraDataMessage);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    @Given("I create kura data message with channel with scope {string}, client id {string} and payload with body and metrics")
    public void fullKuraDataMessage(String scope, String clientId) throws Exception {
        try {
            Date date = new Date();
            KuraDataChannel kuraDataChannel = new KuraDataChannel(scope, clientId);
            KuraDataPayload kuraDataPayload = new KuraDataPayload();
            kuraDataPayload.setBody("Payload".getBytes());
            kuraDataPayload.getMetrics().put("response.code", 200);
            KuraDataMessage kuraDataMessage = new KuraDataMessage(kuraDataChannel, date, kuraDataPayload);
            stepData.put("KuraDataMessage", kuraDataMessage);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    @Given("I try to translate amqp message to kura data message")
    public void iTryToTranslateAmqpMessageToKuraMessage() throws Exception {
        try {
            AmqpMessage amqpMessage = (AmqpMessage) stepData.get("AmqpMessage");
            KuraDataMessage kuraDataMessage = translatorDataAqmpKura.translate(amqpMessage);
            stepData.put("KuraDataMessage", kuraDataMessage);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    @Then("I got kura data message with {string} payload body")
    public void iGotKuraDataMessageWithPayloadBody(String payloadType) throws Throwable {
        KuraDataMessage kuraDataMessage = (KuraDataMessage) stepData.get("KuraDataMessage");
        Assert.assertTrue(kuraDataMessage.getPayload().getBody().getClass().getSimpleName().equals(payloadType));
    }

    @And("I got kura data message channel with {string} and {string} data")
    public void iGotKuraDataMessageChannelWithAndData(String scope, String clientId) {
        KuraDataMessage kuraDataMessage = (KuraDataMessage) stepData.get("KuraDataMessage");
        Assert.assertTrue(kuraDataMessage.getChannel().getScope().equals(scope));
        Assert.assertTrue(kuraDataMessage.getChannel().getClientId().equals(clientId));
    }

    @Then("I got kura data message with proper payload metrics response code {int}")
    public void iGotKuraDataMessageWithProperPayloadMetrics(int responseCode) {
        KuraDataMessage kuraDataMessage = (KuraDataMessage) stepData.get("KuraDataMessage");
        Assert.assertEquals(kuraDataMessage.getPayload().getMetrics().get("response.code"), responseCode);
    }

    @Then("I got kura data message with empty payload")
    public void iGotKuraDataMessageWithEmptyPayload() {
        KuraDataMessage kuraDataMessage = (KuraDataMessage) stepData.get("KuraDataMessage");
        Assert.assertEquals(null, kuraDataMessage.getPayload().getBody());
    }

    @Given("I create jms message with (valid|invalid|empty) payload {string} and (valid|invalid) topic {string}")
    public void iCreateJmsMessageWithInvalidPayloadAndInvalidTopic(String payload, String topic) throws Exception {
        try {
            Date date = new Date();
            JmsTopic jmsTopic = new JmsTopic(topic);
            KuraPayload kuraPayload = new KuraPayload();
            if (payload.equals("invalidPayload") || payload.equals("")) {
                kuraPayload.setBody(payload.getBytes());
            } else {
                kuraPayload.getMetrics().put(payload, 200);
            }
            JmsPayload jmsPayload = new JmsPayload(kuraPayload.toByteArray());
            JmsMessage jmsMessage = new JmsMessage(jmsTopic, date, jmsPayload);
            stepData.put("JmsMessage", jmsMessage);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    @And("I try to translate jms message to kura data message")
    public void iTryToTranslateJmsMessageToKuraMessage() throws Exception {
        JmsMessage jmsMessage = (JmsMessage) stepData.get("JmsMessage");
        try {
            KuraDataMessage kuraDataMessage = translatorDataJmsKura.translate(jmsMessage);
            stepData.put("KuraDataMessage", kuraDataMessage);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @And("I try to translate kura data message to jms message")
    public void iTryToTranslateKuraDataMessageToJmsMessage() throws Exception {
        try {
            KuraDataMessage kuraDataMessage = (KuraDataMessage) stepData.get("KuraDataMessage");
            JmsMessage jmsMessage = translatorDataKuraJms.translate(kuraDataMessage);
            stepData.put("JmsMessage", jmsMessage);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    @Then("I got kura data message channel with {string} scope, {string} client id and proper semanticPart")
    public void iCreateJmsMessageWithInvalidPayloadAndTopic(String scope, String clientId, List<String> semanticParts) {
        KuraDataMessage kuraDataMessage = (KuraDataMessage) stepData.get("KuraDataMessage");
        Assert.assertEquals(scope, kuraDataMessage.getChannel().getScope());
        Assert.assertEquals(clientId, kuraDataMessage.getChannel().getClientId());
        for (String semanticPart : semanticParts) {
            Assert.assertTrue(kuraDataMessage.getChannel().getSemanticParts().contains(semanticPart));
        }
    }

    @Then("I got jms message with topic {string} and (empty body|non empty body)")
    public void iGotJmsMessageWithTopicAndEmptyPayload(String topic) {
        JmsMessage jmsMessage = (JmsMessage) stepData.get("JmsMessage");
        Assert.assertEquals(new JmsTopic(topic).getTopic(), jmsMessage.getTopic().getTopic());
        if (jmsMessage.getPayload().getBody().length == 0) {
            Assert.assertTrue(jmsMessage.getPayload().getBody().length == 0);
        } else {
            Assert.assertTrue(jmsMessage.getPayload().getBody().length != 0);
        }
    }

    @When("I try to translate amqp null message to kura data message")
    public void iTryToTranslateAmqpNullMessageToKuraDataMessage() throws Exception {
        try {
            AmqpMessage amqpMessage = (AmqpMessage) stepData.get("AmqpMessage");
            KuraDataMessage kuraDataMessage = translatorDataAqmpKura.translate((AmqpMessage) null);
            stepData.put("KuraDataMessage", kuraDataMessage);
        } catch (Exception ex){
            verifyException(ex);
        }
    }

    @Given("I create kura data message with channel with scope {string}, client id {string} and null payload")
    public void iCreateKuraDataMessageWithChannelWithScopeClientIdAndNullPayload(String scope, String clientId) {
        KuraDataChannel kuraDataChannel = new KuraDataChannel(scope, clientId);
        Date date = new Date();
        KuraDataMessage kuraDataMessage = new KuraDataMessage(kuraDataChannel, date, null);
        stepData.put("KuraDataMessage", kuraDataMessage);
    }

    @Given("I create kura data message with null channel and payload without body and with metrics")
    public void iCreateKuraDataMessageWithNullChannelAndPayloadWithoutBodyAndWithMetrics() {
        Date date = new Date();
        KuraDataPayload kuraDataPayload = new KuraDataPayload();
        kuraDataPayload.getMetrics().put("response.code", 200);
        KuraDataMessage kuraDataMessage = new KuraDataMessage(null, date, kuraDataPayload);

        stepData.put("KuraDataMessage", kuraDataMessage);
    }

    @And("I try to translate invalid kura data message to amqp message")
    public void iTryToTranslateInvalidKuraDataMessageToAmqpMessage() throws Exception {
        try {
            KuraDataMessage kuraDataMessage = (KuraDataMessage) stepData.get("KuraDataMessage");
            AmqpMessage amqpMessage = translatorDataKuraAmqp.translate((KuraDataMessage) null);
            stepData.put("AmqpMessage", amqpMessage);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    @When("I try to translate invalid jms message to kura data message")
    public void iTryToTranslateInvalidJmsMessageToKuraDataMessage() throws Exception{
        try {
            KuraDataMessage kuraDataMessage = translatorDataJmsKura.translate((JmsMessage) null);
            stepData.put("KuraDataMessage", kuraDataMessage);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("I try to translate invalid kura data message to jms message")
    public void iTryToTranslateInvalidKuraDataMessageToJmsMessage() throws Exception {
        try {
            JmsMessage jmsMessage = translatorDataKuraJms.translate((KuraDataMessage) null);
            stepData.put("JmsMessage", jmsMessage);
        } catch (Exception ex){
            verifyException(ex);
        }
    }
}
