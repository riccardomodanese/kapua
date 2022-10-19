/*******************************************************************************
 * Copyright (c) 2017, 2022 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.certificate.internal.setting;

import org.eclipse.kapua.commons.setting.AbstractKapuaSetting;
import org.eclipse.kapua.commons.setting.system.SystemSetting;

/**
 * Kapua certificate setting implementation.
 *
 * @since 1.0
 *
 */
public class KapuaCertificateSetting extends AbstractKapuaSetting<KapuaCertificateSettingKeys> {

    private static final String CERTIFICATE_SETTING_PROPERTIES = "kapua-certificate-setting.properties";

    private static KapuaCertificateSetting instance;

    /**
     * Construct a new Kapua certificate setting reading settings from {@link KapuaCertificateSetting#CERTIFICATE_SETTING_PROPERTIES}
     */
    private KapuaCertificateSetting() {
        super(CERTIFICATE_SETTING_PROPERTIES);
    }

    /**
     * Return the Kapua certificate setting instance (singleton)
     *
     * @return
     */
    public static KapuaCertificateSetting getInstance() {
       synchronized (SystemSetting.class) {
            if (instance == null) {
                instance = new KapuaCertificateSetting();
            }
        }
        return instance;
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
        synchronized (KapuaCertificateSetting.class) {
            instance = null;
        }
    }
}
