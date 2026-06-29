package com.example.event_management.models;

import java.io.Serializable;
import java.util.Date;

public class Ticket implements Serializable {
    private String ticketId;
    private String orderId;
    private String eventId;
    private String userId; // Chủ sở hữu hiện tại
    private String purchaserId; // Người mua ban đầu
    private String title;
    private int price;
    private Date eventDate;
    private Date purchaseDate;
    private String location;
    private String imgUrl;
    private int quantity;
    private String ticketType;
    private String status; // "Đã mua", "Đã bán", "Đã hủy"
    private String confirmCode;
    private String recipientUsername; // Lưu username người nhận nếu đã chuyển

    public Ticket() {}

    // Getters and Setters
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPurchaserId() { return purchaserId; }
    public void setPurchaserId(String purchaserId) { this.purchaserId = purchaserId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }

    public Date getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Date purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getConfirmCode() { return confirmCode; }
    public void setConfirmCode(String confirmCode) { this.confirmCode = confirmCode; }

    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }
}
