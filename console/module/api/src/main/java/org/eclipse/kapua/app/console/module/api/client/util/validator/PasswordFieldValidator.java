/*******************************************************************************
 * Copyright (c) 2017, 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.app.console.module.api.client.util.validator;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class PasswordFieldValidator extends TextFieldValidator {

    private final int minLength;

    public PasswordFieldValidator(TextField<String> passwordField, int minLength) {
        super(passwordField, FieldType.PASSWORD);
        textField.setRegex(null);
        this.minLength = minLength;
        this.textField.getMessages().setRegexText(this.textField.getMessages().getRegexText().replace("{0}", Integer.toString(minLength)));
        this.textField.setToolTip(this.textField.getToolTip().getToolTipConfig().getText().replace("{0}", Integer.toString(minLength)));
    }

    public String validate(Field<?> field, String value) {

        // if the field is not dirty, ignore the validation
        // this is needed for the update flow, in which we do not show the whole password
        boolean isDirty = textField.isDirty();
        if (!isDirty) {
            textField.setRegex(null);
            textField.setMinLength(0);
            return null;
        }

        if (textFieldType.getRegex() != null) {
            textField.setRegex(textFieldType.getRegex());
        }
        textField.setMinLength(minLength);
        return super.validate(field, value);
    }

}
