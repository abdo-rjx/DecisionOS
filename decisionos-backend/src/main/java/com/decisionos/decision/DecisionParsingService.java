package com.decisionos.decision;

import com.decisionos.common.exceptions.LlmUnavailableException;
import com.decisionos.groq.LlmClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DecisionParsingService {

    private static final Logger log = LoggerFactory.getLogger(DecisionParsingService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LlmClient llmClient;

    public Map<String, Object> parse(String title, String description, DecisionType type) {
        String text = ((title == null ? "" : title) + " " + (description == null ? "" : description)).trim();
        try {
            String raw = llmClient.complete(
                    "You parse business decisions into JSON. Do NOT invent numbers. Only use the numbers given to you. "
                            + "Reply with ONLY a flat JSON object of numbers/strings, no prose. "
                            + "Known keys: hires, fired, avgSalary, spend, amount, percent, priceDeltaPercent, newCapacity.",
                    "Decision type: " + type + ". Text: \"" + text + "\". JSON:");
            Map<String, Object> parsed = tryParseJson(raw);
            if (!parsed.isEmpty()) {
                return parsed;
            }
        } catch (LlmUnavailableException e) {
            log.debug("LLM unavailable for decision parsing, using heuristics");
        } catch (Exception e) {
            log.debug("LLM decision parsing failed, using heuristics: {}", e.getMessage());
        }
        return heuristic(text, type);
    }

    private Map<String, Object> heuristic(String text, DecisionType type) {
        Map<String, Object> p = new HashMap<>();
        double firstNumber = firstNumber(text);
        double percent = firstPercent(text);
        switch (type) {
            case HIRING -> {
                p.put("hires", (int) Math.max(1, firstNumber <= 0 ? 1 : firstNumber));
                p.put("avgSalary", 8000.0);
            }
            case COST_CUTTING -> p.put("amount", firstNumber > 0 ? firstNumber : 10000.0);
            case MARKETING_SPEND -> p.put("spend", firstNumber > 0 ? firstNumber : 20000.0);
            case PRICE_CHANGE -> p.put("priceDeltaPercent", percent != 0 ? percent : 10.0);
            case INFRA_EXPANSION, NEW_BRANCH, TECH_INVESTMENT, NEW_PRODUCT, NEW_MARKET ->
                    p.put("amount", firstNumber > 0 ? firstNumber : 50000.0);
            default -> {
                if (firstNumber > 0) {
                    p.put("amount", firstNumber);
                }
            }
        }
        return p;
    }

    private double firstNumber(String text) {
        Matcher m = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)").matcher(text);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private double firstPercent(String text) {
        Matcher m = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*%").matcher(text);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private Map<String, Object> tryParseJson(String raw) {
        if (raw == null) {
            return Map.of();
        }
        String json = raw.trim();
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(json.substring(start, end + 1), new TypeReference<>() {
            });
        } catch (Exception e) {
            return Map.of();
        }
    }
}
