package com.example.voyeger;

import java.time.LocalDateTime;

public class JoinRequest {
    private String id;
    private String tripId;
    private String requesterUsername;
    private String message;
    private LocalDateTime requestTime;
    private RequestStatus status;

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }

    public JoinRequest(String id, String tripId, String requesterUsername, String message) {
        this.id = id;
        this.tripId = tripId;
        this.requesterUsername = requesterUsername;
        this.message = message;
        this.requestTime = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getRequesterUsername() { return requesterUsername; }
    public void setRequesterUsername(String requesterUsername) { this.requesterUsername = requesterUsername; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public void approve() {
        this.status = RequestStatus.APPROVED;
    }

    public void reject() {
        this.status = RequestStatus.REJECTED;
    }

    @Override
    public String toString() {
        return String.format("%s wants to join (Status: %s) - %s",
            requesterUsername, status, message);
    }
}

