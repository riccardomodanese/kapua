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
package org.eclipse.kapua.consumer.commons.plugin;

/**
 * A provider for {@link ProtocolDescriptor} instances
 */
public interface ProtocolDescriptorProvider {

    /**
     * Get a {@link ProtocolDescriptor} for the given transport name
     * 
     * @param connectorName
     *            The name of the connector to lookup
     * @return The connector descriptor, or {@code null} if this provider could not find one
     */
    public ProtocolDescriptor getDescriptor(String connectorName);
}
