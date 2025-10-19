package com.example.voyeger;

import java.util.*;

public class ServerCoordinator {
    private static final int[] PORTS = {8081, 8082, 8083, 8084, 8085};

    public static void main(String[] args) {
        List<ChatServer> servers = new ArrayList<>();

        // Create and start all servers
        for (int port : PORTS) {
            ChatServer server = new ChatServer(port);
            servers.add(server);
            new Thread(() -> {
                System.out.println("Starting server on port " + port);
                server.start();
            }).start();
        }

        // Wait a bit for servers to start, then connect them
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Connect servers in a mesh network
        for (int i = 0; i < servers.size(); i++) {
            for (int j = 0; j < servers.size(); j++) {
                if (i != j) {
                    servers.get(i).addOtherServer(servers.get(j));
                    System.out.println("Connected server " + PORTS[i] + " to " + PORTS[j]);
                }
            }
        }

        System.out.println("All 5 chat servers are started and connected in mesh network.");
    }
}