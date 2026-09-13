package com.decisionos.groq;

import com.decisionos.common.exceptions.LlmUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class LlmTestController {

    private final LlmClient llmClient;

    public LlmTestController(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @GetMapping("/llm-test")
    public ResponseEntity<Map<String, String>> llmTest() {
        String output = llmClient.complete(
                "You are a helpful assistant.",
                "Say hello in one sentence."
        );
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "response", output
        ));
    }

    @ExceptionHandler(LlmUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleLlmUnavailable(LlmUnavailableException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", "error",
                "message", "LLM unavailable"
        ));
    }
}
