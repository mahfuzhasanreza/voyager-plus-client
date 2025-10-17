package com.example.voyeger;

public class User {
    private String username;
    private String displayName;
    private String email;
    private String bio;
    private String profilePicturePath;
    private String coverPhotoPath;
    private java.time.LocalDateTime joinedDate;

    public User(String username, String displayName, String email) {
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.bio = "";
        this.profilePicturePath = "/default-avatar.png";
        this.coverPhotoPath = "/default-cover.jpg";
        this.joinedDate = java.time.LocalDateTime.now();
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePicturePath() { return profilePicturePath; }
    public void setProfilePicturePath(String profilePicturePath) { this.profilePicturePath = profilePicturePath; }

    public String getCoverPhotoPath() { return coverPhotoPath; }
    public void setCoverPhotoPath(String coverPhotoPath) { this.coverPhotoPath = coverPhotoPath; }

    public java.time.LocalDateTime getJoinedDate() { return joinedDate; }
    public void setJoinedDate(java.time.LocalDateTime joinedDate) { this.joinedDate = joinedDate; }

    @Override
    public String toString() {
        return displayName + " (@" + username + ")";
    }
}

