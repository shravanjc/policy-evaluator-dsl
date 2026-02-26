package com.insurance.policy_evaluator_dsl.api;

import java.util.List;

import com.insurance.policy_evaluator_dsl.api.mapper.PolicyApiMapper;
import com.insurance.policy_evaluator_dsl.application.PolicyManagementService;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.dto.CreatePolicyRequest;
import com.insurance.policy_evaluator_dsl.dto.PolicyResponse;
import com.insurance.policy_evaluator_dsl.dto.UpdatePremiumRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PolicyController implements PoliciesApi {

    private final PolicyManagementService policyManagementService;
    private final PolicyApiMapper policyApiMapper;

    @Override
    public ResponseEntity<PolicyResponse> createPolicy(CreatePolicyRequest createPolicyRequest) {
        final Policy policy = policyApiMapper.toDomain(createPolicyRequest);
        final PolicyResponse response = policyApiMapper.toResponse(policyManagementService.create(policy));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deletePolicy(Long id) {
        policyManagementService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PolicyResponse> getPolicyById(Long id) {
        final Policy policy = policyManagementService.findById(id);
        return ResponseEntity.ok(policyApiMapper.toResponse(policy));
    }

    @Override
    public ResponseEntity<List<PolicyResponse>> listPolicies() {
        final List<PolicyResponse> response = policyManagementService.findAll().stream()
                .map(policyApiMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PolicyResponse> updatePremium(Long id, UpdatePremiumRequest updatePremiumRequest) {
        final Policy policy = policyManagementService.updatePremium(id, updatePremiumRequest.getBasePremium(), updatePremiumRequest.getVariablePremiumDsl(), updatePremiumRequest.getCurrency());
        return ResponseEntity.ok(policyApiMapper.toResponse(policy));
    }
}
