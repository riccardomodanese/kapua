/*******************************************************************************
 * Copyright (c) 2016, 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua;

import org.eclipse.kapua.qa.markers.junit.JUnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(JUnitTests.class)
public class KapuaExceptionTest {

    @Test
    public void test() {
        KapuaException ke = new KapuaException(KapuaErrorCodes.ENTITY_NOT_FOUND, "user", 1);
        Assert.assertEquals("The entity of type user with id/name 1 was not found.", ke.getMessage());

        ke = KapuaException.internalError("ciao");
        Assert.assertEquals("An internal error occurred: ciao.", ke.getMessage());

        ke = KapuaException.internalError(new NullPointerException());
        Assert.assertEquals("An internal error occurred: java.lang.NullPointerException.", ke.getMessage());

        ke = new KapuaException((KapuaErrorCode) () -> "MISSING", "abc", 1);
        Assert.assertEquals("Error: abc, 1", ke.getMessage());
    }

    @Test
    public void missingBundleTest() {
        KapuaExceptionWithoutBundle exceptionWithoutBundle = new KapuaExceptionWithoutBundle(KapuaErrorCodes.ILLEGAL_STATE, "param1", "param2");
        Assert.assertEquals("Error: param1, param2", exceptionWithoutBundle.getMessage());
    }

    @Test
    public void missingErrorCodeTest() {
        KapuaException exceptionWithoutCode = new KapuaException(MissingKapuaErrorCodes.NOT_EXISTING, "param1", "param2");
        Assert.assertEquals("Error: param1, param2", exceptionWithoutCode.getMessage());
    }
}
