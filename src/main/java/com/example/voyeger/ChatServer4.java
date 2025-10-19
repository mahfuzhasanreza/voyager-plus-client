package com.example.voyeger;

public class ChatServer4 {
    public static void main(String[] args) {
        System.out.println("=== Starting Chat Server 4 on Port 8084 ===");
        ChatServer server = new ChatServer(8084);
        server.start();
    }
}