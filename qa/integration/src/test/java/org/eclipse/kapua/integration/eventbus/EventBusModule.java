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
package org.eclipse.kapua.integration.eventbus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.kapua.commons.event.ServiceEventClientConfiguration;
import org.eclipse.kapua.commons.event.ServiceEventModule;
import org.eclipse.kapua.commons.event.ServiceEventModuleConfiguration;
import org.eclipse.kapua.commons.event.ServiceInspector;
import org.eclipse.kapua.integration.eventbus.service.DummyServiceConsumer;
import org.eclipse.kapua.integration.eventbus.service.DummyServiceConsumerImpl;
import org.eclipse.kapua.integration.eventbus.service.DummyServiceRaiser;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.KapuaService;
import org.eclipse.kapua.service.account.internal.AccountEntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KapuaProvider
public class EventBusModule extends ServiceEventModule implements KapuaService {

    protected static final Logger logger = LoggerFactory.getLogger(EventBusModule.class);
    private static final String UNIQUE_ID = "_" + UUID.randomUUID().toString();

    public static final String EVENT_BUS_TEST_ADDRESS = "eventBusTest";

    @Inject
    private DummyServiceConsumerImpl dummyServiceConsumerImpl;

    @Inject
    private DummyServiceRaiser dummyServiceRaiser;

    @Override
    protected ServiceEventModuleConfiguration initializeConfiguration() {
//        org.eclipse.kapua.integration.eventbus.service.DummyServiceConsumer_8a5ad73b-cf3d-45b9-83ec-beff8ebda56c=eventBusTest}
        List<ServiceEventClientConfiguration> secc = new ArrayList<>();
        secc.addAll(ServiceInspector.getEventBusClients(dummyServiceConsumerImpl, DummyServiceConsumer.class));
        secc.addAll(ServiceInspector.getEventBusClients(dummyServiceRaiser, DummyServiceRaiser.class));
        return new ServiceEventModuleConfiguration(
                EVENT_BUS_TEST_ADDRESS,
                //it doesn't matter which entity manager factory we will configure since the consumer event service will have no db access needing
                AccountEntityManagerFactory.getInstance(),
                updateClientConfiguration(secc, UNIQUE_ID));
    }

    protected ServiceEventClientConfiguration[] updateClientConfiguration(List<ServiceEventClientConfiguration> configs, String uniqueId) {
        ArrayList<ServiceEventClientConfiguration> configList = new ArrayList<>();
        for(ServiceEventClientConfiguration config : configs) {
            if(config.getEventListener() == null) {
                // config for @RaiseServiceEvent
                logger.debug("Adding config for @RaiseServiceEvent - address: {}, name: {}, listener: {}", config.getAddress(), config.getClientName(), config.getEventListener());
                configList.add(config);
            } else {
                // config for @ListenServiceEvent
                String uniqueName = config.getClientName() + uniqueId;
                logger.debug("Adding config for @ListenServiceEvent - address: {}, name: {}, listener: {}", config.getAddress(), uniqueName, config.getEventListener());
                configList.add(new ServiceEventClientConfiguration(config.getAddress(), uniqueName, config.getEventListener()));
            }
        }
        return configList.toArray(new ServiceEventClientConfiguration[0]);
    }

}
