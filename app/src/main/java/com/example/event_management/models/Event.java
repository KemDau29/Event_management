package com.example.event_management.models;

import com.google.firebase.firestore.DocumentReference;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class Event implements Serializable {
    private String id;
    private String title;
    private String location;
    private int price;
    private Date date;
    private String description;
    private double latitude;
    private double longitude;
    
    // Các field mới
    private transient DocumentReference cate;


    @PropertyName("cate")
    public DocumentReference getCate() { return cate; }

    @PropertyName("cate")
    public void setCate(DocumentReference cate) { this.cate = cate; }
    private int attendants;
    private String imageUrl; // Thay thế cho imageUrl cũ nếu cần, hoặc dùng song song
    private int remainingTickets;
    private java.util.List<TicketType> ticketTypes;
    private boolean isLimited;
    private java.util.Date startTime;
    private java.util.Date endTime;
    private java.util.Date ticketOpenDate;
    private java.util.Date ticketCloseDate;
    private java.util.Date announcementDate;
    private java.util.List<TimelineItem> timeline;

    public Event() {}

    public java.util.Date getTicketOpenDate() { return ticketOpenDate; }
    public void setTicketOpenDate(java.util.Date ticketOpenDate) { this.ticketOpenDate = ticketOpenDate; }

    public java.util.Date getTicketCloseDate() { return ticketCloseDate; }
    public void setTicketCloseDate(java.util.Date ticketCloseDate) { this.ticketCloseDate = ticketCloseDate; }

    public java.util.Date getAnnouncementDate() { return announcementDate; }
    public void setAnnouncementDate(java.util.Date announcementDate) { this.announcementDate = announcementDate; }

    public java.util.Date getStartTime() { return startTime; }
    public void setStartTime(java.util.Date startTime) { this.startTime = startTime; }

    public java.util.Date getEndTime() { return endTime; }
    public void setEndTime(java.util.Date endTime) { this.endTime = endTime; }

    public java.util.List<TimelineItem> getTimeline() { return timeline; }
    public void setTimeline(java.util.List<TimelineItem> timeline) { this.timeline = timeline; }

    public java.util.List<TicketType> getTicketTypes() {
        return ticketTypes;
    }

    public void setTicketTypes(java.util.List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public int getAttendants() { return attendants; }
    public void setAttendants(int attendants) { this.attendants = attendants; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String ImageUrl) { this.imageUrl = ImageUrl; }

    public int getRemainingTickets() { return remainingTickets; }
    public void setRemainingTickets(int remainingTickets) { this.remainingTickets = remainingTickets; }

    public boolean isLimited() { return isLimited; }
    public void setLimited(boolean limited) { isLimited = limited; }

    public String getFormattedDate() {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}
