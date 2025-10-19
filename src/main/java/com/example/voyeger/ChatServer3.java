package com.example.voyeger;

public class ChatServer3 {
    public static void main(String[] args) {
        System.out.println("=== Starting Chat Server 3 on Port 8083 ===");
        ChatServer server = new ChatServer(8083);
        server.start();
    }
}