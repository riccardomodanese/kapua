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
 *******************************************************************************/
package org.eclipse.kapua.commons.jpa;

import org.eclipse.kapua.commons.service.internal.AbstractKapuaService;
import org.eclipse.kapua.commons.service.internal.cache.EntityCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEntityCacheFactory implements CacheFactory {

    private static final Logger logger = LoggerFactory.getLogger(AbstractKapuaService.class);

    private String idCacheName;

    public AbstractEntityCacheFactory(String idCacheName) {
        this.idCacheName = idCacheName;
    }

    public String getEntityIdCacheName() {
        return idCacheName;
    }

    @Override
    public EntityCache createCache() {
        try {
            return new EntityCache(getEntityIdCacheName());
        }
        catch (Throwable t) {
            logger.error("\n\n============\n============\nError initializing locator... {}\n============\n============\n", t.getMessage(), t);
            throw t;
        }
    }
}
