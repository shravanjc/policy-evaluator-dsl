package com.insurance.policy_evaluator_dsl.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PolicyEvaluationService {

    private final DslEvaluator dslEvaluator;

    public EligibilityResult evaluate(Policy policy, Applicant applicant) {
        boolean isEligible = dslEvaluator.evaluateEligibility(policy.getEligibilityDsl(), applicant);
        if (!isEligible) {
            return EligibilityResult.ineligible("Not eligible");
        }
        BigDecimal variable = dslEvaluator.evaluatePremium(policy.getVariablePremium(), applicant);
        BigDecimal total = BigDecimal.valueOf(policy.getBasePremium())
                .add(variable)
                .setScale(2, RoundingMode.HALF_UP);
        return EligibilityResult.eligible(total, policy.getCurrency());
    }
}
