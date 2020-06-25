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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

/**
 * Lifecycle application container main class
 *
 */
@ImportResource({"classpath:spring/applicationContext.xml"})
@PropertySource(value = "classpath:spring/application.properties")
@SpringBootApplication
public class LogApplication {

    public LogApplication() {
    }

    public void doNothing() {
        //spring needs a public constructor but our checkstyle doesn't allow a class with only static methods and a public constructor
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(LogApplication.class, args);
    }

}
