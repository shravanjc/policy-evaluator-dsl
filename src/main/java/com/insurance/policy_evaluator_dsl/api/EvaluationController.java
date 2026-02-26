package com.insurance.policy_evaluator_dsl.api;

import com.insurance.policy_evaluator_dsl.dto.EvaluationRequest;
import com.insurance.policy_evaluator_dsl.dto.EvaluationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EvaluationController implements EvaluationApi {

    @Override
    public ResponseEntity<EvaluationResponse> evaluateApplicant(Long id, EvaluationRequest evaluationRequest) {
        throw new UnsupportedOperationException("not implemented");
    }
}
