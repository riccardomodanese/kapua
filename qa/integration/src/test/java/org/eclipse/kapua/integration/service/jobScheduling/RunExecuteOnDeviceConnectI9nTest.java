/*******************************************************************************
 * Copyright (c) 2019, 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.integration.service.jobScheduling;

import cucumber.api.CucumberOptions;

import org.eclipse.kapua.qa.common.cucumber.CucumberProperty;
import org.eclipse.kapua.qa.common.cucumber.CucumberWithProperties;
import org.junit.runner.RunWith;

@RunWith(CucumberWithProperties.class)
@CucumberOptions(
        features = {
                "classpath:features/jobScheduling/ExecuteOnDeviceConnectI9n.feature",
        },
        glue = { "org.eclipse.kapua.service.job.steps",
                "org.eclipse.kapua.service.user.steps",
                "org.eclipse.kapua.qa.common",
                "org.eclipse.kapua.qa.integration.steps",
                "org.eclipse.kapua.service.account.steps",
                "org.eclipse.kapua.service.device.registry.steps",
                "org.eclipse.kapua.service.scheduler.steps"
        },
        plugin = { "pretty",
                "html:target/cucumber",
                "json:target/cucumber.json" },
        strict = true,
        monochrome = true)
@CucumberProperty(key="test.type", value="integration")
@CucumberProperty(key="commons.settings.hotswap", value="true")
@CucumberProperty(key="commons.db.jdbcConnectionUrlResolver", value="DEFAULT")
@CucumberProperty(key="commons.db.connection.scheme", value="jdbc:h2:tcp")
@CucumberProperty(key="datastore.index.prefix", value="")
@CucumberProperty(key="commons.settings.hotswap", value="true")
public class RunExecuteOnDeviceConnectI9nTest {
}
