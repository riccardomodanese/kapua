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
package org.eclipse.kapua.broker.client.amqp.proton;

import java.io.IOException;

import org.apache.qpid.proton.message.Message;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.broker.client.amqp.AmqpClientPublisher;
import org.eclipse.kapua.broker.client.amqp.AmqpClientSubscriber;
import org.eclipse.kapua.broker.client.amqp.proton.ClientOptions.AmqpClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmqpClient {

    private static final Logger logger = LoggerFactory.getLogger(AmqpClient.class);

    private String clientId;
    private AmqpClientPublisher publisher;
    private AmqpClientSubscriber subscriber;

    public AmqpClient(ClientOptions options) throws IOException {
        this.clientId = options.getString(AmqpClientOptions.CLIENT_ID);
        publisher = new AmqpClientPublisher(options);
        subscriber = new AmqpClientSubscriber(options);
    }

    public String getClientId() {
        return clientId;
    }

    public void send(Message message, String destination) throws KapuaException {
        publisher.send(message, destination);
    }

    public void subscribe(String destination, ConsumerHandler consumerHandler) {
        subscriber.subscribe(destination, consumerHandler);
    }

    public boolean isConnected() {
        return true;
//        return connected;
    }

    public void clean() {
//        logger.info("Clean client {} - snd {} - rec {}", clientId, snd, rec);
//        if (snd!=null) {
//            snd.close();
//            //no needing to call free() for both receiver and sender since it's called implicitly by the close method!
//            //snd.free();
//            snd = null;
//        }
//        if (rec!=null) {
//            //no needing to call free() for both receiver and sender since it's called implicitly by the close method!
//            //rec.free();
//            rec.close();
//            rec = null;
//        }
    }

    public void disconnect() {
        publisher.disconnect();
        subscriber.disconnect();
    }

    public void connect() {
        publisher.connect();
        subscriber.connect();
    }

}
