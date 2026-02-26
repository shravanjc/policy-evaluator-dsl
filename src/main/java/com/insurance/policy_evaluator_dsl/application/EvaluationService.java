package com.insurance.policy_evaluator_dsl.application;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.domain.service.PolicyEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final PolicyManagementService policyManagementService;
    private final PolicyEvaluationService policyEvaluationService;

    public EligibilityResult evaluate(Long policyId, Applicant applicant) {
        Policy policy = policyManagementService.findById(policyId);
        return policyEvaluationService.evaluate(policy, applicant);
    }
}
