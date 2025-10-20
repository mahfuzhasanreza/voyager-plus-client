package com.example.voyeger;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PrivateChatServer {
    private static final int PORT = 8888;
    private static Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private static ServerSocket serverSocket;
    private static boolean running = true;

    public static void main(String[] args) {
        System.out.println("Private Chat Server starting on port " + PORT);
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running and waiting for connections...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    ClientHandler handler = new ClientHandler(clientSocket);
                    new Thread(handler).start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public static void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler handler : connectedClients.values()) {
                handler.disconnect();
            }
            connectedClients.clear();
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Get username
                out.println("ENTER_USERNAME");
                username = in.readLine();

                if (username == null || username.trim().isEmpty()) {
                    disconnect();
                    return;
                }

                synchronized (connectedClients) {
                    connectedClients.put(username, this);
                }

                out.println("SUCCESS:Connected as " + username);
                System.out.println("User registered: " + username);

                // Broadcast online users
                broadcastOnlineUsers();

                // Handle incoming messages
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received from " + username + ": " + message);

                    if (message.startsWith("PRIVATE:")) {
                        // Format: PRIVATE:recipient:message
                        String[] parts = message.substring(8).split(":", 2);
                        if (parts.length == 2) {
                            String recipient = parts[0];
                            String msg = parts[1];
                            sendPrivateMessage(recipient, msg);
                        }
                    } else if (message.equals("GET_USERS")) {
                        sendOnlineUsers();
                    } else if (message.equals("DISCONNECT")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Client handler error for " + username + ": " + e.getMessage());
            } finally {
                disconnect();
            }
        }

        private void sendPrivateMessage(String recipient, String message) {
            ClientHandler recipientHandler = connectedClients.get(recipient);
            if (recipientHandler != null) {
                recipientHandler.out.println("MESSAGE:" + username + ":" + message);
                out.println("SENT:" + recipient + ":" + message);
                System.out.println("Message sent from " + username + " to " + recipient);
            } else {
                out.println("ERROR:User " + recipient + " is not online");
            }
        }

        private void sendOnlineUsers() {
            StringBuilder users = new StringBuilder("USERS:");
            synchronized (connectedClients) {
                for (String user : connectedClients.keySet()) {
                    if (!user.equals(username)) {
                        users.append(user).append(",");
                    }
                }
            }
            out.println(users.toString());
        }

        private void broadcastOnlineUsers() {
            synchronized (connectedClients) {
                for (ClientHandler handler : connectedClients.values()) {
                    handler.sendOnlineUsers();
                }
            }
        }

        public void disconnect() {
            try {
                if (username != null) {
                    connectedClients.remove(username);
                    System.out.println("User disconnected: " + username);
                    broadcastOnlineUsers();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error disconnecting client: " + e.getMessage());
            }
        }
    }
}

