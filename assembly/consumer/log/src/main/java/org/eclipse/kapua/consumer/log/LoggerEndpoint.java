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
package org.eclipse.kapua.consumer.log;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerEndpoint {

    public static final Logger logger = LoggerFactory.getLogger(LoggerEndpoint.class);

    public void logInMessage(Exchange exchange, Object value) {
        logger.info("Received message from syslog: 1) {}", exchange.getIn().getBody());
    }

    public void logInMessageAfterMarshal(Exchange exchange, Object value) {
        logger.info("Received message from syslog: 2) {}", exchange.getIn().getBody());
    }

    public void logOutMessage(Exchange exchange, Object value) {
        exchange.getIn().setBody(new String((byte[])exchange.getIn().getBody()));
        logger.info("Forwarding message to external server: 1) {}", exchange.getIn().getBody());
    }

    public void logOutMessageAfterMarshal(Exchange exchange, Object value) {
        logger.info("Forwarding message to external server: 2) {}", exchange.getIn().getBody());
    }
}
