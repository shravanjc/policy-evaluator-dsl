package com.insurance.policy_evaluator_dsl.api.mapper;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.dto.CreatePolicyRequest;
import com.insurance.policy_evaluator_dsl.dto.EvaluationRequest;
import com.insurance.policy_evaluator_dsl.dto.EvaluationResponse;
import com.insurance.policy_evaluator_dsl.dto.PolicyResponse;
import org.springframework.stereotype.Component;

@Component
public class PolicyApiMapper {

    public Policy toDomain(CreatePolicyRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    public PolicyResponse toResponse(Policy policy) {
        throw new UnsupportedOperationException("not implemented");
    }

    public Applicant toApplicant(EvaluationRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    public EvaluationResponse toEvaluationResponse(EligibilityResult result, Long policyId) {
        throw new UnsupportedOperationException("not implemented");
    }
}
