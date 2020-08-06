/*******************************************************************************
 * Copyright (c) 2018, 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kapua.integration.rest;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features/rest/user/RestUser.feature",
        glue = {"org.eclipse.kapua.qa.common",
                "org.eclipse.kapua.qa.integration.steps",
                "org.eclipse.kapua.service.account.steps",
                "org.eclipse.kapua.service.user.steps"
        },
        plugin = { "pretty",
                "html:target/cucumber/RestUser",
                "json:target/RestUser_cucumber.json"
        },
        strict = true,
        monochrome = true)

//@CucumberProperty(key="certificate.jwt.private.key", value= "certificates/key.pk8")
//@CucumberProperty(key="certificate.jwt.certificate", value= "certificates/certificate.pem")
public class RunRestUserTest {
}
