/*******************************************************************************
 * Copyright (c) 2018, 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.integration.service.datastore;

import cucumber.api.CucumberOptions;
import org.eclipse.kapua.qa.common.cucumber.CucumberProperty;
import org.eclipse.kapua.qa.common.cucumber.CucumberWithProperties;
import org.junit.runner.RunWith;

@RunWith(CucumberWithProperties.class)
@CucumberOptions(
        features = "classpath:features/datastore/DatastoreNewIndex.feature",
        glue = {"org.eclipse.kapua.qa.common",
                "org.eclipse.kapua.qa.integration.steps",
                "org.eclipse.kapua.service.account.steps",
                "org.eclipse.kapua.service.user.steps",
                "org.eclipse.kapua.service.datastore.steps",
                "org.eclipse.kapua.service.device.registry.steps" },
        plugin = {"pretty",
                "html:target/cucumber/DatastoreNewIndex",
                "json:target/DatastoreNewIndex_cucumber.json" },
        strict = true,
        monochrome = true)
@CucumberProperty(key="test.name", value="RunDatastoreNewIndexTest")
@CucumberProperty(key="commons.settings.hotswap", value="true")
@CucumberProperty(key="commons.db.jdbcConnectionUrlResolver", value="DEFAULT")
@CucumberProperty(key="commons.db.connection.scheme", value="jdbc:h2:tcp")
@CucumberProperty(key="commons.db.jdbc.driver", value="org.h2.Driver")
@CucumberProperty(key="commons.db.connection.host", value="localhost")
@CucumberProperty(key="commons.db.connection.port", value="3306")
@CucumberProperty(key="datastore.index.prefix", value="")
public class RunDatastoreNewIndexTest {
}
