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

import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.integration.eventbus.service.DummyServiceRaiser;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.qa.common.DBHelper;
import org.eclipse.kapua.qa.common.utils.EmbeddedBroker;
import org.eclipse.kapua.qa.common.utils.EmbeddedDatastore;
import org.eclipse.kapua.qa.common.utils.EmbeddedEventBroker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netcrusher.core.reactor.NioReactor;
import org.netcrusher.tcp.TcpCrusher;
import org.netcrusher.tcp.TcpCrusherBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBusIT {

    protected static final Logger logger = LoggerFactory.getLogger(EventBusIT.class);

    private static long waitTime0 = 1000;
    private static long waitTime1 = 180000;
    private static long waitTime2 = 8000;
    private DBHelper dbHelper;
    private EmbeddedDatastore embeddedDatastore;
    private EmbeddedEventBroker embeddedEventBroker;
    private EmbeddedBroker embeddedBroker;

    private NioReactor reactor;
    private TcpCrusher crusher;

    @Before
    public void init() throws Exception {
        initSystemProperties();
        dbHelper = new DBHelper();
        dbHelper.setup();
        embeddedDatastore = new EmbeddedDatastore();
        embeddedDatastore.setup();
        embeddedEventBroker = new EmbeddedEventBroker(dbHelper);
        embeddedEventBroker.start();

        reactor = new NioReactor();
        crusher = TcpCrusherBuilder.builder()
            .withReactor(reactor)
            .withBindAddress("127.0.0.1", 15672)
            .withConnectAddress("127.0.0.1", 5672)
            .buildAndOpen();

//        // emulate reconnect
//        crusher.reopen();

        embeddedBroker = new EmbeddedBroker();
        embeddedBroker.start();
    }

    @After
    public void shutdown() throws Exception {
        embeddedBroker.stop();
        embeddedEventBroker.stop();
        embeddedDatastore.closeNode();
        dbHelper.close();
        crusher.close();
        reactor.close();
    }

    @Test
    public void testEvents() throws Exception {
        DummyServiceRaiser dummyServiceRaiser = KapuaLocator.getInstance().getService(DummyServiceRaiser.class);
        KapuaSecurityUtils.doPrivileged(() -> {
            int raised = 1;
            int consumed = 1;
            dummyServiceRaiser.raiseEvent();
            checkEvents(raised++, consumed++);
            dummyServiceRaiser.raiseEvent();
            checkEvents(raised++, consumed++);
            crusher.freeze();
            raised += waitForAndRaiseEvent(waitTime1);
            crusher.unfreeze();
            waitFor(waitTime2);
            dummyServiceRaiser.raiseEvent();
            checkEvents(raised++, consumed++);
            crusher.close();
            raised += waitForAndRaiseEvent(waitTime1);
            crusher.open();
            waitFor(waitTime2);
            dummyServiceRaiser.raiseEvent();
            checkEvents(raised++, consumed++);
            dummyServiceRaiser.raiseEvent();
            checkEvents(raised++, consumed++);
            dummyServiceRaiser.raiseEvent();
            checkEvents(raised++, consumed++);
        });
        //drop connection to the event broker
    }

    public void checkEvents(int raised, int consumed) {
        EventCounter.getInstance().check(EventBusModule.EVENT_BUS_TEST_ADDRESS, raised, consumed);
    }

    private void waitFor(long time) throws InterruptedException {
        logger.info("Wait for {}[s]", time/1000);
        Thread.sleep(time);
        logger.info("Wait for {}[s] DONE", time/1000);
    }

    public int waitForAndRaiseEvent(long time) throws InterruptedException {
        DummyServiceRaiser dummyServiceRaiser = KapuaLocator.getInstance().getService(DummyServiceRaiser.class);
        int raised = 0;
        logger.info("Wait for {}[s]", time/1000);
        for (int i=0; i<time/waitTime0; i++) {
            Thread.sleep(waitTime0);
            dummyServiceRaiser.raiseEvent();
            raised++;
        }
        logger.info("Wait for {}[s] DONE", time/1000);
        return raised;
    }

    public void initSystemProperties() {
        System.setProperty("commons.eventbus.url", "amqp://127.0.0.1:15672");
        System.setProperty("commons.eventbus.username", "kapua-sys");
        System.setProperty("commons.eventbus.password", "kapua-password");
        //System.setProperty("certificate.jwt.private.key", "certificates/key.pk8");
        //System.setProperty("certificate.jwt.certificate", "certificates/certificate.pem");
        System.setProperty("commons.db.schema", "kapuadb");
        System.setProperty("commons.db.schema.update", "true");
        System.setProperty("commons.db.connection.host", "localhost");
        System.setProperty("commons.db.connection.port", "3306");
        //System.setProperty("test.h2.server", "false");
        //System.setProperty("h2.bindAddress", "127.0.0.1");
        //XmlUtil.setContextProvider(new TestJAXBContextProvider());
        //System.setProperty("broker.ip", "192.168.33.10");
        System.setProperty("broker.ip", "localhost");
        //System.setProperty("commons.eventbus.url",  "");
        System.setProperty("commons.settings.hotswap", "true");
        System.setProperty("datastore.client.class", "org.eclipse.kapua.service.datastore.client.rest.RestDatastoreClient");
        //System.setProperty("datastore.elasticsearch.nodes",  "");
        //System.setProperty("datastore.elasticsearch.port",  "");
        //System.setProperty("datastore.index.prefix", "");
        //System.setProperty("DOCKER_CERT_PATH",  "");
        //System.setProperty("DOCKER_HOST",  "");
        //System.setProperty("kapua.config.url", "");
        //System.setProperty("kapua.config.url", "broker.setting/kapua-broker-setting-1.properties");
        System.setProperty("org.eclipse.kapua.qa.broker.extraStartupDelay", "5");
        System.setProperty("org.eclipse.kapua.qa.datastore.extraStartupDelay", "5");
        //System.setProperty("test.h2.server", "false");
        System.setProperty("test.type", "integration");
        //System.setProperty("test.type", "unit");
    }

}