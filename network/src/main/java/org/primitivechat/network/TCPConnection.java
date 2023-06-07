package org.primitivechat.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final TCPConnectionListener listener;

    public TCPConnection(String ipAddr, int port, TCPConnectionListener listener) throws IOException {
        this(new Socket(ipAddr, port), listener);
    }

    public TCPConnection(Socket sSocket, TCPConnectionListener lListener) throws IOException {
        this.listener = lListener;
        this.socket = sSocket;
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8));
        this.rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.onConnect(TCPConnection.this);
                    while (!rxThread.isInterrupted()) {
                            listener.onMessageReceived(TCPConnection.this, in.readLine());
                    }
                } catch (IOException e) {
                    listener.onException(TCPConnection.this, e);
                } finally {
                    listener.onDisconnect(TCPConnection.this);
                }
            }
        });
        this.rxThread.start();
    }

    public synchronized void sendMessage(String msg) {
        try {
            out.write(msg + "\r\n");
            out.flush();
        } catch (IOException e) {
            listener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        try {
            rxThread.interrupt();
            socket.close();
        } catch (IOException e) {
            listener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return String.format("TCPConnection: %s:%s", socket.getInetAddress(), socket.getPort());
    }
}
