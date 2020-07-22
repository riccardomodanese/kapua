###############################################################################
# Copyright (c) 2017, 2019 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech - initial API and implementation
###############################################################################
@security
@miscAuthorization
@integration
@env_none

Feature: Misc Authorization functionality CRUD tests

Scenario: Permission factory sanity checks
    Then The permission factory returns sane results
    Then I can compare permission objects
