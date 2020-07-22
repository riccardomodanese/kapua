/*******************************************************************************
 * Copyright (c) 2017, 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.integration.service.user;

import cucumber.api.CucumberOptions;
import org.eclipse.kapua.qa.common.cucumber.CucumberProperty;
import org.eclipse.kapua.qa.common.cucumber.CucumberWithProperties;
import org.junit.runner.RunWith;

@RunWith(CucumberWithProperties.class)
@CucumberOptions(
        features = "classpath:features/user/TenantSEI9n.feature",
        glue = {"org.eclipse.kapua.qa.common",
                "org.eclipse.kapua.service.account.steps",
                "org.eclipse.kapua.service.user.steps"
               },
        plugin = {"pretty", 
                  "html:target/cucumber/TenantSEI9n",
                  "json:target/TenantSEI9n_cucumber.json"
                 },
        strict = true,
        monochrome = true)
@CucumberProperty(key="test.type", value="unit")
@CucumberProperty(key="test.name", value="RunTenantSEI9nTest")
@CucumberProperty(key="commons.settings.hotswap", value="true")
@CucumberProperty(key="org.eclipse.kapua.qa.datastore.extraStartupDelay", value="1")
@CucumberProperty(key="org.eclipse.kapua.qa.broker.extraStartupDelay", value="1")
public class RunTenantSEI9nTest {}
