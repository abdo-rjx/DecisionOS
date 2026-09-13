package com.decisionos.groq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class LlmConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmConfig.class);

    @Value("${groq.api-base:https://api.groq.com/openai/v1}")
    private String apiBase;

    @Value("${groq.api-key:}")
    private String apiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    @PostConstruct
    void checkApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GROQ_API_KEY is not set — LLM endpoints will return 503 until it is configured. This is expected in local dev without a key.");
        } else {
            log.info("Groq LLM client configured with model '{}'.", model);
        }
    }

    @Bean
    public WebClient groqWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30));

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(apiBase)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (apiKey != null && !apiKey.isBlank()) {
            builder = builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        }

        return builder.build();
    }
}
