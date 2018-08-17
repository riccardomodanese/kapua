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
package org.eclipse.kapua.broker.client.amqp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.broker.client.amqp.proton.ClientOptions;
import org.eclipse.kapua.broker.client.amqp.proton.ClientOptions.AmqpClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmqpClientPublisher extends BaseHandler {

    private static final Logger logger = LoggerFactory.getLogger(AmqpClientPublisher.class);

    private final static String VT_PREFIX = "topic://VirtualTopic.";
    private Thread threadClient;
    protected boolean connected;
    protected AtomicInteger reconnectionFaultCount = new AtomicInteger();
    protected Long reconnectTaskId;
    private ClientOptions options;
    private String clientId;
    private Reactor r;
    private Connection conn;
    private Session ssn;

    //internal fields used by the publisher
    private int nextTag;
    protected ByteArrayOutputStream current = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];

    public AmqpClientPublisher(ClientOptions options) throws IOException {
        this.options = options;
        clientId = options.getString(AmqpClientOptions.CLIENT_ID) + "_snd";
        add(new Handshaker());
        add(new FlowController());
        r = Proton.reactor(this);
    }

    @Override
    public void onReactorInit(Event event) {
        conn = event.getReactor().connectionToHost(options.getString(AmqpClientOptions.BROKER_HOST), options.getInt(AmqpClientOptions.BROKER_PORT, 5673), this);
    }

    @Override
    public void onConnectionInit(Event event) {
        conn = event.getConnection();
        conn.setContainer(clientId);
        conn.open();

        ssn = conn.session();
        ssn.open();
        connected = true;
    }

    @Override
    public void onTransportError(Event event) {
        ErrorCondition condition = event.getTransport().getCondition();
        if (condition != null) {
            logger.error("Error: " + condition.getDescription());
        } else {
            logger.error("Error (no description returned).");
        }
    }

    public void send(Message message, String destination) throws KapuaException {
        Sender snd = ssn.sender(clientId + "_" + UUID.randomUUID().toString() + "_snd");
        Target target = new Target();
        target.setAddress(VT_PREFIX + destination.replaceAll("/", "."));
        snd.setTarget(target);
        Source source = new Source();
        source.setAddress(VT_PREFIX + destination.replaceAll("/", "."));
        snd.setSource(source);
        message.setAddress(message.getAddress());
        snd.open();

        int bufferSize = 1024;
        byte[] encodedMessage = new byte[bufferSize];
        MessageImpl msg = (MessageImpl) message;
        int len = msg.encode2(encodedMessage, 0, bufferSize);

        // looks like the message is bigger than our initial buffer, lets resize and try again.
        if (len > encodedMessage.length) {
          encodedMessage = new byte[len];
          msg.encode(encodedMessage, 0, len);
        }
        byte[] tag = String.valueOf(nextTag++).getBytes();
        Delivery dlv = snd.delivery(tag);
        snd.send(encodedMessage, 0, len);
        dlv.settle();
        snd.advance();
        snd.close();
    }

    @Override
    public void onLinkLocalClose(Event e) {
        super.onLinkLocalClose(e);
        notifyConnectionLost();
        logger.debug("onLinkLocalClose! {}", e);
    }

    @Override
    public void onLinkLocalDetach(Event e) {
        super.onLinkLocalDetach(e);
        notifyConnectionLost();
        logger.debug("onLinkLocalDetach! {}", e);
    }

    @Override
    public void onLinkRemoteOpen(Event e) {
        super.onLinkRemoteOpen(e);
        logger.debug("onLinkRemoteOpen! {}", e);
    }

    @Override
    public void onLinkRemoteDetach(Event e) {
        super.onLinkRemoteDetach(e);
        notifyConnectionLost();
        logger.debug("onLinkRemoteDetach! {}", e);
    }

    @Override
    public void onLinkRemoteClose(Event e) {
        super.onLinkRemoteClose(e);
        notifyConnectionLost();
        logger.debug("onLinkRemoteClose! {}", e);
    }

    @Override
    public void onConnectionLocalClose(Event e) {
        super.onConnectionLocalClose(e);
        notifyConnectionLost();
        logger.debug("onConnectionLocalClose! {}", e);
    }

    @Override
    public void onConnectionRemoteClose(Event e) {
        super.onConnectionRemoteClose(e);
        notifyConnectionLost();
        logger.debug("onConnectionRemoteClose! {}", e);
    }

    @Override
    public void onConnectionLocalOpen(Event e) {
        super.onConnectionLocalOpen(e);
        logger.debug("onConnectionLocalOpen! {}", e);
    }

    @Override
    public void onConnectionRemoteOpen(Event e) {
        super.onConnectionRemoteOpen(e);
        logger.debug("onConnectionRemoteOpen! {}", e);
    }

    @Override
    public void onConnectionBound(Event e) {
        super.onConnectionBound(e);
        logger.debug("onConnectionBound! {}", e);
    }

    @Override
    public void onConnectionFinal(Event e) {
        super.onConnectionFinal(e);
        logger.debug("onConnectionFinal! {}", e);
    }

  @Override
  public void onLinkLocalOpen(Event e) {
      super.onLinkLocalOpen(e);
      logger.debug("onLinkLocalOpen! {}", e);
  }

    public boolean isConnected() {
        return true;
    }

    protected void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void clean() {
    }

    public void disconnect() {
        if (ssn!=null) {
            ssn.close();
        }
        if (conn!=null) {
            conn.close();
        }
        if (r != null) {
            r.stop();
        }
        synchronized (this) {
            threadClient = null;
        }
    }

    public void connect() {
        synchronized (this) {
            if (threadClient!=null) {
                KapuaException.internalError("Cannot start the client since another instance is still running!");
            }
            else {
                threadClient = new Thread(() -> {
                    r.run();
                });
                threadClient.start();
            }
        }
    }

    protected void notifyConnectionLost() {
        connected = false;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        disconnect();
    }
}
