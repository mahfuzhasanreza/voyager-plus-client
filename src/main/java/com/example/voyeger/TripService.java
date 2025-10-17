package com.example.voyeger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TripService {
    private static TripService instance;
    private Map<String, Trip> trips;
    private Map<String, GroupChat> groupChats;
    private String currentUser; // Simulated current user

    private TripService() {
        this.trips = new HashMap<>();
        this.groupChats = new HashMap<>();
        this.currentUser = "User_Demo"; // Default user
    }

    public static TripService getInstance() {
        if (instance == null) {
            instance = new TripService();
        }
        return instance;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    // Create a new trip
    public Trip createTrip(String title, java.time.LocalDate date, String route,
                          double budget, String description, Trip.TripType type) {
        String tripId = UUID.randomUUID().toString();
        Trip trip = new Trip(tripId, title, date, route, budget, description, type, currentUser);
        trips.put(tripId, trip);
        return trip;
    }

    // Post a trip (make it visible)
    public void postTrip(String tripId) {
        Trip trip = trips.get(tripId);
        if (trip != null) {
            trip.setStatus(Trip.TripStatus.POSTED);
        }
    }

    // Get all trips
    public List<Trip> getAllTrips() {
        return new ArrayList<>(trips.values());
    }

    // Get posted trips (visible to public/friends)
    public List<Trip> getPostedTrips() {
        return trips.values().stream()
                .filter(trip -> trip.getStatus() == Trip.TripStatus.POSTED)
                .collect(Collectors.toList());
    }

    // Get user's own trips
    public List<Trip> getUserTrips(String username) {
        return trips.values().stream()
                .filter(trip -> trip.getCreatorUsername().equals(username))
                .collect(Collectors.toList());
    }

    // Get trips user is a member of
    public List<Trip> getUserMemberTrips(String username) {
        return trips.values().stream()
                .filter(trip -> trip.getApprovedMembers().contains(username))
                .collect(Collectors.toList());
    }

    // Request to join a group trip
    public JoinRequest requestToJoin(String tripId, String message) {
        Trip trip = trips.get(tripId);
        if (trip == null || !trip.isGroupTrip()) {
            return null;
        }

        String requestId = UUID.randomUUID().toString();
        JoinRequest request = new JoinRequest(requestId, tripId, currentUser, message);
        trip.addJoinRequest(request);
        return request;
    }

    // Approve a join request
    public GroupChat approveJoinRequest(String tripId, String requestId) {
        Trip trip = trips.get(tripId);
        if (trip == null) {
            return null;
        }

        JoinRequest request = trip.getJoinRequests().stream()
                .filter(r -> r.getId().equals(requestId))
                .findFirst()
                .orElse(null);

        if (request == null) {
            return null;
        }

        // Approve the request
        request.approve();
        trip.approveMember(request.getRequesterUsername());

        // Create or update group chat
        GroupChat groupChat = groupChats.get(trip.getGroupChatId());
        if (groupChat == null) {
            // Create new group chat if it doesn't exist
            String chatId = UUID.randomUUID().toString();
            groupChat = new GroupChat(chatId, tripId, trip.getTitle() + " - Group Chat", trip.getApprovedMembers());
            groupChats.put(chatId, groupChat);
            trip.setGroupChatId(chatId);
        } else {
            // Add new member to existing chat
            groupChat.addMember(request.getRequesterUsername());
        }

        return groupChat;
    }

    // Reject a join request
    public void rejectJoinRequest(String tripId, String requestId) {
        Trip trip = trips.get(tripId);
        if (trip == null) {
            return;
        }

        JoinRequest request = trip.getJoinRequests().stream()
                .filter(r -> r.getId().equals(requestId))
                .findFirst()
                .orElse(null);

        if (request != null) {
            request.reject();
        }
    }

    // Get trip by ID
    public Trip getTrip(String tripId) {
        return trips.get(tripId);
    }

    // Get group chat for a trip
    public GroupChat getGroupChat(String chatId) {
        return groupChats.get(chatId);
    }

    // Get pending join requests for a trip
    public List<JoinRequest> getPendingRequests(String tripId) {
        Trip trip = trips.get(tripId);
        if (trip == null) {
            return new ArrayList<>();
        }

        return trip.getJoinRequests().stream()
                .filter(r -> r.getStatus() == JoinRequest.RequestStatus.PENDING)
                .collect(Collectors.toList());
    }
}

