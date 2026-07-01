package com.example.event_management.models;

import java.io.Serializable;
import java.util.Date;

public class TicketType implements Serializable {
    private String name;
    private int price;
    private String description;
    
    // Các trường mới cho Early Bird và giới hạn
    private int maxQuantity;
    private int soldQuantity;
    private Date deadline;
    private boolean isEarlyBird;

    public TicketType() {}

    public TicketType(String name, int price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(int soldQuantity) {
        this.soldQuantity = soldQuantity;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public boolean isEarlyBird() {
        return isEarlyBird;
    }

    public void setEarlyBird(boolean earlyBird) {
        isEarlyBird = earlyBird;
    }

    public boolean isAvailable() {
        // Kiểm tra số lượng
        if (maxQuantity > 0 && soldQuantity >= maxQuantity) {
            return false;
        }
        
        // Kiểm tra thời hạn
        if (deadline != null && new Date().after(deadline)) {
            return false;
        }
        
        return true;
    }
}
