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
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.consumer.commons.plugin;

/**
 * Allowed message types
 */
public enum MessageType {

    /**
     * Application message type
     */
    APP,
    /**
     * Birth message type
     */
    BIRTH,
    /**
     * Disconnect message type
     */
    DISCONNECT,
    /**
     * Missing message type
     */
    MISSING,
    /**
     * Notify message type
     */
    NOTIFY,
    /**
     * Data message type
     */
    DATA

}
