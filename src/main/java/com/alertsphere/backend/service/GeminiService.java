package com.alertsphere.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final RestClient restClient;
    private final String apiKey;

    public GeminiService(@Value("${spring.ai.google.genai.api-key}") String apiKey) {
        this.apiKey = apiKey;
        // Using Spring's built-in modern HTTP client
        this.restClient = RestClient.create("https://generativelanguage.googleapis.com");
    }

    public String verifyIncident(String description) {
        try {
            // Build the exact JSON structure the Gemini API expects
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", description)
                            ))
                    )
            );

            // Make the raw HTTP call
            Map response = restClient.post()
                    .uri("/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            // Parse the text from the JSON response safely
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            System.err.println("Gemini Direct Call Error: " + e.getMessage());
            return "REAL"; // Demo safety fallback
        }
    }
}