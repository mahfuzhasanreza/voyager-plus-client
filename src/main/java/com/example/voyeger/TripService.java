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
    private Map<String, User> users;
    private Map<String, TripPost> tripPosts;
    private User currentUser;
    private DatabaseManager dbManager;

    private TripService() {
        this.trips = new HashMap<>();
        this.groupChats = new HashMap<>();
        this.users = new HashMap<>();
        this.tripPosts = new HashMap<>();
        this.dbManager = DatabaseManager.getInstance();

        // Don't create default users - will be loaded from MongoDB
        this.currentUser = null;
    }

    public static TripService getInstance() {
        if (instance == null) {
            instance = new TripService();
        }
        return instance;
    }

    // User Management
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Add user to local cache if not already there
        if (user != null && !users.containsKey(user.getUsername())) {
            users.put(user.getUsername(), user);
        }
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public void updateUser(User user) {
        users.put(user.getUsername(), user);
    }

    // Create a new trip and post it
    public Trip createTrip(String title, java.time.LocalDate date, String route,
                          double budget, String description, Trip.TripType type) {
        String tripId = UUID.randomUUID().toString();
        Trip trip = new Trip(tripId, title, date, route, budget, description, type, currentUser.getUsername());
        trips.put(tripId, trip);

        // Save trip to database
        dbManager.saveTrip(trip);

        return trip;
    }

    // Post a trip (make it visible) and create a trip post for news feed
    public TripPost postTrip(String tripId, String postContent) {
        Trip trip = trips.get(tripId);
        if (trip != null) {
            trip.setStatus(Trip.TripStatus.POSTED);

            // Create a trip post for the news feed
            String postId = UUID.randomUUID().toString();
            TripPost post = new TripPost(postId, trip, currentUser, postContent);
            tripPosts.put(postId, post);

            // Trip is already saved to database in createTrip() or will be saved separately
            // No need to save tripPost to database - using trips collection only

            return post;
        }
        return null;
    }

    // Get all trip posts for news feed (sorted by most recent)
    public List<TripPost> getNewsFeed() {
        return tripPosts.values().stream()
                .sorted((p1, p2) -> p2.getPostedAt().compareTo(p1.getPostedAt()))
                .collect(Collectors.toList());
    }

    // Get solo trip posts for news feed - FETCH FROM BACKEND
    public List<TripPost> getSoloTripPosts() {
        try {
            String excludeUsername = currentUser != null ? currentUser.getUsername() : null;
            List<Trip> soloTrips = TripApiClient.fetchSoloTrips(excludeUsername);

            System.out.println("✅ Fetched " + soloTrips.size() + " solo trips from backend");

            return convertTripsToTripPosts(soloTrips);

        } catch (Exception e) {
            System.err.println("❌ Error fetching solo trips: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get group trip posts for news feed - FETCH FROM BACKEND
    public List<TripPost> getGroupTripPosts() {
        try {
            String excludeUsername = currentUser != null ? currentUser.getUsername() : null;
            List<Trip> groupTrips = TripApiClient.fetchGroupTrips(excludeUsername);

            System.out.println("✅ Fetched " + groupTrips.size() + " group trips from backend");

            return convertTripsToTripPosts(groupTrips);

        } catch (Exception e) {
            System.err.println("❌ Error fetching group trips: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Helper method to convert Trip objects to TripPost objects
    private List<TripPost> convertTripsToTripPosts(List<Trip> tripList) {
        List<TripPost> posts = new ArrayList<>();

        for (Trip trip : tripList) {
            // Get or create user for the trip creator
            User author = users.get(trip.getCreatorUsername());
            if (author == null) {
                // Create a minimal user object if not found
                author = new User(trip.getCreatorUsername(), trip.getCreatorUsername(), "");
                users.put(trip.getCreatorUsername(), author);
            }

            // Create a TripPost with default content
            String postContent = "Check out my " + trip.getType().toString().toLowerCase() + " trip to " + trip.getRoute() + "!";
            String postId = trip.getId() + "_post";
            TripPost post = new TripPost(postId, trip, author, postContent);

            posts.add(post);

            // Cache the trip in local map
            this.trips.put(trip.getId(), trip);
        }

        return posts;
    }

    // Get user's own posts
    public List<TripPost> getUserPosts(String username) {
        return tripPosts.values().stream()
                .filter(post -> post.getAuthor().getUsername().equals(username))
                .sorted((p1, p2) -> p2.getPostedAt().compareTo(p1.getPostedAt()))
                .collect(Collectors.toList());
    }

    // Like/Unlike a post
    public void likePost(String postId) {
        TripPost post = tripPosts.get(postId);
        if (post != null) {
            post.like();
            // Likes are stored in-memory only for now
        }
    }

    public void unlikePost(String postId) {
        TripPost post = tripPosts.get(postId);
        if (post != null) {
            post.unlike();
            // Unlikes are stored in-memory only for now
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

        // Send join request to backend API to save in MongoDB
        boolean apiSuccess = TripApiClient.sendJoinRequest(tripId, currentUser.getUsername(), message);

        if (!apiSuccess) {
            System.err.println("❌ Failed to save join request to database");
            // Still create local request for UI feedback, but log the error
        } else {
            System.out.println("✅ Join request saved to MongoDB database");
        }

        // Also keep local copy for immediate UI updates
        String requestId = UUID.randomUUID().toString();
        JoinRequest request = new JoinRequest(requestId, tripId, currentUser.getUsername(), message);
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

        trip.getJoinRequests().stream()
                .filter(r -> r.getId().equals(requestId))
                .findFirst()
                .ifPresent(JoinRequest::reject);
    }

    // Get trip by ID
    public Trip getTrip(String tripId) {
        return trips.get(tripId);
    }

    // Get trip post by ID
    public TripPost getTripPost(String postId) {
        return tripPosts.get(postId);
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

        // Fetch join requests from backend API
        if (currentUser != null) {
            try {
                System.out.println("🔍 Fetching join requests from backend for trip: " + tripId);
                List<JoinRequest> backendRequests = TripApiClient.fetchJoinRequestsAsList(tripId, currentUser.getUsername());

                // Update local trip with backend requests
                trip.getJoinRequests().clear();
                trip.getJoinRequests().addAll(backendRequests);

                System.out.println("✅ Fetched " + backendRequests.size() + " requests from backend");
            } catch (Exception e) {
                System.err.println("❌ Error fetching requests from backend: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return trip.getJoinRequests().stream()
                .filter(r -> r.getStatus() == JoinRequest.RequestStatus.PENDING)
                .collect(Collectors.toList());
    }

    // Get all join requests for a trip (including approved/rejected)
    public List<JoinRequest> getAllRequests(String tripId) {
        Trip trip = trips.get(tripId);
        if (trip == null) {
            return new ArrayList<>();
        }

        // Fetch join requests from backend API
        if (currentUser != null) {
            try {
                System.out.println("🔍 Fetching all join requests from backend for trip: " + tripId);
                List<JoinRequest> backendRequests = TripApiClient.fetchJoinRequestsAsList(tripId, currentUser.getUsername());

                // Update local trip with backend requests
                trip.getJoinRequests().clear();
                trip.getJoinRequests().addAll(backendRequests);

                System.out.println("✅ Fetched " + backendRequests.size() + " total requests from backend");
            } catch (Exception e) {
                System.err.println("❌ Error fetching requests from backend: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return new ArrayList<>(trip.getJoinRequests());
    }
}
