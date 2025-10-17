package com.example.voyeger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String BASE_URL = "http://localhost:5000";
    private HttpClient httpClient;

    private DatabaseManager() {
        httpClient = HttpClient.newHttpClient();
        System.out.println("✅ Connected to backend server: " + BASE_URL);
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Hash password using SHA-256 with salt (built-in Java security)
     */
    private String hashPassword(String password) {
        try {
            // Generate salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // Hash password with salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));

            // Combine salt and hash, encode to Base64
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verify password against stored hash
     */
    private boolean verifyPassword(String password, String storedHash) {
        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);

            // Extract salt (first 16 bytes)
            byte[] salt = new byte[16];
            System.arraycopy(combined, 0, salt, 0, 16);

            // Extract stored hash (remaining bytes)
            byte[] storedPasswordHash = new byte[combined.length - 16];
            System.arraycopy(combined, 16, storedPasswordHash, 0, storedPasswordHash.length);

            // Hash the input password with the same salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] inputPasswordHash = md.digest(password.getBytes("UTF-8"));

            // Compare hashes
            return MessageDigest.isEqual(inputPasswordHash, storedPasswordHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Register a new user - posts to your /user endpoint
     */
    public boolean registerUser(String username, String email, String password, String fullName) {
        try {
            // Hash the password
            String hashedPassword = hashPassword(password);
            if (hashedPassword == null) {
                System.err.println("❌ Failed to hash password");
                return false;
            }

            // Create JSON string manually to match your API
            String jsonData = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"fullName\":\"%s\",\"displayName\":\"%s\",\"bio\":\"\",\"profilePicturePath\":\"/default-avatar.png\",\"coverPhotoPath\":\"/default-cover.jpg\",\"createdAt\":\"%s\",\"lastLogin\":\"%s\"}",
                username, email, hashedPassword, fullName, fullName,
                java.time.LocalDateTime.now().toString(),
                java.time.LocalDateTime.now().toString()
            );

            // Send POST request to /user endpoint
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/user"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Server response code: " + response.statusCode());
            System.out.println("Server response body: " + response.body());

            if (response.statusCode() == 200) {
                System.out.println("✅ User registered successfully: " + username);
                return true;
            } else {
                System.err.println("❌ Registration failed with status: " + response.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Login user - you'll need to add a GET /user?identifier=... endpoint to your backend
     */
    public User loginUser(String usernameOrEmail, String password) {
        try {
            // Get user by username or email
            String encodedIdentifier = java.net.URLEncoder.encode(usernameOrEmail, "UTF-8");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/user?identifier=" + encodedIdentifier))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("User not found: " + response.statusCode());
                return null; // User not found
            }

            // Parse JSON response manually
            String jsonResponse = response.body();

            // Extract fields from JSON
            String storedPassword = extractJsonValue(jsonResponse, "password");

            // Verify password
            if (storedPassword == null || !verifyPassword(password, storedPassword)) {
                System.out.println("Invalid password");
                return null; // Invalid password
            }

            // Create User object
            String username = extractJsonValue(jsonResponse, "username");
            String displayName = extractJsonValue(jsonResponse, "displayName");
            String email = extractJsonValue(jsonResponse, "email");

            User user = new User(username, displayName, email);

            String bio = extractJsonValue(jsonResponse, "bio");
            if (bio != null) user.setBio(bio);

            String profilePic = extractJsonValue(jsonResponse, "profilePicturePath");
            if (profilePic != null) user.setProfilePicturePath(profilePic);

            String coverPhoto = extractJsonValue(jsonResponse, "coverPhotoPath");
            if (coverPhoto != null) user.setCoverPhotoPath(coverPhoto);

            System.out.println("✅ User logged in successfully: " + user.getUsername());
            return user;

        } catch (Exception e) {
            System.err.println("❌ Error logging in: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Simple JSON value extractor (without external library)
     */
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return null;

            startIndex += searchKey.length();
            int endIndex = json.indexOf("\"", startIndex);

            if (endIndex == -1) return null;
            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Update user profile
     */
    public boolean updateUserProfile(User user) {
        try {
            String jsonData = String.format(
                "{\"username\":\"%s\",\"displayName\":\"%s\",\"email\":\"%s\",\"bio\":\"%s\",\"profilePicturePath\":\"%s\",\"coverPhotoPath\":\"%s\"}",
                user.getUsername(), user.getDisplayName(), user.getEmail(),
                user.getBio(), user.getProfilePicturePath(), user.getCoverPhotoPath()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/user/update"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println("❌ Error updating user profile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Save a trip to the database
     */
    public boolean saveTrip(Trip trip) {
        try {
            String jsonData = String.format(
                "{\"tripId\":\"%s\",\"title\":\"%s\",\"date\":\"%s\",\"route\":\"%s\",\"budget\":%f,\"description\":\"%s\",\"type\":\"%s\",\"creatorUsername\":\"%s\",\"status\":\"%s\",\"createdAt\":\"%s\",\"isGroupTrip\":%b}",
                trip.getId(),
                escapeJson(trip.getTitle()),
                trip.getDate().toString(),
                escapeJson(trip.getRoute()),
                trip.getBudget(),
                escapeJson(trip.getDescription()),
                trip.getType().toString(),
                trip.getCreatorUsername(),
                trip.getStatus().toString(),
                java.time.LocalDateTime.now().toString(),
                trip.isGroupTrip()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/trips"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Trip saved successfully: " + trip.getTitle());
                return true;
            } else {
                System.err.println("❌ Failed to save trip. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error saving trip: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Close connection
     */
    public void close() {
        System.out.println("Backend connection closed");
    }

    /**
     * Helper method to escape JSON special characters
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
