package com.alertsphere.backend.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    // Spring AI 2.0.0-M4 uses ChatModel as the standard interface
    private final ChatModel chatModel;

    @Autowired
    public GeminiService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String verifyIncident(String description) {
        try {
            // The logic: prompt the model and get the result
            return chatModel.call(description);
        } catch (Exception e) {
            System.err.println("Gemini Error: " + e.getMessage());
            // Fallback so the backend never crashes
            return "REAL";
        }
    }
}