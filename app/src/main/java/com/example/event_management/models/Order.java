package com.example.event_management.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    private String orderId;
    private String userId; // Thêm trường lưu ID người dùng
    private long totalPrice;
    private Date timestamp;
    private String status; // Thêm trường lưu trạng thái đơn hàng
    private List<CartItem> items;

    public Order() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    // Getter và Setter cho userId
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    // Hỗ trợ thêm hàm này nếu trong CartFragment bạn quen gọi setOrderDate
    public void setOrderDate(Date date) { this.timestamp = date; }

    // Getter và Setter cho status
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}