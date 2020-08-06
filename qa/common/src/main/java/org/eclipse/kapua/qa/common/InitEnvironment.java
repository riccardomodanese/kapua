/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.qa.common;

import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import cucumber.api.Scenario;
import cucumber.api.java.Before;

@Singleton
public class InitEnvironment {

    private static final Logger logger = LoggerFactory.getLogger(InitEnvironment.class);

    @Before(value="@setup and @env_docker", order=0)
    public void initParametersDocker(Scenario scenario) {
        logger.info("=====> Init parameters for docker environment");
        SystemSetting.resetInstance();
        System.setProperty("commons.db.schema", "kapuadb");
        System.setProperty("commons.db.schema.update", "true");
        System.setProperty("commons.db.connection.host", "localhost");
        System.setProperty("commons.db.connection.port", "3306");
        System.setProperty("commons.db.jdbcConnectionUrlResolver", "DEFAULT");
        System.setProperty("commons.db.jdbc.driver", "org.h2.Driver");
        System.setProperty("commons.db.connection.scheme", "jdbc:h2:tcp");
        System.setProperty("datastore.index.prefix", "");
        System.setProperty("certificate.jwt.private.key", "certificates/jwt/test.key");
        System.setProperty("certificate.jwt.certificate", "certificates/jwt/test.cert");
        System.setProperty("broker.ip", "localhost");
//        System.setProperty("kapua.config.url", "");
    }

    @Before(value="@setup and (@env_embedded_minimal or @env_none)", order=0)
    public void initParametersEmbedded(Scenario scenario) {
        logger.info("=====> Init parameters for embedded environment");
        SystemSetting.resetInstance();
        System.setProperty("commons.db.schema", "kapuadb");
        System.setProperty("commons.db.schema.update", "true");
        System.setProperty("commons.db.connection.host", "");
        System.setProperty("commons.db.connection.port", "");
        System.setProperty("commons.db.jdbcConnectionUrlResolver", "H2");
        System.setProperty("commons.db.jdbc.driver", "org.h2.Driver");
        System.setProperty("commons.db.connection.scheme", "jdbc:h2:mem;MODE=MySQL");
        System.setProperty("datastore.index.prefix", "");
        System.setProperty("certificate.jwt.private.key", "certificates/jwt/test.key");
        System.setProperty("certificate.jwt.certificate", "certificates/jwt/test.cert");
        System.setProperty("broker.ip", "localhost");
//        System.setProperty("kapua.config.url", "");
    }
}
