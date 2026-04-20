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
            // A much stricter prompt explicitly calling out fantasy and sci-fi
            String expertPrompt = "You are an expert emergency dispatch AI. " +
                    "Analyze this incident report. If it contains fantasy elements (like dragons, monsters), " +
                    "aliens, impossible physics, obvious jokes, or is biologically impossible, reply exactly 'FAKE'. " +
                    "If it is a plausible real-world incident, reply exactly 'REAL'. " +
                    "Do not explain your reasoning, just output one word.\n\n" +
                    "Incident: " + description;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", expertPrompt)
                            ))
                    )
            );

            Map response = restClient.post()
                    // THE FINAL, STABLE URI:
                    .uri("/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            String aiResult = (String) parts.get(0).get("text");

            // Print the raw AI response to your Render logs so you can see its brain working!
            System.out.println("GEMINI RAW RESPONSE: [" + aiResult + "]");

            // .trim() strips away the invisible newlines and spaces!
            return aiResult.trim().toUpperCase();

        } catch (Exception e) {
            System.err.println("Gemini Error or Safety Block: " + e.getMessage());
            // Fail safe: If the AI errors out or blocks the prompt, do NOT verify it.
            return "FAKE";
        }
    }
}