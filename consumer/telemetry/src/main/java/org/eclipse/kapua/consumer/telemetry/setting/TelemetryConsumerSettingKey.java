/*******************************************************************************
 * Copyright (c) 2017, 2020 Red Hat Inc and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.consumer.telemetry.setting;

import org.eclipse.kapua.commons.setting.SettingKey;

/**
 * Settings
 */
public enum TelemetryConsumerSettingKey implements SettingKey {
    /**
     * Jaxb context provider class name
     */
    JAXB_CONTEXT_CLASS_NAME("consumer_telemetry.jaxb_context_class_name");

    private String key;

    private TelemetryConsumerSettingKey(String key) {
        this.key = key;
    }

    @Override
    public String key() {
        return key;
    }
}
