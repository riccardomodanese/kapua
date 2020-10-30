/*******************************************************************************
 * Copyright (c) 2018, 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.authentication.shiro.exceptions;

import org.eclipse.kapua.KapuaRuntimeException;
import org.eclipse.kapua.service.authentication.KapuaAuthenticationErrorCodes;

public class AuthenticationRuntimeException extends KapuaRuntimeException {

    public AuthenticationRuntimeException(KapuaAuthenticationErrorCodes code) {
        super(code);
    }

    public AuthenticationRuntimeException(KapuaAuthenticationErrorCodes code, Object... arguments) {
        super(code, arguments);
    }

    public AuthenticationRuntimeException(KapuaAuthenticationErrorCodes code, Throwable cause, Object... arguments) {
        super(code, cause, arguments);
    }
}
