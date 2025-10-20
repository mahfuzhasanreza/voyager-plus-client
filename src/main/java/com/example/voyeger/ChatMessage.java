package com.example.voyeger;

import java.time.LocalDateTime;

public class ChatMessage {
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private String messageType; // TEXT, FILE, etc.

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
        this.messageType = "TEXT";
    }

    public ChatMessage(String sender, String content) {
        this();
        this.sender = sender;
        this.content = content;
    }

    public ChatMessage(String sender, String content, String messageType) {
        this(sender, content);
        this.messageType = messageType;
    }

    // Getters and Setters
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
}