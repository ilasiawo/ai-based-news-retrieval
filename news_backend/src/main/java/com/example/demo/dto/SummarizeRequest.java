package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SummarizeRequest {
    private String title;
    private String description;
    private String url;
}
