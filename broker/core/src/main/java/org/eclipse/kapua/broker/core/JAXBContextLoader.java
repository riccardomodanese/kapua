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
package org.eclipse.kapua.broker.core;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.broker.core.setting.BrokerSetting;
import org.eclipse.kapua.broker.core.setting.BrokerSettingKey;
import org.eclipse.kapua.commons.util.ClassUtil;
import org.eclipse.kapua.commons.util.xml.JAXBContextProvider;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jaxb context loader
 *
 */
public class JAXBContextLoader {

    protected static final Logger logger = LoggerFactory.getLogger(JAXBContextLoader.class);

    private static final String JAXB_CONTEXT_CLASS_NAME;

    static {
        BrokerSetting config = BrokerSetting.getInstance();
        JAXB_CONTEXT_CLASS_NAME = config.getString(BrokerSettingKey.JAXB_CONTEXT_CLASS_NAME);
    }

    public JAXBContextLoader() throws KapuaException {
    }

    public void init() throws KapuaException {
        logger.info(">>> Jaxb context loader... load context");
        JAXBContextProvider jaxbContextProvider = ClassUtil.newInstance(JAXB_CONTEXT_CLASS_NAME, null);
        XmlUtil.setContextProvider(jaxbContextProvider);
        logger.info(">>> Jaxb context loader... load context DONE");
    }

    public void reset() {
        XmlUtil.setContextProvider(null);
    }

}
