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
package org.eclipse.kapua.consumer.commons.setting;

import org.eclipse.kapua.commons.setting.SettingKey;

/**
 * Settings
 */
public enum ConsumerSettingKey implements SettingKey {
    /**
     * Allow disabling the default connector descriptor
     */
    DISABLE_DEFAULT_PROTOCOL_DESCRIPTOR("protocol_descriptor.default.disable"),
    /**
     * A URI to a configuration file for providing additional {@link ConnectorDescriptor} configurations
     */
    CONFIGURATION_URI("protocol_descriptor.configuration.uri"),
    /**
     * Jaxb context provider class name
     */
    JAXB_CONTEXT_CLASS_NAME("jaxb_context_class_name");

    private String key;

    private ConsumerSettingKey(String key) {
        this.key = key;
    }

    @Override
    public String key() {
        return key;
    }
}
