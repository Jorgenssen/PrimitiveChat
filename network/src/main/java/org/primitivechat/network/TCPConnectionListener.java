package org.primitivechat.network;

public interface TCPConnectionListener {
    void onConnect(TCPConnection tcpConnection);
    void onMessageReceived(TCPConnection tcpConnection, String message);
    void onDisconnect(TCPConnection tcpConnection);
    void onException(TCPConnection tcpConnection, Exception exception);
}
