package com.alertsphere.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class AiRetryConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        // This provides a manual, clean bean to satisfy the Spring AI starter
        // and stops it from looking for the missing RetryListener class.
        return new RetryTemplate();
    }
}