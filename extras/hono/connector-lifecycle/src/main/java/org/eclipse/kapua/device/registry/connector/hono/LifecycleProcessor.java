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
package org.eclipse.kapua.device.registry.connector.hono;

import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.kapua.KapuaErrorCodes;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.connector.MessageContent;
import org.eclipse.kapua.connector.MessageContext;
import org.eclipse.kapua.connector.MessageTarget;
import org.eclipse.kapua.connector.Properties;
import org.eclipse.kapua.connector.hono.SystemMessageUtil;
import org.eclipse.kapua.message.transport.TransportMessage;
import org.eclipse.kapua.model.id.KapuaId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class LifecycleProcessor implements MessageTarget<TransportMessage> {

    private static final Logger logger = LoggerFactory.getLogger(LifecycleProcessor.class);

    private static final String INVALID_TOPIC = "Cannot detect destination!";

    private Vertx vertx;

    private LifecycleListener lifecycleListener;

    public enum LifecycleTypes {
        APPS,
        BIRTH,
        DC,
        MISSING
    }

    public static LifecycleProcessor create(Vertx vertx) {
        return new LifecycleProcessor(vertx);
    }

    protected LifecycleProcessor(Vertx vertx) {
        this.vertx = vertx;
        lifecycleListener = new LifecycleListener();
    }

    @Override
    public void start(Future<Void> startFuture) {
        // nothing to do
        startFuture.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        // nothing to do
        stopFuture.complete();
    }

    @Override
    public void process(MessageContext<TransportMessage> message, Handler<AsyncResult<Void>> result) {
        MessageContent messageContent = (MessageContent)message.getProperties().get(Properties.MESSAGE_CONTENT);
        if (MessageContent.SYSTEM.equals(messageContent)) {
            result.handle(Future.failedFuture(new KapuaException(KapuaErrorCodes.INTERNAL_ERROR, "Cannot handle SYSTEM messages!")));
        }
        else {
            List<String> destination = message.getMessage().getChannel().getSemanticParts();
            if (destination!=null && destination.size()>1) {
                String messageType = destination.get(1);
                LifecycleTypes token = null;
                try {
                    token = LifecycleTypes.valueOf(messageType);
                }
                catch (IllegalArgumentException | NullPointerException e) {
                    logger.debug("Invalid message type ({})", messageType);
                    result.handle(Future.failedFuture(INVALID_TOPIC));
                    return;
                }
                switch (token) {
                    case APPS:
                        execute(message, result, () -> {
                            KapuaId connectionId = SystemMessageUtil.checkDeviceConnectionId(message);
                            lifecycleListener.processAppsMessage(message, connectionId);
                            return (Void) null;
                        });
                        break;
                    case BIRTH:
                        execute(message, result, () -> {
                            KapuaId connectionId = SystemMessageUtil.createOrUpdateDeviceConnection(message);
                            lifecycleListener.processBirthMessage(message, connectionId);
                            return (Void) null;
                        });
                        break;
                    case DC:
                        execute(message, result, () -> {
                            KapuaId connectionId = SystemMessageUtil.disconnectDeviceConnection(message);
                            lifecycleListener.processDisconnectMessage(message, connectionId);
                            return (Void) null;
                        });
                        break;
                    case MISSING:
                        execute(message, result, () -> {
                            KapuaId connectionId = SystemMessageUtil.checkDeviceConnectionId(message);
                            lifecycleListener.processMissingMessage(message, connectionId);
                            return (Void) null;
                        });
                        break;
                    default:
                        result.handle(Future.succeededFuture());
                        break;
                }
            }
            else {
                result.handle(Future.failedFuture(INVALID_TOPIC));
            }
        }
    }

    private void execute(MessageContext<TransportMessage> message, Handler<AsyncResult<Void>> result, Callable<Void> innerCall) {
        vertx.executeBlocking(fut -> {
            try {
                innerCall.call();
                fut.complete();
            } catch (Exception e) {
                fut.fail(e);
            }
        }, ar -> {
            if (ar.succeeded()) {
                result.handle(Future.succeededFuture());
            }
            else {
                result.handle(Future.failedFuture(ar.cause()));
            }
        });
    }
}
