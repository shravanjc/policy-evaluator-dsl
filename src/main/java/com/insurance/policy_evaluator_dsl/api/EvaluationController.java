package com.insurance.policy_evaluator_dsl.api;

import com.insurance.policy_evaluator_dsl.api.mapper.PolicyApiMapper;
import com.insurance.policy_evaluator_dsl.application.EvaluationService;
import com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult;
import com.insurance.policy_evaluator_dsl.generated.api.EvaluationApi;
import com.insurance.policy_evaluator_dsl.generated.dto.EvaluationRequest;
import com.insurance.policy_evaluator_dsl.generated.dto.EvaluationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class EvaluationController implements EvaluationApi {

    private final EvaluationService evaluationService;
    private final PolicyApiMapper policyApiMapper;

    @Override
    public ResponseEntity<EvaluationResponse> evaluateApplicant(Long policyId, EvaluationRequest evaluationRequest) {
        final EligibilityResult result = evaluationService.evaluate(policyId, policyApiMapper.toApplicant(evaluationRequest));
        return ResponseEntity.ok(policyApiMapper.toEvaluationResponse(result, policyId));
    }
}
