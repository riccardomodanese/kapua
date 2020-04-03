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
package org.eclipse.kapua.integration.eventbus.service;

import org.eclipse.kapua.event.RaiseServiceEvent;
import org.eclipse.kapua.integration.eventbus.EventBusModule;
import org.eclipse.kapua.integration.eventbus.EventCounter;
import org.eclipse.kapua.locator.KapuaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KapuaProvider
public class DummyServiceRaiserImpl implements DummyServiceRaiser {

    protected static final Logger logger = LoggerFactory.getLogger(DummyServiceRaiserImpl.class);

    @Override
    @RaiseServiceEvent(
            service="org.eclipse.kapua.integration.eventbus.service.DummyServiceRaiser",
            entityType="DummyEntity",
            operation="create",
            note="none")
    public void raiseEvent() {
        logger.info("Invoking service event raiser");
        EventCounter.getInstance().incRaised(EventBusModule.EVENT_BUS_TEST_ADDRESS, 1);
    }
}
