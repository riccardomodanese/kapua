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
package org.eclipse.kapua.broker.artemis.plugin.security.context;

import org.apache.activemq.artemis.core.server.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KapuaSession {

    protected static Logger logger = LoggerFactory.getLogger(KapuaSession.class);

    private String connectionId;
    private String username;
    private String account;
    private String clientId;

    public KapuaSession(ServerSession session, String connectionId, String account) {
        this.connectionId = connectionId;
        this.account = account;
        clientId = session.getRemotingConnection().getClientID();
        username = session.getUsername();
    }

    public void log(String operation) {
        logger.info("KapuaSession: {} - User {} - client id: {} - connection id: {}", operation, username, clientId, connectionId);
    }

    public void updateAccount(String account) {
        this.account = account;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getUsername() {
        return username;
    }

    public String getAccount() {
        return account;
    }

    public String getClientId() {
        return clientId;
    }
}
