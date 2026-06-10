package com.example.event_management.models;

public class FriendRequest {
    private String fromId;
    private String fromName;
    private String toId;
    private String status; // pending, accepted, rejected

    public FriendRequest() {}

    public FriendRequest(String fromId, String fromName, String toId, String status) {
        this.fromId = fromId;
        this.fromName = fromName;
        this.toId = toId;
        this.status = status;
    }

    public String getFromId() { return fromId; }
    public void setFromId(String fromId) { this.fromId = fromId; }

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }

    public String getToId() { return toId; }
    public void setToId(String toId) { this.toId = toId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
