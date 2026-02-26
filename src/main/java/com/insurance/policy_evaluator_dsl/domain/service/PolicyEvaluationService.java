package com.insurance.policy_evaluator_dsl.domain.service;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import lombok.RequiredArgsConstructor;

import static java.math.RoundingMode.HALF_UP;

@RequiredArgsConstructor
public class PolicyEvaluationService {

    private final DslEvaluator dslEvaluator;

    public EligibilityResult evaluate(Policy policy, Applicant applicant) {
        boolean isEligible = dslEvaluator.evaluateEligibility(policy.getEligibilityDsl(), applicant);
        if (!isEligible) {
            return EligibilityResult.ineligible("Not eligible. Valid criteria: " + policy.getEligibilityDsl());
        }
        BigDecimal variable = dslEvaluator.evaluatePremium(policy.getVariablePremiumDsl(), applicant);
        BigDecimal total = policy.getBasePremium()
                .add(variable)
                .setScale(2, HALF_UP);
        return EligibilityResult.eligible(total, policy.getCurrency());
    }

    /**
     * Will throw an exception if the given dslExpression is invalid
     */
    public void validateDsl(final String dslExpression) {
        dslEvaluator.validateAndParseDsl(dslExpression);
    }
}
