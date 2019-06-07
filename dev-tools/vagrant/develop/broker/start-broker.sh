#!/usr/bin/env bash
#*******************************************************************************
# Copyright (c) 2011, 2019 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
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