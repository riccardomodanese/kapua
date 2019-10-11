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
package org.eclipse.kapua.datastore.connector;

import java.util.UUID;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.connector.MessageContext;
import org.eclipse.kapua.connector.MessageTarget;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.message.KapuaChannel;
import org.eclipse.kapua.message.KapuaMessage;
import org.eclipse.kapua.message.KapuaMessageFactory;
import org.eclipse.kapua.message.KapuaPayload;
import org.eclipse.kapua.message.transport.TransportMessage;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.datastore.MessageStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class DatastoreTarget implements MessageTarget<TransportMessage> {

    //TODO fix me
    public static final String MESSAGE_TYPE = new String("message-type");
    private static final Logger logger = LoggerFactory.getLogger(DatastoreTarget.class);

    private Vertx vertx;

    private AccountService accountService;
    private MessageStoreService messageStoreService;
    private KapuaMessageFactory kapuaMessageFactory;

    public static DatastoreTarget create(Vertx vertx) {
        return new DatastoreTarget(vertx);
    }

    protected DatastoreTarget(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void start(Future<Void> startFuture) {
        accountService = KapuaLocator.getInstance().getService(AccountService.class);
        //calling the get message store service in order to initialize the ES link here
        messageStoreService = KapuaLocator.getInstance().getService(MessageStoreService.class);
        kapuaMessageFactory = KapuaLocator.getInstance().getFactory(KapuaMessageFactory.class);
        startFuture.complete();
    }

    @Override
    //TODO choose the appropriate exception
    public void process(MessageContext<TransportMessage> message, Handler<AsyncResult<Void>> result) {
        logger.debug("Datastore service... converting received message: {}", message);
        TransportMessage tm = message.getMessage();
        final String scopeName = tm.getScopeName();
        KapuaMessage<KapuaChannel, KapuaPayload> kapuaDataMessage = kapuaMessageFactory.newMessage();

        //channel
        KapuaChannel kapuaChannel = kapuaMessageFactory.newChannel();
        kapuaChannel.setSemanticParts(tm.getChannel().getSemanticParts());

        //payload
        KapuaPayload kapuaPayload = kapuaMessageFactory.newPayload();
        kapuaPayload.setMetrics(tm.getPayload().getMetrics());
        kapuaPayload.setBody(tm.getPayload().getBody());

        kapuaDataMessage.setChannel(kapuaChannel);
        kapuaDataMessage.setPayload(kapuaPayload);
        kapuaDataMessage.setCapturedOn(null);
        kapuaDataMessage.setClientId(tm.getClientId());
        kapuaDataMessage.setDeviceId(null);
        kapuaDataMessage.setId(UUID.randomUUID());
        kapuaDataMessage.setPosition(tm.getPosition());
        kapuaDataMessage.setReceivedOn(tm.getReceivedOn());
        kapuaDataMessage.setSentOn(tm.getSentOn());
        vertx.executeBlocking(fut -> {
            try {
                KapuaSecurityUtils.doPrivileged(() -> {
                    Account account = accountService.findByName(scopeName);
                    if (account==null) {
                        throw KapuaException.internalError(String.format("Cannot find account %s", scopeName));
                    }
                    kapuaDataMessage.setScopeId(account.getId());
                    logger.debug("Datastore service... converting message... DONE storing message...");
                    messageStoreService.store(kapuaDataMessage);
                    logger.debug("Datastore service... storing message... DONE");
                    fut.complete();
                });
            } catch (KapuaException e) {
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

    @Override
    public void stop(Future<Void> stopFuture) {
        // nothing to do
        stopFuture.complete();
    }
}