package com.decisionos.groq;

public interface LlmClient {
    String complete(String systemPrompt, String userPrompt);
}
