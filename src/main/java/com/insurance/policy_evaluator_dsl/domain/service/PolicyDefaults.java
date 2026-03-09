package com.insurance.policy_evaluator_dsl.domain.service;

import java.math.BigDecimal;

/**
 * Provides the configured default values applied to a {@link com.insurance.policy_evaluator_dsl.domain.model.Policy}
 * when optional fields are omitted at creation time.
 *
 * <p>Implemented by infrastructure configuration so that the application layer
 * remains independent of Spring's {@code @ConfigurationProperties}.
 */
public interface PolicyDefaults {

    String eligibilityDsl();

    BigDecimal basePremium();

    String variablePremiumDsl();
}
