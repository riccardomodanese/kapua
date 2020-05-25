/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.translator.amqp.kura;

import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.message.internal.MessageException;
import org.eclipse.kapua.service.device.call.message.kura.app.response.KuraResponseChannel;
import org.eclipse.kapua.service.device.call.message.kura.app.response.KuraResponseMessage;
import org.eclipse.kapua.service.device.call.message.kura.app.response.KuraResponsePayload;
import org.eclipse.kapua.translator.Translator;
import org.eclipse.kapua.translator.exception.TranslateException;
import org.eclipse.kapua.transport.amqpproton.message.AmqpMessage;
import org.eclipse.kapua.transport.amqpproton.message.AmqpPayload;
import org.eclipse.kapua.transport.amqpproton.message.AmqpTopic;

/**
 * Messages translator implementation from {@link AmqpMessage} to {@link KuraResponseMessage}
 * 
 * @since 1.0
 *
 */
public class TranslatorResponseAmqpKura extends Translator<AmqpMessage, KuraResponseMessage> {

    private static final String CONTROL_MESSAGE_CLASSIFIER = SystemSetting.getInstance().getMessageClassifier();

    @Override
    public KuraResponseMessage translate(AmqpMessage aqmpMessage)
            throws TranslateException {
        // Kura topic
        KuraResponseChannel kuraChannel = translate(aqmpMessage.getRequestTopic());

        // Kura payload
        KuraResponsePayload kuraPayload = translate(aqmpMessage.getPayload());

        // Kura message
        return new KuraResponseMessage(kuraChannel,
                aqmpMessage.getTimestamp(),
                kuraPayload);
    }

    private KuraResponseChannel translate(AmqpTopic aqmpTopic)
            throws TranslateException {
        String[] aqmpTopicTokens = aqmpTopic.getSplittedTopic();

        if (!CONTROL_MESSAGE_CLASSIFIER.equals(aqmpTopicTokens[0])) {
            //TODO EXT-CAMEL why another exception (TranslateException)?
            throw new RuntimeException();
//            throw new TranslatorException(TranslatorErrorCodes.INVALID_CHANNEL_CLASSIFIER,
//                    null,
//                    aqmpTopicTokens[0]);
        }

        KuraResponseChannel kuraResponseChannel = new KuraResponseChannel(aqmpTopicTokens[0],
                aqmpTopicTokens[1],
                aqmpTopicTokens[2]);

        kuraResponseChannel.setAppId(aqmpTopicTokens[3]);
        kuraResponseChannel.setReplyPart(aqmpTopicTokens[4]);
        kuraResponseChannel.setRequestId(aqmpTopicTokens[5]);
        return kuraResponseChannel;
    }

    private KuraResponsePayload translate(AmqpPayload aqmpPayload)
            throws TranslateException {
        byte[] aqmpBody = aqmpPayload.getBody();

        KuraResponsePayload kuraResponsePayload = new KuraResponsePayload();
        try {
            kuraResponsePayload.readFromByteArray(aqmpBody);
        } catch (MessageException e) {
            //TODO EXT-CAMEL why another exception (TranslateException)?
            throw new RuntimeException();
        }
        return kuraResponsePayload;
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
