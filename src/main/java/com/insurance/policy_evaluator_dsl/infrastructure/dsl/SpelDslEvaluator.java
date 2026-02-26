package com.insurance.policy_evaluator_dsl.infrastructure.dsl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.service.DslEvaluator;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.MapAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;

import static java.lang.Boolean.TRUE;

/**
 * Spring expression language backed implementation of {@link DslEvaluator}.
 * DSL syntax uses bare variable names ({@code age}, {@code gender}, {@code claimFreeYears})
 * without the {@code #} prefix. This is achieved by setting the {@link Applicant}'s fields as
 * a root {@link Map} and registering a {@link MapAccessor}, so SpEL resolves property lookups
 * directly against the map keys.
 */
@Component
public class SpelDslEvaluator implements DslEvaluator {

    private static final int MAX_CACHE_SIZE = 256;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    // Parsing a SpEL expression is relatively expensive, so we cache compiled Expression objects.
    // To avoid them growing indefinitely (although upper bound is the number of policies), we
    // cached them in a bounded LRU map (cap: {@value MAX_CACHE_SIZE} entries):
    // - LinkedHashMap access moves each entry to the tail on every read, keeping the least-recently-used entry at the head.
    // - This would automatically is then bounded by the cache size by evicting the head.
    // Collections.synchronizedMap wraps every operation under a single mutex, this is required
    // because access-order LinkedHashMap mutates its structure on reads and makes it thread-safe.
    private final Map<String, Expression> expressionCache = Collections.synchronizedMap(
            new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Expression> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            });

    @Override
    public boolean evaluateEligibility(final String dsl, final Applicant applicant) {
        Boolean result = parse(dsl).getValue(buildContext(applicant), Boolean.class);
        return TRUE.equals(result);
    }

    @Override
    public BigDecimal evaluatePremium(final String dsl, final Applicant applicant) {
        Number result = parse(dsl).getValue(buildContext(applicant), Number.class);
        return result != null ? new BigDecimal(result.toString()) : BigDecimal.ZERO;
    }

    private Expression parse(final String dsl) {
        return expressionCache.computeIfAbsent(dsl, parser::parseExpression);
    }

    // SimpleEvaluationContext is used intentionally over StandardEvaluationContext to restrict what
    // the DSL can do - no arbitrary bean resolution, constructor calls, or reflective method invocations.
    private SimpleEvaluationContext buildContext(final Applicant applicant) {
        Map<String, Object> variables = Map.of(
                "age", applicant.age(),
                "gender", applicant.gender().name(),
                "claimFreeYears", applicant.claimFreeYears()
        );
        return SimpleEvaluationContext
                .forPropertyAccessors(new MapAccessor())
                .withRootObject(variables)
                .build();
    }
}
