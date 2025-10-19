package com.example.voyeger;

public class ChatServer5 {
    public static void main(String[] args) {
        System.out.println("=== Starting Chat Server 5 on Port 8085 ===");
        ChatServer server = new ChatServer(8085);
        server.start();
    }
}