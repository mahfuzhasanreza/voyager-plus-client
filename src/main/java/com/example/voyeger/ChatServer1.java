package com.example.voyeger;

public class ChatServer1 {
    public static void main(String[] args) {
        System.out.println("=== Starting Chat Server 1 on Port 8081 ===");
        ChatServer server = new ChatServer(8081);
        server.start();
    }
}