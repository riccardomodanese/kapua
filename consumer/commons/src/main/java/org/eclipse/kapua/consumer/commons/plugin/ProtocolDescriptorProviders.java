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

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for working with {@link ProtocolDescriptorProvider} instances
 */
public final class ProtocolDescriptorProviders {

    private static final Logger logger = LoggerFactory.getLogger(ProtocolDescriptorProviders.class);

    private ProtocolDescriptorProviders() {
    }

    private static ProtocolDescriptorProvider provider;

    /**
     * Get a {@link ProtocolDescriptorProvider} instance
     *
     * @return An instance of {@link ProtocolDescriptorProvider}, never returns {@code null}
     */
    public static ProtocolDescriptorProvider getInstance() {
        synchronized (ProtocolDescriptorProviders.class) {

            if (provider != null) {
                return provider;
            }

            provider = locateProvider();

            return provider;
        }
    }

    /**
     * Get a {@link ProtocolDescriptor} using the default instance
     * 
     * @param connectorName
     *            the connector name to lookup
     * @return The connector descriptor, may be {@code null}
     */
    public static ProtocolDescriptor getDescriptor(String connectorName) {
        return getInstance().getDescriptor(connectorName);
    }

    /**
     * Locate an instance of {@link ProtocolDescriptorProvider}
     *
     * @return An instance of {@link ProtocolDescriptorProvider}, never returns {@code null}
     */
    private static ProtocolDescriptorProvider locateProvider() {
        final ProtocolDescriptorProvider provider = locateProviderFromServices();
        if (provider != null) {
            return provider;
        }

        return new DefaultProtocolDescriptionProvider();
    }

    /**
     * Locate provider instances using {@link ServiceLoader}
     *
     * @return An instance of {@link ProtocolDescriptorProvider}, or {@code null} if none was found
     */
    private static ProtocolDescriptorProvider locateProviderFromServices() {
        ProtocolDescriptorProvider result = null;

        for (final ProtocolDescriptorProvider provider : ServiceLoader.load(ProtocolDescriptorProvider.class)) {
            if (result == null) {
                result = provider;
            } else {
                logger.warn("Multiple instances of {} found via ServiceLoader - first: {}, additional: {}", result, provider);
            }
        }

        return result;
    }
}
