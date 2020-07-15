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
package org.eclipse.kapua.qa.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.java.en.Given;
import cucumber.runtime.java.guice.ScenarioScoped;

@ScenarioScoped
public class InitShiro {

    private static final Logger logger = LoggerFactory.getLogger(InitShiro.class);

    @Given("^Init Security Context$")
    public void start() throws IOException {
        logger.info("Init shiro security manager...");
        try {
            SecurityManager securityManager = SecurityUtils.getSecurityManager();
            logger.info("Found Shiro security manager {}", securityManager);
        }
        catch (UnavailableSecurityManagerException e) {
            logger.info("Init shiro security manager...");
            final URL shiroIniUrl = getClass().getResource("/shiro.ini");
            Ini shiroIni = new Ini();
            try (final InputStream input = shiroIniUrl.openStream()) {
                shiroIni.load(input);
            }
            SecurityManager securityManager = new IniSecurityManagerFactory(shiroIni).getInstance();
            SecurityUtils.setSecurityManager(securityManager);
        }
        logger.info("Init shiro security manager... DONE");
    }

    @Given("^Reset Security Context$")
    public void stop() {
        logger.info("Reset shiro security manager...");
        SecurityUtils.setSecurityManager(null);
        logger.info("Reset shiro security manager... DONE");
    }

}
