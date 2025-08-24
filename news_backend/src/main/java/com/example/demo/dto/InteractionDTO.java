package com.example.demo.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class InteractionDTO {
    private UUID articleId;
    private String type; 
    private Double latitude;   
    private Double longitude;  
}
