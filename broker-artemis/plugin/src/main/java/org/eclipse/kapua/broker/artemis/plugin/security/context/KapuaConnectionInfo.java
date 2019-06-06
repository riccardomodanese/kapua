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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KapuaConnectionInfo {

    protected static Logger logger = LoggerFactory.getLogger(KapuaConnectionInfo.class);

    protected static final String FULL_CLIENT_ID_PATTERN = "%s|%s";

    private String connectionId;
    private String username;
    private String account;
    private String clientId;

    public KapuaConnectionInfo(String connectionId, String clientId, String account, String username) {
        this.connectionId = connectionId;
        this.clientId = clientId;
        this.account = account;
        this.username = username;
    }

    public void log(String operation) {
        logger.info("KapuaConnectionInfo: {} - User {} - client id: {} - connection id: {}", operation, username, clientId, connectionId);
    }

    public boolean isStealingLink(String connectionId) {
        return connectionId !=null ? !connectionId.equals(this.connectionId) : this.connectionId!=null;
    }

    public static String getFullClientId(KapuaMetaData kapuaMetaData) {
        return String.format(FULL_CLIENT_ID_PATTERN, kapuaMetaData.getAccount(), kapuaMetaData.getClientId());
    }

    public static String getFullClientId(String account, String clientId) {
        return String.format(FULL_CLIENT_ID_PATTERN, account, clientId);
    }

    public String getFullClientId() {
        return String.format(FULL_CLIENT_ID_PATTERN, getAccount(), getClientId());
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
