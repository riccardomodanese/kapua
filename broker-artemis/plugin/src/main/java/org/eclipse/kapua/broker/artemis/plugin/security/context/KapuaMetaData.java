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

public class KapuaMetaData {

    private String connectionId;
    private String clientId;
    private String username;
    private String account;
    private String password;

    public KapuaMetaData(String connectionId, String clientId, String username, String account, String password) {
        this.connectionId = connectionId;
        this.clientId = clientId;
        this.username = username;
        this.account = account;
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public String getClientId() {
        return clientId;
    }
}
