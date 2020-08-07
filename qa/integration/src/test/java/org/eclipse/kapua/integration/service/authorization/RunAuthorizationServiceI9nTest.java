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
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.integration.service.authorization;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = { "classpath:features/authorization/AccessInfoService.feature",
                     "classpath:features/authorization/DomainService.feature",
                     "classpath:features/authorization/GroupService.feature",
                     "classpath:features/authorization/MiscAuthorization.feature",
                     "classpath:features/authorization/RoleService.feature"
                   },
        glue = { "org.eclipse.kapua.qa.common",
                 "org.eclipse.kapua.service.authorization.steps",
                 "org.eclipse.kapua.service.account.steps",
                 "org.eclipse.kapua.service.user.steps",
                 "org.eclipse.kapua.service.device.registry.steps"
               },
        plugin = {"pretty", 
                  "html:target/cucumber/AuthorizationServiceI9n",
                  "json:target/AuthorizationServiceI9n_cucumber.json"
                 },
        strict = true,
        monochrome = true)
public class RunAuthorizationServiceI9nTest {}
