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
package org.eclipse.kapua.consumer.log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalUDPServer {

    public static final Logger logger = LoggerFactory.getLogger(LocalUDPServer.class);

    private static final int PORT = 5555;
    private static final int BUFFER_SIZE = 4096;

    private static DatagramSocket socket;

    private LocalUDPServer() {
    }

    public static void main(String[] argv) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("Shutdown Hook is running !");
                socket.close();
            }
        });

        socket = new DatagramSocket(PORT);

        byte[] buffer = new byte[BUFFER_SIZE];

        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        while (true) {
            socket.receive(request);
            logger.info("UDP server received data: {}", new String(request.getData()));
        }
    }

}
