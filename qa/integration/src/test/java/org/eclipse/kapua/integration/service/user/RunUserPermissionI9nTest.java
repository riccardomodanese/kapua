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
package org.eclipse.kapua.integration.service.user;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features/user/UserPermissionI9n.feature",

        glue = {"org.eclipse.kapua.qa.common",
                "org.eclipse.kapua.qa.integration.steps",
                "org.eclipse.kapua.service.account.steps",
                "org.eclipse.kapua.service.user.steps",
                "org.eclipse.kapua.service.authorization.steps",
                "org.eclipse.kapua.service.device.registry.steps",
                "org.eclipse.kapua.service.tag.steps",
                "org.eclipse.kapua.service.job.steps",
                "org.eclipse.kapua.service.datastore.steps",
                "org.eclipse.kapua.service.scheduler.steps",
                "org.eclipse.kapua.service.endpoint.steps"
        },
        plugin = {"pretty",
                "html:target/cucumber/UserServiceI9n",
                "json:target/UserServiceI9n_cucumber.json"
        },
        strict = true,
        monochrome = true)
public class RunUserPermissionI9nTest {}
