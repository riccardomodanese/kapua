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
package org.eclipse.kapua.consumer.lifecycle.setting;

import org.eclipse.kapua.commons.setting.AbstractKapuaSetting;

/**
 * Broker setting implementation.<br>
 * This class handles settings for the {@link ConsumerSettingKey}.
 */
public final class LifecycleConsumerSetting extends AbstractKapuaSetting<LifecycleConsumerSettingKey> {

    private static final String CONFIG_RESOURCE_NAME = "kapua-lifecycle-consumer-setting.properties";

    private static LifecycleConsumerSetting instance;

    private LifecycleConsumerSetting() {
        super(CONFIG_RESOURCE_NAME);
    }

    /**
     * Return the broker setting instance (singleton)
     */
    public static LifecycleConsumerSetting getInstance() {
        synchronized (LifecycleConsumerSetting.class) {
            if (instance == null) {
                instance = new LifecycleConsumerSetting();
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
        synchronized (LifecycleConsumerSetting.class) {
            instance = null;
        }
    }
}
