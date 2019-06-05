#!/usr/bin/env bash
#*******************************************************************************
# Copyright (c) 2011, 2018 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech - initial API and implementation
#*******************************************************************************

BROKER_PLUGIN_DEPENDENCY_DIR="/kapua/broker-artemis/plugin/target/dependency";
BROKER_PLUGIN_TARGET_DIR="/kapua/broker-artemis/plugin/target";

echo "    Copy dependencies from broker-plugin..."
for name in $(ls  ${BROKER_PLUGIN_DEPENDENCY_DIR} | grep jar | grep -Ev 'qa|jaxb-|artemis|netty|commons|jboss|geronimo|wildfly|johnzon|jgroups|sources');
    do
        echo "        Create symbolic link from ./lib/${name}  ${BROKER_PLUGIN_DEPENDENCY_DIR}/${name}";
        ln -s  ${BROKER_PLUGIN_DEPENDENCY_DIR}/${name} ./lib/${name};
    done;
echo "    Copy dependencies from broker-plugin... DONE!"

echo "    Copy broker-plugin..."
for name in $(ls  ${BROKER_PLUGIN_TARGET_DIR} | grep jar | grep -Ev 'qa|jaxb-|sources');
    do
        echo "        Create symbolic link from ./lib/${name}  ${BROKER_PLUGIN_TARGET_DIR}/${name}";
        ln -s  ${BROKER_PLUGIN_TARGET_DIR}/${name} ./lib/${name};
    done;
echo "    Copy broker-plugin... DONE!"

#ln -s /kapua/broker-artemis/plugin/target/kapua-broker-artemis-plugin-1.1.0-SNAPSHOT.jar lib/kapua-broker-artemis-plugin-1.1.0-SNAPSHOT.jar;