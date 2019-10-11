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
package org.eclipse.kapua.broker.client.hono;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.qpid.proton.message.Message;
import org.eclipse.hono.client.ApplicationClientFactory;
import org.eclipse.hono.client.HonoConnection;
import org.eclipse.hono.client.MessageConsumer;
import org.eclipse.hono.config.ClientConfigProperties;
import org.eclipse.hono.util.TimeUntilDisconnectNotification;
import org.eclipse.kapua.broker.client.hono.ClientOptions.HonoClientOptions;
import org.eclipse.kapua.message.transport.TransportMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClientOptions;

public class HonoClient {

    private static final Logger logger = LoggerFactory.getLogger(HonoClient.class);

    private static final String ERROR_MESSAGE_CLIENT_DETACHED = "Hono client - remotely detached consumer link";

    protected ClientOptions clientOptions;
    protected boolean connected;
    protected AtomicInteger reconnectionFaultCount = new AtomicInteger();
    protected Long reconnectTaskId;

    protected Vertx vertx;
    private ApplicationClientFactory clientFactory;
    private Consumer<Message> messageConsumer;

    public HonoClient(Vertx vertx, ClientOptions clientOptions) {
        this.vertx = vertx;
        this.clientOptions = clientOptions;
    }

    public void messageHandler(Consumer<Message> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    public void connect(final Future<Void> connectFuture) {
        String host = clientOptions.getString(HonoClientOptions.HOST);
        Integer port = clientOptions.getInt(HonoClientOptions.PORT, null);
        Objects.requireNonNull(host);
        Objects.requireNonNull(port);
        logger.info("Hono client - Connecting to {}:{}", host, port);
        clientFactory = ApplicationClientFactory.create(HonoConnection.newConnection(vertx, getClientConfigProperties(host, port)));
        final Future<MessageConsumer> consumerFuture = Future.future();

        consumerFuture.setHandler(result -> {
            if (!result.succeeded()) {
                logger.error("clientFactory could not create downstream consumer for [{}:{}]",
                        host, port, result.cause());
                notifyConnectionLost();
            }
        });

        clientFactory.connect()
            .compose(connectedClient -> {
                clientFactory.addDisconnectListener(c -> {
                    logger.info("lost connection to Hono, trying to reconnect ...");
                    notifyConnectionLost();
                });
                clientFactory.addReconnectListener(c -> {
                    logger.info("reconnected to Hono");
                });
                return createConsumer();
            })
            .setHandler(consumerFuture);
    }

    public void disconnect(final Future<Void> closeFuture) {
        if(clientFactory!=null) {
            clientFactory.disconnect(event -> {
                logger.info("Hono client - closing connection {}", event);
                if (!closeFuture.isComplete()) {
                    closeFuture.complete();
                }
            });
        }
    }

    protected void notifyConnectionLost() {
        logger.info("Notify disconnection...");
        if (reconnectTaskId == null) {
            if (reconnectTaskId == null) {
                long backOff = evaluateBackOff();
                logger.info("Notify disconnection... Start new task {}", backOff);
                reconnectTaskId = vertx.setTimer(backOff, new Handler<Long>() {

                    @Override
                    public void handle(Long obj) {
                        Future<Void> future = Future.future();
                        future.setHandler(result -> {
                            reconnectTaskId = null;
                            if (result.succeeded()) {
                                logger.info("Establish connection retry {}... SUCCESS", reconnectionFaultCount.get());
                                reconnectionFaultCount.set(0);
                            } else {
                                logger.info("Establish connection retry {}... FAILURE", reconnectionFaultCount.get(), result.cause());
                                if (reconnectionFaultCount.incrementAndGet() > clientOptions.getInt(HonoClientOptions.MAXIMUM_RECONNECTION_ATTEMPTS, -1)
                                        && clientOptions.getInt(HonoClientOptions.MAXIMUM_RECONNECTION_ATTEMPTS, -1)>-1) {
                                    logger.error("Maximum reconnection attempts reached. Exiting...");
                                    System.exit(clientOptions.getInt(HonoClientOptions.MAXIMUM_RECONNECTION_ATTEMPTS, -1));
                                };
                                //schedule a new task
                                notifyConnectionLost();
                            }
                        });
                        connect(future);
                        logger.info("Started new connection down");
                    }
                });
            }
            else {
                logger.info("Another reconnect operation is enqueed. No action will be taken!");
            }
        }
        else {
            logger.info("Another reconnect operation is enqueed. No action will be taken!");
        }
        logger.info("Notify disconnection... DONE");
    }

    private long evaluateBackOff() {
        Long waitBetweenReconnect = clientOptions.getLong(HonoClientOptions.WAIT_BETWEEN_RECONNECT, 1l);
        return (1 + reconnectionFaultCount.get()) * waitBetweenReconnect + (long)((double)waitBetweenReconnect * Math.random());
    }

    protected ClientConfigProperties getClientConfigProperties(String host, int port) {
        ClientConfigProperties props = new ClientConfigProperties();
        props.setHost(host);
        props.setPort(port);
        props.setUsername(clientOptions.getString(HonoClientOptions.USERNAME));
        props.setPassword(clientOptions.getString(HonoClientOptions.PASSWORD));
        props.setTrustStorePath(clientOptions.getString(HonoClientOptions.TRUSTSTORE_FILE));
        props.setHostnameVerificationRequired(false);
        props.setName(clientOptions.getString(HonoClientOptions.NAME));
        logger.info("Setting Hono connection parameters:\n\tHost: {}\n\tPort: {}\n\tUsername: {}\n\tMessage type: {}\n\tTrust Store Path: {}\n\tName: {}",
                props.getHost(), props.getPort(), props.getUsername(), clientOptions.get(HonoClientOptions.MESSAGE_TYPE), props.getTrustStorePath(), props.getName());
        return props;
    }

    protected ProtonClientOptions getProtonClientOptions() {
        ProtonClientOptions opts = new ProtonClientOptions();
        Integer connectTimeout = clientOptions.getInt(HonoClientOptions.CONNECT_TIMEOUT, 10000);//milliseconds
        Integer idleTimeout = clientOptions.getInt(HonoClientOptions.IDLE_TIMEOUT, 30);//no activity for t>idleTimeout will close the connection (in seconds)
        Long waitBetweenReconnect = clientOptions.getLong(HonoClientOptions.WAIT_BETWEEN_RECONNECT, 10000l);//in milliseconds
        int heartbeat = idleTimeout * 1000 / 2;
        logger.info("Setting Proton connection parameters:\n\tConnect Timeout: {}[ms]\n\tIdle Timeout: {}[s]\n\tHeartbeat: {}[ms]\n\tReconnect Interval: {}[ms]\n\tReconnect Attempts: 1",
                connectTimeout, idleTimeout, heartbeat, waitBetweenReconnect);
        //check if zero disables the timeout and heartbeat
        //no activity for t>2*heartbeat will close connection (in milliseconds)
        //the reconnect attempts are managed externally
        opts.setConnectTimeout(connectTimeout)
            .setIdleTimeout(idleTimeout)
            .setHeartbeat(heartbeat)
            .setReconnectAttempts(1)
            .setReconnectInterval(waitBetweenReconnect);

        //TODO do we need to set some other parameter?
        return opts;
    }

    protected void handleCommandReadinessNotification(final TimeUntilDisconnectNotification notification) {
    }

    private void consumeMessage(Message message) {
        messageConsumer.accept(message);
    }

    private final Future<MessageConsumer> createConsumer() {
        String tenantId = clientOptions.getString(HonoClientOptions.TENANT_ID);
        return createMessageConsumer(tenantId).compose(telemetryMessageConsumer -> {
            logger.info("Consumer ready for telemetry messages.");
            return Future.succeededFuture(telemetryMessageConsumer);
        }).recover(t ->
            Future.failedFuture(t)
        );
    }

    private Future<MessageConsumer> createMessageConsumer(String tenantId) {
        TransportMessageType transportMessageType = (TransportMessageType)clientOptions.get(HonoClientOptions.MESSAGE_TYPE);
        switch (transportMessageType) {
        case TELEMETRY:
            return clientFactory.createTelemetryConsumer(tenantId,
                    this::consumeMessage,
                    closeHook -> logger.error("remotely detached consumer link"));
        case EVENTS:
            return clientFactory.createEventConsumer(tenantId,
                    this::consumeMessage,
                    closeHook -> logger.error("remotely detached consumer link"));
        default:
            throw new RuntimeException(String.format("Invalid consumer %s type! (Allowed [TELEMETRY, EVENT])", transportMessageType));
        }
    }

}
