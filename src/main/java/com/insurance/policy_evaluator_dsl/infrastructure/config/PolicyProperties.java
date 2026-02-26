package com.insurance.policy_evaluator_dsl.infrastructure.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "policy.default")
public record PolicyProperties(
        String name,
        String description,
        String eligibilityDsl,
        BigDecimal basePremium,
        String variablePremiumDsl,
        String currency
) {
}
