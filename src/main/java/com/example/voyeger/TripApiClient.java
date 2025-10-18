package com.example.voyeger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple HTTP client to fetch trips from backend API without external dependencies
 */
public class TripApiClient {
    private static final String BASE_URL = "http://localhost:5000";

    public static List<Trip> fetchSoloTrips(String excludeUsername) {
        try {
            String url = BASE_URL + "/trips/solo";
            if (excludeUsername != null && !excludeUsername.isEmpty()) {
                url += "?excludeUsername=" + URLEncoder.encode(excludeUsername, StandardCharsets.UTF_8);
            }

            String jsonResponse = httpGet(url);
            return parseTripsFromJson(jsonResponse);
        } catch (Exception e) {
            System.err.println("❌ Error fetching solo trips: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<Trip> fetchGroupTrips(String excludeUsername) {
        try {
            String url = BASE_URL + "/trips/group";
            if (excludeUsername != null && !excludeUsername.isEmpty()) {
                url += "?excludeUsername=" + URLEncoder.encode(excludeUsername, StandardCharsets.UTF_8);
            }

            String jsonResponse = httpGet(url);
            return parseTripsFromJson(jsonResponse);
        } catch (Exception e) {
            System.err.println("❌ Error fetching group trips: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static String httpGet(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    private static List<Trip> parseTripsFromJson(String json) {
        List<Trip> trips = new ArrayList<>();

        // Simple regex-based JSON parsing (works for clean JSON arrays)
        // Pattern to match each trip object in the array
        Pattern tripPattern = Pattern.compile("\\{[^}]+\\}");
        Matcher tripMatcher = tripPattern.matcher(json);

        while (tripMatcher.find()) {
            String tripJson = tripMatcher.group();
            try {
                Trip trip = parseSingleTrip(tripJson);
                if (trip != null) {
                    trips.add(trip);
                }
            } catch (Exception e) {
                System.err.println("Error parsing trip: " + e.getMessage());
            }
        }

        return trips;
    }

    private static Trip parseSingleTrip(String json) {
        try {
            String id = extractJsonValue(json, "_id");
            if (id == null) id = extractJsonValue(json, "id");

            String title = extractJsonValue(json, "title");
            String route = extractJsonValue(json, "route");
            String dateStr = extractJsonValue(json, "date");
            String budgetStr = extractJsonValue(json, "budget");
            String description = extractJsonValue(json, "description");
            String typeStr = extractJsonValue(json, "type");
            String creatorUsername = extractJsonValue(json, "creatorUsername");
            String statusStr = extractJsonValue(json, "status");

            if (id == null || title == null || route == null) {
                return null;
            }

            // Parse date
            LocalDate date = LocalDate.now();
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    date = LocalDate.parse(dateStr);
                } catch (Exception e) {
                    // If parsing fails, use current date
                }
            }

            // Parse budget
            double budget = 0.0;
            if (budgetStr != null && !budgetStr.isEmpty()) {
                try {
                    budget = Double.parseDouble(budgetStr);
                } catch (Exception e) {
                    // Default to 0
                }
            }

            // Parse type
            Trip.TripType type = Trip.TripType.SOLO;
            if (typeStr != null) {
                try {
                    if (typeStr.equalsIgnoreCase("group") || typeStr.equalsIgnoreCase("GROUP")) {
                        type = Trip.TripType.GROUP;
                    }
                } catch (Exception e) {
                    // Default to SOLO
                }
            }

            // Create trip
            Trip trip = new Trip(
                id,
                title,
                date,
                route,
                budget,
                description != null ? description : "",
                type,
                creatorUsername != null ? creatorUsername : "Unknown"
            );

            // Parse status
            if (statusStr != null) {
                try {
                    Trip.TripStatus status = Trip.TripStatus.valueOf(statusStr.toUpperCase());
                    trip.setStatus(status);
                } catch (Exception e) {
                    trip.setStatus(Trip.TripStatus.POSTED);
                }
            } else {
                trip.setStatus(Trip.TripStatus.POSTED);
            }

            return trip;

        } catch (Exception e) {
            System.err.println("Error in parseSingleTrip: " + e.getMessage());
            return null;
        }
    }

    private static String extractJsonValue(String json, String key) {
        // Pattern to match "key":"value" or "key":value (for numbers)
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"?([^,\"\\}]+)\"?");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    /**
     * Send a join request to a group trip
     * @param tripId The MongoDB ObjectId of the trip
     * @param requesterUsername The username of the person requesting to join
     * @param message Optional message from the requester
     * @return true if request was successful, false otherwise
     */
    public static boolean sendJoinRequest(String tripId, String requesterUsername, String message) {
        try {
            String url = BASE_URL + "/trips/" + tripId + "/request";

            // Create JSON payload
            String jsonPayload = "{" +
                "\"requesterUsername\":\"" + escapeJson(requesterUsername) + "\"," +
                "\"message\":\"" + escapeJson(message != null ? message : "") + "\"" +
                "}";

            System.out.println("📤 Sending join request to: " + url);
            System.out.println("📤 Payload: " + jsonPayload);

            String response = httpPost(url, jsonPayload);
            System.out.println("✅ Join request response: " + response);

            return response != null && response.contains("insertedId");
        } catch (Exception e) {
            System.err.println("❌ Error sending join request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetch join requests for a specific trip (trip owner only)
     * @param tripId The MongoDB ObjectId of the trip
     * @param username The username of the trip creator
     * @return List of join requests (as JSON string for now)
     */
    public static String fetchJoinRequests(String tripId, String username) {
        try {
            String url = BASE_URL + "/trips/" + tripId + "/requests?username=" +
                URLEncoder.encode(username, StandardCharsets.UTF_8);
            return httpGet(url);
        } catch (Exception e) {
            System.err.println("❌ Error fetching join requests: " + e.getMessage());
            e.printStackTrace();
            return "[]";
        }
    }

    /**
     * Fetch and parse join requests for a specific trip
     * @param tripId The MongoDB ObjectId of the trip
     * @param username The username of the trip creator
     * @return List of JoinRequest objects
     */
    public static List<JoinRequest> fetchJoinRequestsAsList(String tripId, String username) {
        try {
            String jsonResponse = fetchJoinRequests(tripId, username);
            return parseJoinRequestsFromJson(jsonResponse, tripId);
        } catch (Exception e) {
            System.err.println("❌ Error parsing join requests: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Parse join requests from JSON array
     */
    private static List<JoinRequest> parseJoinRequestsFromJson(String json, String tripId) {
        List<JoinRequest> requests = new ArrayList<>();

        // Pattern to match each request object in the array
        Pattern requestPattern = Pattern.compile("\\{[^}]+\\}");
        Matcher requestMatcher = requestPattern.matcher(json);

        while (requestMatcher.find()) {
            String requestJson = requestMatcher.group();
            try {
                JoinRequest request = parseSingleJoinRequest(requestJson, tripId);
                if (request != null) {
                    requests.add(request);
                }
            } catch (Exception e) {
                System.err.println("Error parsing join request: " + e.getMessage());
            }
        }

        System.out.println("✅ Parsed " + requests.size() + " join requests from backend");
        return requests;
    }

    /**
     * Parse a single join request from JSON
     */
    private static JoinRequest parseSingleJoinRequest(String json, String tripId) {
        try {
            String requestId = extractJsonValue(json, "_id");
            String requesterUsername = extractJsonValue(json, "requesterUsername");
            String message = extractJsonValue(json, "message");
            String statusStr = extractJsonValue(json, "status");

            if (requestId == null || requesterUsername == null) {
                return null;
            }

            JoinRequest request = new JoinRequest(requestId, tripId, requesterUsername, message != null ? message : "");

            // Set status if provided
            if (statusStr != null) {
                try {
                    if (statusStr.equalsIgnoreCase("APPROVED")) {
                        request.approve();
                    } else if (statusStr.equalsIgnoreCase("REJECTED")) {
                        request.reject();
                    }
                    // Default is PENDING, no need to set
                } catch (Exception e) {
                    // Keep default PENDING status
                }
            }

            return request;

        } catch (Exception e) {
            System.err.println("Error in parseSingleJoinRequest: " + e.getMessage());
            return null;
        }
    }

    private static String httpPost(String urlString, String jsonPayload) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        // Write the payload
        conn.getOutputStream().write(jsonPayload.getBytes(StandardCharsets.UTF_8));

        int responseCode = conn.getResponseCode();
        System.out.println("📡 HTTP Response Code: " + responseCode);

        if (responseCode != 200 && responseCode != 201) {
            // Read error response
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            System.err.println("❌ Error response: " + errorResponse.toString());
            throw new Exception("HTTP " + responseCode + ": " + errorResponse.toString());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    private static String httpPut(String urlString, String jsonPayload) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        // Write the payload
        conn.getOutputStream().write(jsonPayload.getBytes(StandardCharsets.UTF_8));

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Fetch notifications for a user (join requests for their trips)
     * @param username The username of the trip creator
     * @return List of Notification objects
     */
    public static List<Notification> fetchNotifications(String username) {
        try {
            String url = BASE_URL + "/notifications/" + URLEncoder.encode(username, StandardCharsets.UTF_8);
            String jsonResponse = httpGet(url);
            return parseNotificationsFromJson(jsonResponse);
        } catch (Exception e) {
            System.err.println("❌ Error fetching notifications: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get the count of pending notifications for a user
     * @param username The username of the trip creator
     * @return Number of pending notifications
     */
    public static int fetchNotificationCount(String username) {
        try {
            String url = BASE_URL + "/notifications/" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "/count";
            System.out.println("🔍 Fetching notification count for user: " + username);
            System.out.println("📡 URL: " + url);

            String jsonResponse = httpGet(url);
            System.out.println("📥 Response: " + jsonResponse);

            // Extract count from JSON response {"count": 5}
            String countStr = extractJsonValue(jsonResponse, "count");
            if (countStr != null) {
                int count = Integer.parseInt(countStr);
                System.out.println("✅ Notification count: " + count);
                return count;
            }
            System.out.println("⚠️ No count found in response");
            return 0;
        } catch (Exception e) {
            System.err.println("❌ Error fetching notification count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Parse notifications from JSON array
     */
    private static List<Notification> parseNotificationsFromJson(String json) {
        List<Notification> notifications = new ArrayList<>();

        // Pattern to match each notification object in the array
        Pattern notifPattern = Pattern.compile("\\{[^}]+\\}");
        Matcher notifMatcher = notifPattern.matcher(json);

        while (notifMatcher.find()) {
            String notifJson = notifMatcher.group();
            try {
                Notification notification = parseSingleNotification(notifJson);
                if (notification != null) {
                    notifications.add(notification);
                }
            } catch (Exception e) {
                System.err.println("Error parsing notification: " + e.getMessage());
            }
        }

        System.out.println("✅ Parsed " + notifications.size() + " notifications from backend");
        return notifications;
    }

    /**
     * Parse a single notification from JSON
     */
    private static Notification parseSingleNotification(String json) {
        try {
            String id = extractJsonValue(json, "_id");
            String type = extractJsonValue(json, "type");
            String tripId = extractJsonValue(json, "tripId");
            String tripTitle = extractJsonValue(json, "tripTitle");
            String tripRoute = extractJsonValue(json, "tripRoute");
            String requesterUsername = extractJsonValue(json, "requesterUsername");
            String tripCreatorUsername = extractJsonValue(json, "tripCreatorUsername");
            String message = extractJsonValue(json, "message");
            String createdAtStr = extractJsonValue(json, "createdAt");

            if (id == null || tripTitle == null) {
                return null;
            }

            // Parse timestamp - for now use current time if parsing fails
            java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
            if (createdAtStr != null) {
                try {
                    // Parse ISO date format
                    createdAt = java.time.LocalDateTime.parse(createdAtStr,
                        java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception e) {
                    // Use current time if parsing fails
                }
            }

            // Use appropriate username based on notification type
            String displayUsername = requesterUsername != null ? requesterUsername : tripCreatorUsername;

            return new Notification(
                id,
                type != null ? type : "JOIN_REQUEST",
                tripId != null ? tripId : "",
                tripTitle,
                tripRoute != null ? tripRoute : "",
                displayUsername != null ? displayUsername : "Unknown",
                message != null ? message : "",
                createdAt
            );

        } catch (Exception e) {
            System.err.println("Error in parseSingleNotification: " + e.getMessage());
            return null;
        }
    }

    /**
     * Dismiss a notification (mark as read/delete)
     * @param username The username of the user dismissing the notification
     * @param notificationId The ID of the notification to dismiss
     * @return true if successful, false otherwise
     */
    public static boolean dismissNotification(String username, String notificationId) {
        try {
            String url = BASE_URL + "/notifications/" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "/" + notificationId;
            System.out.println("🗑️ Dismissing notification: " + notificationId);

            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            System.out.println("📡 HTTP Response Code: " + responseCode);

            if (responseCode == 200) {
                System.out.println("✅ Notification dismissed successfully");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error dismissing notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Approve a join request and automatically create/update group chat
     * @param tripId The MongoDB ObjectId of the trip
     * @param requestId The ID of the join request to approve
     * @param username The username of the trip creator (for authorization)
     * @return true if request was approved successfully, false otherwise
     */
    public static boolean approveJoinRequest(String tripId, String requestId, String username) {
        try {
            String url = BASE_URL + "/trips/" + tripId + "/requests/" + requestId + "/respond";

            // Create JSON payload with action "approve"
            String jsonPayload = "{" +
                "\"action\":\"approve\"," +
                "\"responderUsername\":\"" + escapeJson(username) + "\"" +
                "}";

            System.out.println("📤 Approving join request: " + url);
            System.out.println("📤 Payload: " + jsonPayload);

            String response = httpPut(url, jsonPayload);
            System.out.println("✅ Approve request response: " + response);

            return response != null && (response.contains("approved successfully") || response.contains("success"));
        } catch (Exception e) {
            System.err.println("❌ Error approving join request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create or update a group chat for a trip (In-Memory only)
     * @param tripId The MongoDB ObjectId of the trip
     * @param chatName The name of the group chat
     * @param members List of usernames who are members of the chat
     * @return The group chat ID if successful, null otherwise
     */
    public static String createOrUpdateGroupChat(String tripId, String chatName, List<String> members) {
        try {
            // Group chat is automatically created by backend when request is approved
            // This method just returns the tripId as chatId since backend uses tripId as chatId
            System.out.println("✅ Group chat automatically created by backend for trip: " + tripId);
            return tripId;
        } catch (Exception e) {
            System.err.println("❌ Error in createOrUpdateGroupChat: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetch group chat for a specific trip
     * @param tripId The MongoDB ObjectId of the trip
     * @return GroupChat object if found, null otherwise
     */
    public static GroupChat fetchGroupChat(String tripId) {
        try {
            String url = BASE_URL + "/groupchats/" + tripId;
            String jsonResponse = httpGet(url);
            return parseGroupChatFromJson(jsonResponse, tripId);
        } catch (Exception e) {
            System.err.println("❌ Error fetching group chat: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse a group chat from JSON
     */
    private static GroupChat parseGroupChatFromJson(String json, String tripId) {
        try {
            String chatId = extractJsonValue(json, "_id");
            if (chatId == null) chatId = extractJsonValue(json, "id");

            String chatName = extractJsonValue(json, "chatName");

            if (chatId == null || chatName == null) {
                return null;
            }

            // Parse members array
            List<String> members = new ArrayList<>();
            Pattern membersPattern = Pattern.compile("\"members\"\\s*:\\s*\\[([^\\]]+)\\]");
            Matcher membersMatcher = membersPattern.matcher(json);

            if (membersMatcher.find()) {
                String membersStr = membersMatcher.group(1);
                Pattern memberPattern = Pattern.compile("\"([^\"]+)\"");
                Matcher memberMatcher = memberPattern.matcher(membersStr);

                while (memberMatcher.find()) {
                    members.add(memberMatcher.group(1));
                }
            }

            GroupChat groupChat = new GroupChat(chatId, tripId, chatName, members);

            System.out.println("✅ Parsed group chat: " + chatName + " with " + members.size() + " members");
            return groupChat;

        } catch (Exception e) {
            System.err.println("Error in parseGroupChatFromJson: " + e.getMessage());
            return null;
        }
    }

    /**
     * Send a message to a group chat
     * @param tripId The trip ID (used as chat ID)
     * @param sender The username of the sender
     * @param content The message content
     * @return true if successful, false otherwise
     */
    public static boolean sendChatMessage(String tripId, String sender, String content) {
        try {
            String url = BASE_URL + "/groupchats/" + tripId + "/messages";

            String jsonPayload = "{" +
                "\"sender\":\"" + escapeJson(sender) + "\"," +
                "\"content\":\"" + escapeJson(content) + "\"" +
                "}";

            System.out.println("📤 Sending chat message to: " + url);

            String response = httpPost(url, jsonPayload);
            System.out.println("✅ Message sent successfully");

            return response != null && response.contains("success");
        } catch (Exception e) {
            System.err.println("❌ Error sending chat message: " + e.getMessage());
            return false;
        }
    }
}
