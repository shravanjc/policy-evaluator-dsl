package com.insurance.policy_evaluator_dsl.domain.model;

import java.math.BigDecimal;

public record EligibilityResult(
        boolean eligible,
        BigDecimal premium,
        String currency,
        String reason
) {
    public static EligibilityResult eligible(final BigDecimal premium, final String currency) {
        return new EligibilityResult(true, premium, currency, null);
    }

    public static EligibilityResult ineligible(final String reason) {
        return new EligibilityResult(false, null, null, reason);
    }
}
