/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.app.console.module.device.client.device.command;

import org.eclipse.kapua.app.console.module.api.client.ui.view.descriptor.AbstractTabDescriptor;
import org.eclipse.kapua.app.console.module.api.shared.model.GwtSession;
import org.eclipse.kapua.app.console.module.device.client.device.DeviceView;
import org.eclipse.kapua.app.console.module.device.shared.model.GwtDevice;

public class DeviceTabCommandDescriptor extends AbstractTabDescriptor<GwtDevice, DeviceTabCommand, DeviceView> {

    @Override
    public DeviceTabCommand getTabViewInstance(DeviceView view, GwtSession currentSession) {
        return new DeviceTabCommand(currentSession);
    }

    @Override
    public String getViewId() {
        return "device.command";
    }

    @Override
    public Integer getOrder() {
        return 600;
    }

    @Override
    public Boolean isEnabled(GwtSession currentSession) {
        return true;
    }
}