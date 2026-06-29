package com.example.event_management.models;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CartItem implements Serializable {
    private String cartItemId;
    private String eventId;
    private String title;
    private int price;
    private Date date;
    private String location;
    private String imgUrl;
    private int quantity;
    private String ticketType;
    private String ticketDescription;
    private boolean isChosen;
    private String confirmCode;

    // Constructor rỗng bắt buộc cho Firestore
    public CartItem() {}

    public String getConfirmCode() { return confirmCode; }
    public void setConfirmCode(String confirmCode) { this.confirmCode = confirmCode; }

    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    // Xử lý ép kiểu an toàn cho price từ cả String hoặc Number trên Firestore
    public int getPrice() { return price; }
    public void setPrice(Object price) {
        if (price instanceof Long) {
            this.price = ((Long) price).intValue();
        } else if (price instanceof Integer) {
            this.price = (Integer) price;
        } else if (price instanceof String) {
            try {
                this.price = Integer.parseInt((String) price);
            } catch (NumberFormatException e) {
                this.price = 0;
            }
        }
    }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    // 🔥 FIX CRASH: Sử dụng duy nhất cặp Getter/Setter này và chỉ định rõ Property Name cho Firebase
    @PropertyName("imgUrl")
    public String getImageUrl() {
        return imgUrl;
    }

    @PropertyName("imgUrl")
    public void setImageUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    // Xử lý ép kiểu an toàn cho quantity từ cả String hoặc Number trên Firestore
    public int getQuantity() { return quantity; }
    public void setQuantity(Object quantity) {
        if (quantity instanceof Long) {
            this.quantity = ((Long) quantity).intValue();
        } else if (quantity instanceof Integer) {
            this.quantity = (Integer) quantity;
        } else if (quantity instanceof String) {
            try {
                this.quantity = Integer.parseInt((String) quantity);
            } catch (NumberFormatException e) {
                this.quantity = 1;
            }
        }
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public String getTicketDescription() {
        return ticketDescription;
    }

    public void setTicketDescription(String ticketDescription) {
        this.ticketDescription = ticketDescription;
    }

    public boolean isChosen() { return isChosen; }
    public void setChosen(boolean chosen) { isChosen = chosen; }

    public String getFormattedDate() {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }
}