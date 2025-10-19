package com.example.voyeger;

public class ChatServer2 {
    public static void main(String[] args) {
        System.out.println("=== Starting Chat Server 2 on Port 8082 ===");
        ChatServer server = new ChatServer(8082);
        server.start();
    }
}