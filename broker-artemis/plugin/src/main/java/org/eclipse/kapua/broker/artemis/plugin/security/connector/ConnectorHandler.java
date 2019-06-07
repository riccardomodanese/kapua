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
package org.eclipse.kapua.broker.artemis.plugin.security.connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.ActiveMQExceptionType;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to sync connectors
 *
 */
public class ConnectorHandler {

    private static Logger logger = LoggerFactory.getLogger(AcceptorHandler.class);

    private static final String DEFAULT_FACTORY_CLASS = "org.apache.activemq.artemis.core.server.ConnectorServiceFactory";

    private ActiveMQServer server;
    //TODO should take care of concurrency?
    private Map<String, String> definedConnectors;

    public ConnectorHandler(ActiveMQServer server) {
        this.server = server;
        definedConnectors = new HashMap<>();
    }

    /**
     * Add connector
     * @param name connector name
     * @param uri connector uri
     * @return the previous connector uri (if present)
     * @throws Exception
     */
    public String addAcceptor(String name, String uri) throws Exception {
        String previousAcceptor = definedConnectors.put(name, uri);
        syncAcceptors();
        return previousAcceptor;
    }

    /**
     * Remove connector
     * 
     * @param name connector name
     * @return the current connector uri (if present)
     * @throws Exception
     */
    public String removeAcceptor(String name) throws Exception {
        String currentAcceptor = definedConnectors.remove(name);
        syncAcceptors();
        return currentAcceptor;
    }

    private void syncAcceptors() throws Exception {
        logger.info("Init connectors... server started: {} - {}", server.isStarted(), server.getState());
        if (server.isStarted()) {
            List<String> connectorToRemove = new ArrayList<>();
            server.getConfiguration().getConnectorConfigurations().forEach((name, tc) -> {
                String connectorName = tc.getName();
                logger.info("Checking connector {}", connectorName);
                if (definedConnectors.get(connectorName) == null) {
                    connectorToRemove.add(connectorName);
                    logger.info("Adding connector {} to the remove list", connectorName);
                }
                else {
                    logger.info("Leaving connector {} running", connectorName);
                }
            });
            connectorToRemove.forEach(connectorName -> {
                logger.info("Stopping connector {}...", connectorName);
                try {
                    server.getActiveMQServerControl().destroyConnectorService(connectorName);
//                    server.getConnectorsService().destroyService(connectorName);
                } catch (Exception e) {
                    logger.error("Error stopping connector {}... Error: {}", connectorName, e.getMessage(), e);
                }
                logger.info("Stopping connector {}... DONE", connectorName);
            });

            Map<String, String> configuredConnectors = getConnectors();
            definedConnectors.forEach((name, uri) -> {
                logger.info("Adding connector... name: {} - uri: {}", name, uri);
                try {
                    if (configuredConnectors.get(name) == null) {
                        Map<String, Object> parameters = transforUriToMap(uri);
                        server.getActiveMQServerControl().createConnectorService(name, DEFAULT_FACTORY_CLASS, parameters);
                    }
                } catch (Exception e) {
                    logger.error("Error initializing connector {}... Error: {}", name, e.getMessage(), e);
                }
            });
        }

        logAcceptorConfigurations();
    }

    private Map<String, Object> transforUriToMap(String uri) throws ActiveMQException {
        Map<String, Object> parameters = new HashMap<>();
        if (uri!=null) {
            String[] parametersStr = uri.split("&");
            for (String parameter : parametersStr) {
                if (parameter==null) {
                    //TODO find proper exception
                    throw new ActiveMQException(ActiveMQExceptionType.SECURITY_EXCEPTION, "Internal error!");
                }
                String[] tmp = parameter.split("=");
                parameters.put(tmp[0], tmp[1]);
            }
        }
        return parameters;
    }

    private Map<String, String> getConnectors() {
        List<String> connectorsList = Arrays.asList(server.getActiveMQServerControl().getConnectorServices());
        Map<String, String> map = connectorsList.stream()
            .collect(Collectors.toMap(String::toString, tc -> tc));
        return map;
    }

    private void logAcceptorConfigurations() {
        logger.info("Defined connector configurations...");
        server.getConfiguration().getConnectorConfigurations().forEach((name, tc) -> {
            logger.info("name: {} - factory: {}", tc.getName(), tc.getFactoryClassName());
            logger.info("\tparams");
            tc.getParams().forEach((key, value) -> logger.info("\t\t{} : {}", key, value));
            logger.info("\textraparams");
            tc.getExtraParams().forEach((key, value) -> logger.info("\t\t{} : {}", key, value));
        });
    }
}
