package com.example.event_management.models;

import java.io.Serializable;
import java.util.List;

public class Organization implements Serializable {
    private String id;
    private String name;
    private String description;
    private String logoUrl;
    private List<String> followers; // List of user UIDs

    public Organization() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public List<String> getFollowers() { return followers; }
    public void setFollowers(List<String> followers) { this.followers = followers; }
}
