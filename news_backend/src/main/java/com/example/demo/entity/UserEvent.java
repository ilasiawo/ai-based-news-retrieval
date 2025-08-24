package com.example.demo.entity;

import jakarta.persistence.*;
import com.example.demo.dto.EventType;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_events")
public class UserEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "user_latitude")
    private Double userLatitude;

    @Column(name = "user_longitude")
    private Double userLongitude;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    public UserEvent() {
        this.eventTimestamp = LocalDateTime.now();
    }

    public UserEvent(UUID articleId, EventType eventType, Double userLatitude, Double userLongitude) {
        this.articleId = articleId;
        this.eventType = eventType;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.eventTimestamp = LocalDateTime.now();
    }

    // Getters and setters below

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Double getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(Double userLatitude) {
        this.userLatitude = userLatitude;
    }

    public Double getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(Double userLongitude) {
        this.userLongitude = userLongitude;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
}
