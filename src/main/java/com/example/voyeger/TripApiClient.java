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
}

