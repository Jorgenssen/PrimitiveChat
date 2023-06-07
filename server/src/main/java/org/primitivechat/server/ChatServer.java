package org.primitivechat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primitivechat.common.PropertiesLoader;
import org.primitivechat.network.TCPConnection;
import org.primitivechat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionListener {
    private static final Logger log = LogManager.getLogger(ChatServer.class);

    private static final int PORT = Integer.parseInt(PropertiesLoader.getProperties()
            .getProperty("server.port", String.valueOf(8189)));
    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    public static void main(String[] args) {
        new ChatServer();
    }

    private ChatServer() {
        log.info("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try {
                    new TCPConnection(serverSocket.accept(), this);
                } catch (IOException e) {
                    log.error(String.format("TCPConnection exception: %s", e));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnect(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        distributeMessage(String.format("Client connected: %s", tcpConnection));
    }

    @Override
    public synchronized void onMessageReceived(TCPConnection tcpConnection, String message) {
        if (!connections.isEmpty()) {
            distributeMessage(message);
        }
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        distributeMessage(String.format("Client disconnected: %s", tcpConnection));
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception exception) {
        log.warn(String.format("TCPConnection exception: %s", exception));
    }

    private void distributeMessage(String msg) {
        if (msg != null) {
            log.info(msg);
        }
        if (!connections.isEmpty()) {
            connections.forEach(c -> c.sendMessage(msg));
        }
    }
}
