package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.dto.AnalyzeResponse;
import com.example.demo.dto.SummarizeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Service
public class LLMService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public LLMService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8001").build();
        this.objectMapper = new ObjectMapper();
    }

    // Generates a summary for an article using LLM API
    public String generateSummary(String title, String description, String url) throws Exception {
        var requestBody = new SummaryRequest(title, description, url);

        Mono<String> responseMono = webClient.post()
            .uri("/summarize")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class);

        String responseJson = responseMono.block();

        SummarizeResponse response = objectMapper.readValue(responseJson, SummarizeResponse.class);
        return response.getSummary();
    }

    // Sends user query to LLM analyze API and gets intent and entities
    public AnalyzeResponse analyzeQuery(String query) throws Exception {
        var requestBody = new QueryRequest(query);

        Mono<String> responseMono = webClient.post()
            .uri("/analyze")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class);

        String responseJson = responseMono.block();

        return objectMapper.readValue(responseJson, AnalyzeResponse.class);
    }

    // DTO classes for requests

    static record SummaryRequest(String title, String description, String url) {}

    static record QueryRequest(String query) {}
}
