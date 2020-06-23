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

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.qa.common.TestJAXBContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.java.en.Given;
import cucumber.runtime.java.guice.ScenarioScoped;

@ScenarioScoped
public class InitJaxbContext {

    private static final Logger logger = LoggerFactory.getLogger(InitJaxbContext.class);

    @Given("^Init Jaxb Context$")
    public void initJAXBContext() throws KapuaException {
        logger.info("Initializing Test JAXB context...");
        XmlUtil.setContextProvider(new TestJAXBContextProvider());
        logger.info("Initializing Test JAXB context... DONE");
    }
}
