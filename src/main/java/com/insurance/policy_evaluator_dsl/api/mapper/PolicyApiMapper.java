package com.insurance.policy_evaluator_dsl.api.mapper;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.dto.CreatePolicyRequest;
import com.insurance.policy_evaluator_dsl.dto.EvaluationRequest;
import com.insurance.policy_evaluator_dsl.dto.EvaluationResponse;
import com.insurance.policy_evaluator_dsl.dto.PolicyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface PolicyApiMapper {

    @Mapping(target = "id", ignore = true)
    Policy toDomain(CreatePolicyRequest request);

    PolicyResponse toResponse(Policy policy);

    Applicant toApplicant(EvaluationRequest request);

    @Mapping(target = ".", source = "result")
    EvaluationResponse toEvaluationResponse(EligibilityResult result, Long policyId);
}
