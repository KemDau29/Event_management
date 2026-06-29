package com.example.event_management.models;

import java.io.Serializable;
import java.util.Date;

public class TimelineItem implements Serializable {
    private Date startTime;
    private Date endTime;
    private String activity;

    public TimelineItem() {}

    public TimelineItem(Date startTime, Date endTime, String activity) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.activity = activity;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }
}
