package com.insurance.policy_evaluator_dsl.domain.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.PropertyOrFieldReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.MapAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;

import static java.lang.Boolean.TRUE;
import static java.math.BigDecimal.ZERO;

/**
 * Spring expression language backed implementation of {@link DslEvaluator}.
 * DSL syntax uses bare variable names ({@code age}, {@code gender}, {@code claimFreeYears})
 * without the {@code #} prefix. This is achieved by setting the {@link Applicant}'s fields as
 * a root {@link Map} and registering a {@link MapAccessor}, so SpEL resolves property lookups
 * directly against the map keys.
 */
@Component
public class SpelDslEvaluator implements DslEvaluator {

    private static final Set<String> KNOWN_VARIABLES = Set.of("age", "gender", "claimFreeYears");

    private final SpelExpressionParser parser = new SpelExpressionParser();

    // Built once: holds only the MapAccessor configuration, no per-request state.
    // The root object (applicant variables) is passed per-call via getValue(context, root, type).
    private final SimpleEvaluationContext evaluationContext = SimpleEvaluationContext
            .forPropertyAccessors(new MapAccessor())
            .build();

    // For test purposes, just keeping it simple. Should ideally be switched to Caffeine cache
    // to maintain time or size based eviction
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    @Override
    public boolean evaluateEligibility(final String dsl, final Applicant applicant) {
        if (dsl == null || applicant == null) {
            return false;
        }
        Boolean result = parseAndCache(dsl).getValue(evaluationContext, buildVariables(applicant), Boolean.class);
        return TRUE.equals(result);
    }

    @Override
    public BigDecimal evaluatePremium(final String dsl, final Applicant applicant) {
        if (dsl == null || applicant == null) {
            return ZERO;
        }
        BigDecimal result = parseAndCache(dsl).getValue(evaluationContext, buildVariables(applicant), BigDecimal.class);
        return result != null ? result : ZERO;
    }

    @Override
    public void validateAndParseDsl(final String dsl) {
        if (dsl == null || dsl.isBlank()) {
            throw new IllegalArgumentException("DSL expression must not be null or empty");
        }
        parseAndCache(dsl);
    }

    private Expression parseAndCache(final String dsl) {
        return expressionCache.computeIfAbsent(dsl, key -> {
            Expression expression = parser.parseExpression(key);
            validateVariables(expression);
            return expression;
        });
    }

    private void validateVariables(final Expression expression) {
        Set<String> unknown = collectPropertyNames(((SpelExpression) expression).getAST())
                .stream()
                .filter(name -> !KNOWN_VARIABLES.contains(name))
                .collect(Collectors.toSet());
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException(
                    "DSL references unknown variable(s): %s. Known variables: %s"
                            .formatted(unknown, KNOWN_VARIABLES));
        }
    }

    private Set<String> collectPropertyNames(final SpelNode node) {
        Set<String> names = new HashSet<>();
        if (node instanceof PropertyOrFieldReference ref) {
            names.add(ref.getName());
        }
        for (int index = 0; index < node.getChildCount(); index++) {
            names.addAll(collectPropertyNames(node.getChild(index)));
        }
        return names;
    }

    private Map<String, Object> buildVariables(final Applicant applicant) {
        return Map.of(
                "age", applicant.age(),
                "gender", applicant.gender().name(),
                "claimFreeYears", applicant.claimFreeYears()
        );
    }
}
