/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others
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

import org.eclipse.kapua.commons.setting.AbstractKapuaSetting;

/**
 * Broker setting implementation.<br>
 * This class handles settings for the {@link ConsumerSettingKey}.
 */
public final class ConsumerSetting extends AbstractKapuaSetting<ConsumerSettingKey> {

    private static final String CONFIG_RESOURCE_NAME = "kapua-broker-setting.properties";

    private static ConsumerSetting instance;

    private ConsumerSetting() {
        super(CONFIG_RESOURCE_NAME);
    }

    /**
     * Return the broker setting instance (singleton)
     */
    public static ConsumerSetting getInstance() {
        synchronized (ConsumerSetting.class) {
            if (instance == null) {
                instance = new ConsumerSetting();
            }
            return instance;
        }
    }

    /**
     * Allow re-setting the global instance
     * <p>
     * This method clears out the internal global instance in order to let the next call
     * to {@link #getInstance()} return a fresh instance.
     * </p>
     * <p>
     * This may be helpful for unit tests which need to change system properties for testing
     * different behaviors.
     * </p>
     */
    public static void resetInstance() {
        synchronized (ConsumerSetting.class) {
            instance = null;
        }
    }
}
