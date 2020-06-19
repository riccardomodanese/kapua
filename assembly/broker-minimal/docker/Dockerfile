###############################################################################
# Copyright (c) 2018, 2020 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech - initial API and implementation
#
###############################################################################

FROM @docker.account@/java-base

COPY maven /

USER 0

RUN useradd -u 1002 -g 0 -d '/var/opt/activemq' -s '/sbin/nologin' activemq && \
    chown -R activemq:root /var/opt/activemq /opt/activemq && \
    chmod -R g=u /var/opt/activemq /opt/activemq

ENV ACTIVEMQ_OPTS ""

EXPOSE 1883 5672 8883 8161

VOLUME /opt/activemq/data

USER 1002

ENTRYPOINT /var/opt/activemq/run-broker
