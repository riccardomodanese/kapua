/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.eclipseiot.setting;

import org.eclipse.kapua.commons.setting.SettingKey;

/**
 * Broker settings
 */
public enum EclipseiotSettingKey implements SettingKey {

    /**
     * Hono connection configuration - username
     */
    HONO_USERNAME("eclipseiot.hono.username"),
    /**
     * Hono connection configuration - password
     */
    HONO_PASSWORD("eclipseiot.hono.password"),
    /**
     * Hono connection configuration - tenant id (comma separated tenant id list)
     */
    HONO_TENANT_ID("eclipseiot.hono.tenant_id"),
    /**
     * Hono connection configuration - host
     */
    HONO_HOST("eclipseiot.hono.host"),
    /**
     * Hono connection configuration - port
     */
    HONO_PORT("eclipseiot.hono.port"),
    /**
     * Hono connection configuration - trutstore file
     */
    HONO_TRUSTSTORE_FILE("eclipseiot.hono.truststore_file"),
    /**
     * Converter class
     */
    CONVERTER_CLASS_NAME("eclipseiot.converter.class"),
    /**
     * Processor class
     */
    PROCESSOR_CLASS_NAME("eclipseiot.processor.class");

    private String key;

    private EclipseiotSettingKey(String key) {
        this.key = key;
    }

    @Override
    public String key() {
        return key;
    }
}
