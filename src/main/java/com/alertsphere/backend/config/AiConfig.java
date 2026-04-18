package com.alertsphere.backend.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Import the official Google SDK Client
import com.google.genai.Client;

@Configuration
public class AiConfig {

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @Bean
    public ChatModel chatModel() {
        // 1. Initialize the official Google GenAI Client
        Client client = Client.builder().apiKey(apiKey).build();

        // 2. Pass it to the Spring AI Chat Model wrapper
        return GoogleGenAiChatModel.builder()
                .genAiClient(client)
                .build();
    }
}