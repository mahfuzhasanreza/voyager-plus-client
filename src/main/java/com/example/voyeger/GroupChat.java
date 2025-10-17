package com.example.voyeger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupChat {
    private String id;
    private String tripId;
    private String chatName;
    private List<String> members;
    private List<ChatMessage> messages;
    private LocalDateTime createdAt;

    public GroupChat(String id, String tripId, String chatName, List<String> members) {
        this.id = id;
        this.tripId = tripId;
        this.chatName = chatName;
        this.members = new ArrayList<>(members);
        this.messages = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getChatName() { return chatName; }
    public void setChatName(String chatName) { this.chatName = chatName; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void addMember(String username) {
        if (!members.contains(username)) {
            members.add(username);
        }
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
    }

    public static class ChatMessage {
        private String sender;
        private String content;
        private LocalDateTime timestamp;

        public ChatMessage(String sender, String content) {
            this.sender = sender;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }

        public String getSender() { return sender; }
        public String getContent() { return content; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s", timestamp.toLocalTime(), sender, content);
        }
    }
}

