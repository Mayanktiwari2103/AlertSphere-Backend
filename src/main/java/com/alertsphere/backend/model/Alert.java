package com.alertsphere.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity

public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String alertType;
    private String description;
    private double latitude;
    private double longitude;
    private String locationName;
    private String severity;

    private LocalDateTime timestamp;
    private boolean verified=false;
    
    private Integer upvotes=0;
    private Integer downvotes=0;

    private String imageUrl;
    // Constructors
    public Alert(){}
    public Alert(String alertType, String description, double latitude, double longitude,String locationName,Integer upvotes, Integer downvotes, String imageUrl,String severity, String title) {
        this.alertType = alertType;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = LocalDateTime.now();
        this.locationName=locationName;// set timestamp automatically
        this.upvotes=upvotes;
        this.downvotes=downvotes;
        this.imageUrl=imageUrl;
        this.severity=severity;
        this.title=title;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    public String getLocationName() {return locationName;}
    public void setLocationName(String locationName) {this.locationName=locationName;}

    public Integer getUpvotes() { return upvotes; }
    public void setUpvotes(Integer upvotes) { this.upvotes = upvotes; }

    public Integer getDownvotes() { return downvotes; }
    public void setDownvotes(Integer downvotes) { this.downvotes = downvotes; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title=title;
    }

}
