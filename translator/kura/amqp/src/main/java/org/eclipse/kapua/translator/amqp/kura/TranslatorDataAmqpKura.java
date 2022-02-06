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

import org.eclipse.kapua.message.internal.MessageException;
import org.eclipse.kapua.service.device.call.message.kura.data.KuraDataChannel;
import org.eclipse.kapua.service.device.call.message.kura.data.KuraDataMessage;
import org.eclipse.kapua.service.device.call.message.kura.data.KuraDataPayload;
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
 * Messages translator implementation from {@link AmqpMessage} to {@link org.eclipse.kapua.service.device.call.message.kura.KuraMessage}
 */
public class TranslatorDataAmqpKura extends Translator<AmqpMessage, KuraDataMessage> {

    @Override
    public KuraDataMessage translate(AmqpMessage amqpMessage)
            throws TranslateException {
        try {
            KuraDataChannel kuraChannel = translate(amqpMessage.getRequestTopic());
            KuraDataPayload kuraPayload = translate(amqpMessage.getPayload());
            return new KuraDataMessage(kuraChannel,
                amqpMessage.getTimestamp(),
                kuraPayload);
        } catch (InvalidChannelException | InvalidPayloadException te) {
            throw te;
        } catch (Exception e) {
            throw new InvalidMessageException(e, amqpMessage);
        }
    }

    private KuraDataChannel translate(AmqpTopic amqpTopic)
            throws TranslateException {
        try {
            String[] amqpTopicTokens = amqpTopic.getSplittedTopic();
            if (amqpTopicTokens.length < 2) {
                throw new TranslatorException(TranslatorErrorCodes.INVALID_CHANNEL, null, (Object) amqpTopicTokens);
            }
            KuraDataChannel kuraDataChannel = new KuraDataChannel();
            kuraDataChannel.setScope(amqpTopicTokens[0]);
            kuraDataChannel.setClientId(amqpTopicTokens[1]);
            for (int i = 2; i < amqpTopicTokens.length; i++) {
                kuraDataChannel.getSemanticParts().add(amqpTopicTokens[i]);
            }
            // Return Kura Channel
            return kuraDataChannel;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidChannelException(e, amqpTopic);
        }
    }

    private KuraDataPayload translate(AmqpPayload amqpPayload)
            throws TranslateException {
        try {
            KuraDataPayload kuraDataPayload = new KuraDataPayload();
            if (amqpPayload.hasBody()) {
                byte[] mqttBody = amqpPayload.getBody();
                try {
                    kuraDataPayload.readFromByteArray(mqttBody);
                } catch (MessageException ex) {
                    kuraDataPayload.setBody(mqttBody);
                }
            }
            // Return Kura Payload
            return kuraDataPayload;
        } catch (Exception e) {
            throw new InvalidPayloadException(e, amqpPayload);
        }
    }

    @Override
    public Class<AmqpMessage> getClassFrom() {
        return AmqpMessage.class;
    }

    @Override
    public Class<KuraDataMessage> getClassTo() {
        return KuraDataMessage.class;
    }

}
