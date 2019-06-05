/*******************************************************************************
 * Copyright (c) 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.broker.artemis.plugin.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.ActiveMQExceptionType;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.core.protocol.mqtt.MQTTConnection;
import org.apache.activemq.artemis.core.remoting.FailureListener;
import org.apache.activemq.artemis.core.server.ServerConsumer;
import org.apache.activemq.artemis.core.server.ServerSession;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerPlugin;
import org.apache.activemq.artemis.core.transaction.Transaction;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.eclipse.kapua.broker.artemis.plugin.security.context.KapuaConnectionInfo;
import org.eclipse.kapua.broker.artemis.plugin.security.context.KapuaMetaData;
import org.eclipse.kapua.broker.artemis.plugin.security.context.KapuaSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.DefaultChannelId;

public class ArtemisSecurityPlugin implements ActiveMQServerPlugin {

    protected static Logger logger = LoggerFactory.getLogger(ArtemisSecurityPlugin.class);

    private static final String CONNECTION_ID = "CONNECTION_ID";
    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String ACL = "ACL";

    //TODO check if connection id has a correct equals implementation otherwise cannot be used as key for a map
    //use string as key since some method returns DefaultChannelId as connection id, some other a string
    //the string returned by some method as connection id is the asShortText of DefaultChannelId
    private static final Map<String, KapuaSession> SESSION_MAP = new ConcurrentHashMap<>();
    private static final Map<String, KapuaConnectionInfo> CONNECTION_MAP = new ConcurrentHashMap<>();

    private String version;

    //
    private static final String ACCOUNT = "ACCOUNT";

    public ArtemisSecurityPlugin() {
    }

    /**
     * CONNECT
     */

    @Override
    public void afterCreateConnection(RemotingConnection connection) throws ActiveMQException {
        Object connectionID = connection.getID();
        connection.addCloseListener(() -> {
            logger.info("Connection closed: {} - {}", connectionID, connectionID.getClass());
        });
        connection.addFailureListener(new FailureListener() {

            @Override
            public void connectionFailed(ActiveMQException exception, boolean failedOver, String scaleDownTargetNodeID) {
                logger.info("connectionFailed: {} - {} \n {} - {} - {}", connectionID, connectionID.getClass(), exception.getMessage(), failedOver, scaleDownTargetNodeID);
                logger.trace("{}", exception);
            }

            @Override
            public void connectionFailed(ActiveMQException exception, boolean failedOver) {
                logger.info("connectionFailed: {} - {} \n {} - {}", connectionID, connectionID.getClass(), exception.getMessage(), failedOver);
                logger.trace("{}", exception);
            }
        });
        String clientId = ((MQTTConnection)connection).getClientID();
        logger.info("afterCreateConnection: {}", connectionID, connectionID.getClass());
        ActiveMQServerPlugin.super.afterCreateConnection(connection);//no info
    }

    @Override
    public void afterCreateSession(ServerSession session) throws ActiveMQException {
        String connectionId = getConnectionId(session.getConnectionID());
        try {
            KapuaConnectionInfo kapuaConnectionInfo = setConnectionInfo(session, connectionId, ACCOUNT);
            fillMetaData(session, connectionId);
            KapuaSession kapuaSession = new KapuaSession(session, connectionId, ACCOUNT);
            kapuaSession.log("afterCreateSession");
            kapuaConnectionInfo.log("afterCreateSession");
            setKapuaSession(kapuaSession);

            Object acl = doLogin();
            //create ACL
            setAcl(session, kapuaSession, acl);
            ActiveMQServerPlugin.super.afterCreateSession(session);
        }
        catch (Exception e) {
            throw new ActiveMQException(ActiveMQExceptionType.SECURITY_EXCEPTION, "User not authorized!", e);
        }
    }

    /**
     * DISCONNECT
     */

    @Override
    public void afterDestroyConnection(RemotingConnection connection) throws ActiveMQException {
        Object connectionID = connection.getID();
        logger.info("afterDestroyConnection: {}", connectionID, connectionID.getClass());
        ActiveMQServerPlugin.super.afterDestroyConnection(connection);//just client-id
    }

    @Override
    public void beforeCloseSession(ServerSession session, boolean failed) throws ActiveMQException {
        String connectionId = getConnectionId(session.getConnectionID());
        KapuaMetaData kapuaMetaData = getKapuaMetaData(session, ACCOUNT, connectionId);
        KapuaConnectionInfo connectionInfo = getConnectionInfo(kapuaMetaData);
        KapuaSession kapuaSession = getKapuaSession(connectionId);
        if (connectionInfo.isStealingLink(connectionId)) {
            logger.info("Stealing link occurred... skip adding disconnect event!");
        }
        else {
            logger.info("Adding disconnect event!");
            doLogout();
            cleanConnectionInfo(connectionInfo);
        }
        connectionInfo.log("beforeCloseSession");
        kapuaSession.log("beforeCloseSession");
        ActiveMQServerPlugin.super.beforeCloseSession(session, failed);
    }

    /**
     * SUBSCRIBE
     */

    @Override
    public void afterCreateConsumer(ServerConsumer consumer) throws ActiveMQException {
        String connectionId = getConnectionId(consumer.getConnectionID());
        KapuaSession kapuaSession = getKapuaSession(connectionId);
        kapuaSession.log("afterCreateConsumer");
        checkConsumerAllowed();
        ActiveMQServerPlugin.super.afterCreateConsumer(consumer);
    }

    /**
     * PUBLISH
     */

    @Override
    public void beforeSend(ServerSession session, Transaction tx, Message message, boolean direct,
            boolean noAutoCreateQueue) throws ActiveMQException {
        String connectionId = getConnectionId(session.getConnectionID());
        KapuaMetaData kapuaMetaData = getKapuaMetaData(session, ACCOUNT, connectionId);
        KapuaSession kapuaSession = getKapuaSession(connectionId);
        kapuaSession.log("beforeSend");
        checkPublisherAllowed();
        ActiveMQServerPlugin.super.beforeSend(session, tx, message, direct, noAutoCreateQueue);
    }

    /**
     * UTILS
     * @throws ActiveMQException
     */

    private String getConnectionId(Object connectionId) throws ActiveMQException {
        logger.info("==========> {} - {}", connectionId, connectionId.getClass());
        if (connectionId instanceof DefaultChannelId) {
            return ((DefaultChannelId)connectionId).asShortText();
        }
        else if (connectionId instanceof String) {
            return (String) connectionId;
        }
        throw new ActiveMQException("Unsupported connectionId type " + (connectionId != null ? connectionId.getClass() : null));
    }

    private KapuaConnectionInfo setConnectionInfo(ServerSession session, String connectionId, String account) {
        String clientId = session.getRemotingConnection().getClientID();
        String username = session.getUsername();
        KapuaConnectionInfo kapuaConnectionInfo = new KapuaConnectionInfo(connectionId, clientId, account, username);
        CONNECTION_MAP.put(KapuaConnectionInfo.getFullClientId(kapuaConnectionInfo), kapuaConnectionInfo);
        return kapuaConnectionInfo;
    }

    private KapuaConnectionInfo cleanConnectionInfo(KapuaConnectionInfo kapuaConnectionInfo) {
        return CONNECTION_MAP.remove(KapuaConnectionInfo.getFullClientId(kapuaConnectionInfo));
    }

    private KapuaConnectionInfo getConnectionInfo(KapuaMetaData kapuaMetaData) {
        return CONNECTION_MAP.get(KapuaConnectionInfo.getFullClientId(kapuaMetaData));
    }

    private void fillMetaData(ServerSession session, String connectionId) throws ActiveMQException {
        try {
            session.addMetaData(CONNECTION_ID, connectionId);
            session.addMetaData(CLIENT_ID, session.getRemotingConnection().getClientID());
            session.addMetaData(USERNAME, session.getUsername());
            session.addMetaData(PASSWORD, session.getPassword());
        } catch (Exception e) {
            throw new ActiveMQException(ActiveMQExceptionType.SECURITY_EXCEPTION, "Cannot fill metadata!", e);
        }
    }

    private KapuaMetaData getKapuaMetaData(ServerSession session, String account, String connectionId) {
        return new KapuaMetaData(connectionId, session.getMetaData(CLIENT_ID), session.getMetaData(USERNAME), account, session.getMetaData(PASSWORD));
    }

    private KapuaSession getKapuaSession(String connectionId) {
        return SESSION_MAP.get(connectionId);
    }

    private void setKapuaSession(KapuaSession kapuaSession) {
        SESSION_MAP.put(kapuaSession.getConnectionId(), kapuaSession);
    }

    private void setAcl(ServerSession session, KapuaSession kapuaSession, Object acl) throws ActiveMQException {
        try {
            session.addMetaData(ACL, "acl_" + acl);
        } catch (Exception e) {
            throw new ActiveMQException(ActiveMQExceptionType.SECURITY_EXCEPTION, "Cannot fill metadata!", e);
        }
    }

    /**
     * backend calls
     */

    private Object doLogin() {
        return null;
    }

    private void doLogout() {
    }

    private void checkPublisherAllowed() {
    }

    private void checkConsumerAllowed() {
    }

//    @Override
//    public void duplicateSessionMetadataFailure(ServerSession session, String key, String data)
//            throws ActiveMQException {
//        // TODO Auto-generated method stub
//        ActiveMQServerPlugin.super.duplicateSessionMetadataFailure(session, key, data);
//    }

//    @Override
//    public void criticalFailure(CriticalComponent components) throws ActiveMQException {
//        // TODO Auto-generated method stub
//        ActiveMQServerPlugin.super.criticalFailure(components);
//    }

}