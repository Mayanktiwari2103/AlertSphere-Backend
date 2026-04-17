
package com.alertsphere.backend.service;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final ChatClient chatClient;

    public GeminiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String verifyIncident(String description) {
        // This fulfills the "incident verification system" requirement [cite: 120]
        String prompt = "Review this incident report: '" + description + "'. Is it a real emergency? Respond with ONLY 'REAL' or 'FAKE'.";
        return chatClient.prompt(prompt).call().content();
    }
}