package org.primitivechat.client;

import org.primitivechat.common.PropertiesLoader;
import org.primitivechat.network.TCPConnection;
import org.primitivechat.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDR = PropertiesLoader.getProperties()
            .getProperty("server.address", "127.0.0.1");
    private static final int PORT = Integer.parseInt(PropertiesLoader.getProperties()
            .getProperty("server.port", String.valueOf(8189)));
    private static final int WIDTH = Integer.parseInt(PropertiesLoader.getProperties()
            .getProperty("client.window.width", String.valueOf(640)));
    public static final int HEIGHT = Integer.parseInt(PropertiesLoader.getProperties()
            .getProperty("client.window.height", String.valueOf(480)));
    private final JTextArea chatTextArea = new JTextArea();
    private final JTextField fieldNickName = new JTextField("Anonymous");
    private final JTextField fieldPrompt = new JTextField();
    private TCPConnection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientWindow::new);
    }

    private ClientWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);

        chatTextArea.setEditable(false);
        chatTextArea.setLineWrap(true);
        add(chatTextArea, BorderLayout.CENTER);

        add(fieldNickName, BorderLayout.NORTH);

        fieldPrompt.addActionListener(this);
        add(fieldPrompt, BorderLayout.SOUTH);

        setVisible(true);
        try {
            this.connection = new TCPConnection(IP_ADDR, PORT, this);
        } catch (IOException e) {
            printMessage(String.format("Connection exception: %s", e));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldPrompt.getText();
        if (!msg.isBlank()) {
            fieldPrompt.setText(null);
            connection.sendMessage(String.format("%s: %s", fieldNickName.getText(), msg));
        }

    }

    @Override
    public void onConnect(TCPConnection tcpConnection) {
        printMessage("Connection established");
    }

    @Override
    public void onMessageReceived(TCPConnection tcpConnection, String message) {
        printMessage(message);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMessage("Connection closed");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception exception) {
        printMessage(String.format("Connection exception: %s", exception));
    }

    private synchronized void printMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            chatTextArea.append(msg + "\n");
            chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());
        });
    }
}
