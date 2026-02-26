package com.insurance.policy_evaluator_dsl.infrastructure.dsl;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.service.DslEvaluator;
import org.springframework.stereotype.Component;

@Component
public class SpelDslEvaluator implements DslEvaluator {

    @Override
    public boolean evaluateEligibility(String dsl, Applicant applicant) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public BigDecimal evaluatePremium(String dsl, Applicant applicant) {
        throw new UnsupportedOperationException("not implemented");
    }
}
