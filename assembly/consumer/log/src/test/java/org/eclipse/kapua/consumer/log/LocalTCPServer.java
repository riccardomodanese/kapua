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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalTCPServer {

    private static final Logger logger = LoggerFactory.getLogger(LocalTCPServer.class);

    private static final int PORT = 5556;

    private static ServerSocket serverSocket;

    private LocalTCPServer() {
    }

    public static void main(String[] argv) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("Shutdown Hook is running !");
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        logger.error("Error while closing socket", e);
                    }
                }
            }
        });

        serverSocket = new ServerSocket(PORT);
        while (true) {
            new Thread(new SocketConsumer(serverSocket.accept())).start();
        }
    }

}

class SocketConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LocalTCPServer.class);

    private Socket socket;

    public SocketConsumer(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        InputStream input;
        try {
            input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String str = reader.readLine();
            while (str!=null&& str.length()>0) {
                logger.info("TCP server received data: {}", str);
                str = reader.readLine();
            }
            socket.close();
        } catch (IOException e) {
            logger.error("Error while closing socket", e);
        }
    }

}