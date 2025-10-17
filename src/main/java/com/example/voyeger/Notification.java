package com.example.voyeger;

import java.time.LocalDateTime;

/**
 * Represents a notification for join requests
 */
public class Notification {
    private String id;
    private String type; // "JOIN_REQUEST"
    private String tripId;
    private String tripTitle;
    private String tripRoute;
    private String requesterUsername;
    private String message;
    private LocalDateTime createdAt;

    public Notification(String id, String type, String tripId, String tripTitle, String tripRoute,
                       String requesterUsername, String message, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.tripId = tripId;
        this.tripTitle = tripTitle;
        this.tripRoute = tripRoute;
        this.requesterUsername = requesterUsername;
        this.message = message;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTripId() {
        return tripId;
    }

    public String getTripTitle() {
        return tripTitle;
    }

    public String getTripRoute() {
        return tripRoute;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDisplayText() {
        return requesterUsername + " wants to join your trip \"" + tripTitle + "\"";
    }

    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minutes ago";

        long hours = minutes / 60;
        if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";

        long days = hours / 24;
        if (days < 7) return days + " day" + (days > 1 ? "s" : "") + " ago";

        long weeks = days / 7;
        return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
    }

    @Override
    public String toString() {
        return getDisplayText() + " (" + getTimeAgo() + ")";
    }
}

