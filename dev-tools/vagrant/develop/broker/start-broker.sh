#!/usr/bin/env bash
#*******************************************************************************
# Copyright (c) 2016, 2021 Eurotech and/or its affiliates and others
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#     Eurotech - initial API and implementation
#*******************************************************************************

echo '### starting telemetry Artemis'
cd /usr/local/artemis/kapua-telemetry

./update-kapua-jars-cfg.sh

#bin/artemis run
bin/artemis-service start

echo '### starting telemetry Artemis 1'
cd /usr/local/artemis/kapua-telemetry-1

./update-kapua-jars-cfg.sh

#bin/artemis run
bin/artemis-service start

echo '### starting telemetry Artemis 2'
cd /usr/local/artemis/kapua-telemetry-2

./update-kapua-jars-cfg.sh

#bin/artemis run
bin/artemis-service start
