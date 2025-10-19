package com.example.voyeger;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private int port;
    private Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private List<ChatServer> otherServers = new ArrayList<>();
    private ExecutorService executor = Executors.newCachedThreadPool();
    private String serverName;

    public ChatServer(int port) {
        this.port = port;
        this.serverName = "Server-" + port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(serverName + " started on port " + port);
            logToAllServers(serverName + " is now ONLINE");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                executor.execute(clientHandler);
                System.out.println(serverName + ": New client connected from " +
                        clientSocket.getInetAddress().getHostAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        String formattedMessage = "[" + serverName + "] " + message;
        System.out.println(formattedMessage);

        // Broadcast to all clients on this server
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }

        // Broadcast to other servers
        for (ChatServer server : otherServers) {
            server.receiveMessageFromOtherServer(message, this.serverName);
        }
    }

    public void receiveMessageFromOtherServer(String message, String sourceServer) {
        String formattedMessage = "[Cross-Server from " + sourceServer + "] " + message;
        System.out.println(formattedMessage);

        // Broadcast to all clients on this server
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void logToAllServers(String logMessage) {
        for (ChatServer server : otherServers) {
            System.out.println("[Server Network] " + logMessage);
        }
    }

    public void addOtherServer(ChatServer server) {
        otherServers.add(server);
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println(serverName + ": Client disconnected. Remaining clients: " + clients.size());
    }

    public String getServerName() {
        return serverName;
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private String currentRoom;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(server.getServerName() + " Received raw: " + message);

                if (message.startsWith("JOIN:")) {
                    String[] parts = message.substring(5).split(":");
                    if (parts.length == 2) {
                        this.username = parts[0];
                        this.currentRoom = parts[1];
                        String joinMessage = "USER_JOIN:" + username + ":" + currentRoom;
                        server.broadcastMessage(joinMessage, this);
                        System.out.println(server.getServerName() + ": " + username + " joined room: " + currentRoom);
                    }
                } else if (message.startsWith("MESSAGE:")) {
                    server.broadcastMessage(message, this);
                    System.out.println(server.getServerName() + ": Message from " + username + ": " + message);
                } else if (message.startsWith("FILE:")) {
                    server.broadcastMessage(message, this);
                    System.out.println(server.getServerName() + ": File shared by " + username + ": " + message);
                } else if (message.startsWith("LEAVE:")) {
                    String leaveMessage = "USER_LEAVE:" + username;
                    server.broadcastMessage(leaveMessage, this);
                    System.out.println(server.getServerName() + ": " + username + " left the room");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(server.getServerName() + ": Client connection error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server.removeClient(this);
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            System.out.println(server.getServerName() + " Sent to client: " + message);
        }
    }
}