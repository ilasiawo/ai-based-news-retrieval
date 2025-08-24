package com.example.demo.dto;

import java.util.UUID;

public class UserEventRequest {
    
    private UUID articleId;
    private EventType eventType;
    private Double userLatitude;
    private Double userLongitude;
    
    public UserEventRequest() {}
    
    public UserEventRequest(UUID articleId, EventType eventType, Double userLatitude, Double userLongitude) {
        this.articleId = articleId;
        this.eventType = eventType;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
    }
    
    // Getters and setters
    public UUID getArticleId() { return articleId; }
    public void setArticleId(UUID articleId) { this.articleId = articleId; }
    
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    
    public Double getUserLatitude() { return userLatitude; }
    public void setUserLatitude(Double userLatitude) { this.userLatitude = userLatitude; }
    
    public Double getUserLongitude() { return userLongitude; }
    public void setUserLongitude(Double userLongitude) { this.userLongitude = userLongitude; }
}
