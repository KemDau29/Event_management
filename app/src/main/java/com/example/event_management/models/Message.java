package com.example.event_management.models;

public class Message {
    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;

    public Message() {}

    public Message(String senderId, String receiverId, String message, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    private String eventId;
    private boolean isEventShare;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    @com.google.firebase.firestore.PropertyName("isEventShare")
    public boolean isEventShare() { return isEventShare; }
    @com.google.firebase.firestore.PropertyName("isEventShare")
    public void setEventShare(boolean eventShare) { isEventShare = eventShare; }

    private String eventTitle;
    private String eventImageUrl;
    private String eventDate;
    private int eventPrice;
    private String eventLocation;

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public String getEventImageUrl() { return eventImageUrl; }
    public void setEventImageUrl(String eventImageUrl) { this.eventImageUrl = eventImageUrl; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public int getEventPrice() { return eventPrice; }
    public void setEventPrice(int eventPrice) { this.eventPrice = eventPrice; }

    public String getEventLocation() { return eventLocation; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }
}
