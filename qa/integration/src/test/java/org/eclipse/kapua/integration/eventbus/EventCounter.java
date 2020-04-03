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
package org.eclipse.kapua.integration.eventbus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

public class EventCounter {

    private Map<String, AtomicInteger> eventsMapRaised = new HashMap<>();
    private Map<String, AtomicInteger> eventsMapConsumed = new HashMap<>();

    private static final EventCounter INSTANCE = new EventCounter();

    public static EventCounter getInstance() {
        return INSTANCE;
    }

    public void incConsumed(String address, int count) {
        inc(eventsMapConsumed, address, count);
    }

    public void incRaised(String address, int count) {
        inc(eventsMapRaised, address, count);
    }

    private void inc(Map<String, AtomicInteger> map, String address, int count) {
        AtomicInteger value = map.get(address);
        if (value==null) {
            synchronized (map) {
                value = map.get(address);
                if (value == null) {
                    value = new AtomicInteger();
                    map.put(address, value);
                }
            }
        }
        value.addAndGet(count);
    }

    public void check(String address, int raised, int consumed) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Assert.assertEquals("Wrong raised messages", raised, eventsMapRaised.get(address) != null ? eventsMapRaised.get(address).intValue() : -1);
        Assert.assertEquals("Wrong consume messages", consumed, eventsMapConsumed.get(address) != null ? eventsMapConsumed.get(address).intValue() : -1);
    }
}
