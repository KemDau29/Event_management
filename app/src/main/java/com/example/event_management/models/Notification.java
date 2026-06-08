package com.example.event_management.models;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type; // e.g., "PURCHASE_SUCCESS", "EVENT_REMINDER"
    private Date timestamp;
    private boolean isRead;
    
    // Additional data for details
    private String orderId;
    private String eventId;
    private String ticketInfo; // Combined ticket codes or summary

    public Notification() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTicketInfo() { return ticketInfo; }
    public void setTicketInfo(String ticketInfo) { this.ticketInfo = ticketInfo; }
}
