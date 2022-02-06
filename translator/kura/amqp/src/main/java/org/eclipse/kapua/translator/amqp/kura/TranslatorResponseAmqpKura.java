/*******************************************************************************
 * Copyright (c) 2021 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.translator.amqp.kura;

import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.service.device.call.message.kura.app.response.KuraResponseChannel;
import org.eclipse.kapua.service.device.call.message.kura.app.response.KuraResponseMessage;
import org.eclipse.kapua.service.device.call.message.kura.app.response.KuraResponsePayload;
import org.eclipse.kapua.translator.Translator;
import org.eclipse.kapua.translator.exception.InvalidChannelException;
import org.eclipse.kapua.translator.exception.InvalidMessageException;
import org.eclipse.kapua.translator.exception.InvalidPayloadException;
import org.eclipse.kapua.translator.exception.TranslateException;
import org.eclipse.kapua.translator.exception.TranslatorErrorCodes;
import org.eclipse.kapua.translator.exception.TranslatorException;
import org.eclipse.kapua.transport.amqp.message.AmqpMessage;
import org.eclipse.kapua.transport.amqp.message.AmqpPayload;
import org.eclipse.kapua.transport.amqp.message.AmqpTopic;

/**
 * Messages translator implementation from {@link AmqpMessage} to {@link KuraResponseMessage}
 * 
 * @since 1.0
 *
 */
public class TranslatorResponseAmqpKura extends Translator<AmqpMessage, KuraResponseMessage> {

    private static final String CONTROL_MESSAGE_CLASSIFIER = SystemSetting.getInstance().getMessageClassifier();

    @Override
    public KuraResponseMessage translate(AmqpMessage amqpMessage)
            throws TranslateException {
        try {
            KuraResponseChannel kuraChannel = translate(amqpMessage.getRequestTopic());
            KuraResponsePayload kuraPayload = translate(amqpMessage.getPayload());
            return new KuraResponseMessage(kuraChannel, amqpMessage.getTimestamp(), kuraPayload);
        } catch (InvalidChannelException | InvalidPayloadException te) {
            throw te;
        } catch (Exception e) {
            throw new InvalidMessageException(e, amqpMessage);
        }
    }

    private KuraResponseChannel translate(AmqpTopic amqpTopic)
            throws TranslateException {
        try {
            String[] amqpTopicTokens = amqpTopic.getSplittedTopic();
            if (amqpTopicTokens.length != 6) {
                throw new TranslatorException(TranslatorErrorCodes.INVALID_CHANNEL, null, (Object) amqpTopicTokens);
            }
            if (!CONTROL_MESSAGE_CLASSIFIER.equals(amqpTopicTokens[0])) {
                throw new TranslatorException(TranslatorErrorCodes.INVALID_CHANNEL, null, amqpTopicTokens[0]);
            }
            KuraResponseChannel kuraResponseChannel = new KuraResponseChannel(amqpTopicTokens[0],
                amqpTopicTokens[1],
                amqpTopicTokens[2]);
            kuraResponseChannel.setAppId(amqpTopicTokens[3]);
            kuraResponseChannel.setReplyPart(amqpTopicTokens[4]);
            kuraResponseChannel.setRequestId(amqpTopicTokens[5]);
            return kuraResponseChannel;
        } catch (Exception e) {
            throw new InvalidChannelException(e, amqpTopic);
        }
    }

    private KuraResponsePayload translate(AmqpPayload amqpPayload)
            throws TranslateException {
        try {
            KuraResponsePayload kuraResponsePayload = new KuraResponsePayload();
            if (amqpPayload.hasBody()) {
                kuraResponsePayload.readFromByteArray(amqpPayload.getBody());
            }
            return kuraResponsePayload;
        } catch (Exception e) {
            throw new InvalidPayloadException(e, amqpPayload);
        }
    }

    @Override
    public Class<AmqpMessage> getClassFrom() {
        return AmqpMessage.class;
    }

    @Override
    public Class<KuraResponseMessage> getClassTo() {
        return KuraResponseMessage.class;
    }
}
