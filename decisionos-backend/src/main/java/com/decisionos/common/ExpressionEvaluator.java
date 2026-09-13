package com.decisionos.common;

import com.decisionos.common.exceptions.BadRequestException;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Strict whitelist expression evaluator (§3.4 security constraints).
 * Allowed: numbers, whitelisted variables, + - * / ^ ( ) , and min/max/abs.
 * No function calls beyond the whitelist, no method invocations, no string literals.
 */
@Component
public class ExpressionEvaluator {

    private static final Pattern ALLOWED_CHARS = Pattern.compile("[A-Za-z0-9_+\\-*/^()., \\t]+");
    private static final Pattern STRING_LITERAL = Pattern.compile("['\"]");
    private static final Pattern SUSPICIOUS = Pattern.compile("[;{}@#$%!&|<>?=:\\\\]");

    private static final Function MIN = new Function("min", 2) {
        @Override
        public double apply(double... args) {
            return Math.min(args[0], args[1]);
        }
    };

    private static final Function MAX = new Function("max", 2) {
        @Override
        public double apply(double... args) {
            return Math.max(args[0], args[1]);
        }
    };

    private static final Function ABS = new Function("abs", 1) {
        @Override
        public double apply(double... args) {
            return Math.abs(args[0]);
        }
    };

    public void validate(String formula, Set<String> allowedVariables) {
        if (formula == null || formula.isBlank()) {
            throw new BadRequestException("Formula must not be empty");
        }
        if (STRING_LITERAL.matcher(formula).find()) {
            throw new BadRequestException("String literals are not allowed in formulas");
        }
        if (SUSPICIOUS.matcher(formula).find()) {
            throw new BadRequestException("Formula contains disallowed characters");
        }
        if (!ALLOWED_CHARS.matcher(formula).matches()) {
            throw new BadRequestException("Formula contains disallowed characters");
        }
        // Identifier check: every word must be an allowed variable or whitelisted function
        String[] tokens = formula.split("[^A-Za-z_][^A-Za-z0-9_]*|[^A-Za-z0-9_]+");
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            if (token.equals("min") || token.equals("max") || token.equals("abs")) {
                continue;
            }
            if (token.matches("[0-9]+")) {
                continue;
            }
            if (!allowedVariables.contains(token)) {
                throw new BadRequestException("Unknown variable in formula: " + token);
            }
        }
        // Parse check: any parse error is a validation failure, not a runtime error
        try {
            build(formula, allowedVariables, Map.of());
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Invalid formula: " + e.getMessage());
        }
    }

    public double evaluate(String formula, Map<String, Double> variables) {
        try {
            Expression expr = build(formula, variables.keySet(), variables);
            double result = expr.evaluate();
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                throw new BadRequestException("Formula evaluated to a non-finite value: " + formula);
            }
            return result;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to evaluate formula: " + e.getMessage());
        }
    }

    private Expression build(String formula, Set<String> allowedVariables, Map<String, Double> values) {
        ExpressionBuilder builder = new ExpressionBuilder(formula)
                .function(MIN)
                .function(MAX)
                .function(ABS);
        // Only expose whitelisted variables — never the default function set
        Map<String, Double> safe = new HashMap<>();
        for (String v : allowedVariables) {
            safe.put(v, values.getOrDefault(v, 0.0));
        }
        builder.variables(safe.keySet());
        Expression expr = builder.build();
        for (Map.Entry<String, Double> e : safe.entrySet()) {
            expr.setVariable(e.getKey(), e.getValue());
        }
        return expr;
    }
}
