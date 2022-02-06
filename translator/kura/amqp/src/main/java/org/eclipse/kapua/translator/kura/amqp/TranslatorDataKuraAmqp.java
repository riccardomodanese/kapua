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
package org.eclipse.kapua.translator.kura.amqp;

import org.eclipse.kapua.service.device.call.message.kura.data.KuraDataChannel;
import org.eclipse.kapua.service.device.call.message.kura.data.KuraDataMessage;
import org.eclipse.kapua.service.device.call.message.kura.data.KuraDataPayload;
import org.eclipse.kapua.translator.Translator;
import org.eclipse.kapua.translator.exception.InvalidChannelException;
import org.eclipse.kapua.translator.exception.InvalidMessageException;
import org.eclipse.kapua.translator.exception.InvalidPayloadException;
import org.eclipse.kapua.translator.exception.TranslateException;
import org.eclipse.kapua.transport.amqp.message.AmqpMessage;
import org.eclipse.kapua.transport.amqp.message.AmqpPayload;
import org.eclipse.kapua.transport.amqp.message.AmqpTopic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Messages translator implementation from {@link org.eclipse.kapua.service.device.call.message.kura.KuraMessage} to {@link AmqpMessage}
 */
public class TranslatorDataKuraAmqp extends Translator<KuraDataMessage, AmqpMessage> {

    @Override
    public AmqpMessage translate(KuraDataMessage kuraDataMessage)
            throws TranslateException {
        // Return Amqp message
        try {
            AmqpTopic amqpRequestTopic = translate(kuraDataMessage.getChannel());
            AmqpPayload amqpPayload = translate(kuraDataMessage.getPayload());
            return new AmqpMessage(amqpRequestTopic,
                new Date(),
                amqpPayload);
        } catch (InvalidChannelException | InvalidPayloadException te) {
            throw te;
        } catch (Exception e) {
            throw new InvalidMessageException(e, kuraDataMessage);
        }
    }

    private AmqpTopic translate(KuraDataChannel kuraDataChannel)
            throws TranslateException {
        try {
            List<String> topicTokens = new ArrayList<>();
            topicTokens.add(kuraDataChannel.getScope());
            topicTokens.add(kuraDataChannel.getClientId());
            if (!kuraDataChannel.getSemanticParts().isEmpty()) {
                topicTokens.addAll(kuraDataChannel.getSemanticParts());
            }
            return new AmqpTopic(topicTokens.toArray(new String[0]));
        } catch (Exception e) {
            throw new InvalidChannelException(e, kuraDataChannel);
        }
    }

    private AmqpPayload translate(KuraDataPayload kuraDataPayload)
            throws TranslateException {
        try {
            return new AmqpPayload(kuraDataPayload.toByteArray());
        } catch (Exception e) {
            throw new InvalidPayloadException(e, kuraDataPayload);
        }
    }

    @Override
    public Class<KuraDataMessage> getClassFrom() {
        return KuraDataMessage.class;
    }

    @Override
    public Class<AmqpMessage> getClassTo() {
        return AmqpMessage.class;
    }

}
