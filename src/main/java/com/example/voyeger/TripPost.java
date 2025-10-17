package com.example.voyeger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TripPost {
    private String id;
    private Trip trip;
    private User author;
    private String content;
    private LocalDateTime postedAt;
    private int likes;
    private int comments;
    private int shares;

    public TripPost(String id, Trip trip, User author, String content) {
        this.id = id;
        this.trip = trip;
        this.author = author;
        this.content = content;
        this.postedAt = LocalDateTime.now();
        this.likes = 0;
        this.comments = 0;
        this.shares = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }

    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }

    public void like() { this.likes++; }
    public void unlike() { if (this.likes > 0) this.likes--; }
    public void addComment() { this.comments++; }
    public void share() { this.shares++; }

    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(postedAt, now).toMinutes();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minutes ago";

        long hours = minutes / 60;
        if (hours < 24) return hours + " hours ago";

        long days = hours / 24;
        if (days < 7) return days + " days ago";

        return postedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    @Override
    public String toString() {
        return author.getDisplayName() + " posted: " + trip.getTitle();
    }
}

