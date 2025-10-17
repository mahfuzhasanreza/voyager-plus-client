package com.example.voyeger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Trip {
    private String id;
    private String title;
    private LocalDate date;
    private String route;
    private double budget;
    private String description;
    private TripType type;
    private String creatorUsername;
    private TripStatus status;
    private List<JoinRequest> joinRequests;
    private List<String> approvedMembers;
    private String groupChatId;

    public enum TripType {
        SOLO, GROUP
    }

    public enum TripStatus {
        DRAFT, POSTED, IN_PROGRESS, COMPLETED
    }

    public Trip(String id, String title, LocalDate date, String route, double budget,
                String description, TripType type, String creatorUsername) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.route = route;
        this.budget = budget;
        this.description = description;
        this.type = type;
        this.creatorUsername = creatorUsername;
        this.status = TripStatus.DRAFT;
        this.joinRequests = new ArrayList<>();
        this.approvedMembers = new ArrayList<>();
        this.approvedMembers.add(creatorUsername); // Creator is automatically a member
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TripType getType() { return type; }
    public void setType(TripType type) { this.type = type; }

    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }

    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }

    public List<JoinRequest> getJoinRequests() { return joinRequests; }
    public void setJoinRequests(List<JoinRequest> joinRequests) { this.joinRequests = joinRequests; }

    public List<String> getApprovedMembers() { return approvedMembers; }
    public void setApprovedMembers(List<String> approvedMembers) { this.approvedMembers = approvedMembers; }

    public String getGroupChatId() { return groupChatId; }
    public void setGroupChatId(String groupChatId) { this.groupChatId = groupChatId; }

    public boolean isGroupTrip() {
        return type == TripType.GROUP;
    }

    public void addJoinRequest(JoinRequest request) {
        this.joinRequests.add(request);
    }

    public void approveMember(String username) {
        if (!approvedMembers.contains(username)) {
            approvedMembers.add(username);
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s) | Budget: $%.2f | %s",
            title, route, date, budget, type);
    }
}

