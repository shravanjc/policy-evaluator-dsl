package com.insurance.policy_evaluator_dsl.domain.service;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;

public class PolicyEvaluationService {

    private final DslEvaluator dslEvaluator;

    public PolicyEvaluationService(DslEvaluator dslEvaluator) {
        this.dslEvaluator = dslEvaluator;
    }

    public EligibilityResult evaluate(Policy policy, Applicant applicant) {
        throw new UnsupportedOperationException("not implemented");
    }
}
