package com.example.demo.dto;

public enum EventType {
    VIEW(1),
    CLICK(2),
    LIKE(4),
    SHARE(6);

    private final int score;

    EventType(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
