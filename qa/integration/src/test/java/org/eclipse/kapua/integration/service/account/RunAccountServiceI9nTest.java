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
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.integration.service.account;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {
                "classpath:features/account/AccountService.feature",
                "classpath:features/account/AccountExpirationI9n.feature",
                "classpath:features/account/FindSelfAccount.feature",
                "classpath:features/account/AccountGroupService.feature",
                "classpath:features/account/AccountDeviceRegistryService.feature",
                "classpath:features/account/AccountJobService.feature",
                "classpath:features/account/AccountRoleService.feature",
                "classpath:features/account/AccountTagService.feature",
                "classpath:features/account/AccountUserService.feature",
                "classpath:features/account/AccountCredentialService.feature"
                },
        glue = {"org.eclipse.kapua.qa.common",
                "org.eclipse.kapua.service.account.steps",
                "org.eclipse.kapua.service.user.steps",
                "org.eclipse.kapua.service.authorization.steps",
                "org.eclipse.kapua.service.device.registry.steps",
                "org.eclipse.kapua.service.job.steps",
                "org.eclipse.kapua.service.tag.steps"
        },
        plugin = {"pretty",
                "html:target/cucumber/AccountServiceI9n",
                "json:target/AccountServiceI9n_cucumber.json"
        },
        strict = true,
        monochrome = true)
public class RunAccountServiceI9nTest {
}
