package com.decisionos.groq;

import com.decisionos.common.exceptions.LlmUnavailableException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class GroqLlmClient implements LlmClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final double TEMPERATURE = 0.2;
    private static final int MAX_TOKENS = 500;

    private final WebClient groqWebClient;

    @Value("${groq.api-key:}")
    private String apiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    public GroqLlmClient(@Qualifier("groqWebClient") WebClient groqWebClient) {
        this.groqWebClient = groqWebClient;
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new LlmUnavailableException("GROQ_API_KEY is not configured");
        }

        ChatRequest request = new ChatRequest(
                model,
                List.of(
                        new ChatMessage("system", systemPrompt),
                        new ChatMessage("user", userPrompt)
                ),
                TEMPERATURE,
                MAX_TOKENS
        );

        try {
            ChatResponse response = groqWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(new LlmUnavailableException(
                                            "Groq API returned status " + clientResponse.statusCode() + ": " + body))))
                    .bodyToMono(ChatResponse.class)
                    .timeout(TIMEOUT)
                    .block(TIMEOUT.plusSeconds(5));

            if (response == null || response.choices() == null || response.choices().isEmpty()
                    || response.choices().get(0).message() == null
                    || response.choices().get(0).message().content() == null) {
                throw new LlmUnavailableException("Groq API returned an empty response");
            }

            return response.choices().get(0).message().content().trim();
        } catch (LlmUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmUnavailableException("Failed to call Groq API: " + e.getMessage(), e);
        }
    }

    record ChatRequest(
            String model,
            List<ChatMessage> messages,
            double temperature,
            @JsonProperty("max_tokens") int maxTokens
    ) {
    }

    record ChatMessage(String role, String content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Choice(ChatMessage message) {
    }
}
