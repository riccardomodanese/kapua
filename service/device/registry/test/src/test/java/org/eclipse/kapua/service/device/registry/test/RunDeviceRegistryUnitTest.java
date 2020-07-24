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
package org.eclipse.kapua.service.device.registry.test;

import cucumber.api.CucumberOptions;
import org.eclipse.kapua.qa.common.cucumber.CucumberProperty;
import org.junit.runner.RunWith;

@RunWith(CucumberWithPropertiesForDeviceRegistry.class)
@CucumberOptions(
        features = { "classpath:features/DeviceRegistryConnection.feature"
                   },
        glue = { "org.eclipse.kapua.service.device.registry.steps",
                 "org.eclipse.kapua.qa.common"
               },
        plugin = { "pretty", 
                   "html:target/cucumber",
                   "json:target/cucumber.json" },
        strict = true,
        monochrome = true)
@CucumberProperty(key = "locator.class.impl", value = "org.eclipse.kapua.qa.common.MockedLocator")
@CucumberProperty(key = "commons.settings.hotswap", value = "true")
@CucumberProperty(key = "commons.db.connection.scheme", value = "jdbc:h2:mem;MODE=MySQL")
@CucumberProperty(key = "commons.db.jdbcConnectionUrlResolver", value = "H2")
@CucumberProperty(key = "commons.db.schema.update", value = "true")
@CucumberProperty(key = "commons.db.connection.host", value = "")
@CucumberProperty(key = "commons.db.connection.port", value = "")
public class RunDeviceRegistryUnitTest {
}
