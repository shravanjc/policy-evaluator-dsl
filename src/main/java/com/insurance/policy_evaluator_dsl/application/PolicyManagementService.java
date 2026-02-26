package com.insurance.policy_evaluator_dsl.application;

import java.util.List;

import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.domain.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyManagementService {

    private final PolicyRepository policyRepository;

    public Policy create(Policy policy) {
        throw new UnsupportedOperationException("not implemented");
    }

    public Policy findById(Long id) {
        throw new UnsupportedOperationException("not implemented");
    }

    public List<Policy> findAll() {
        throw new UnsupportedOperationException("not implemented");
    }

    public Policy updatePremium(Long id, Double basePremium, String variablePremium, String currency) {
        throw new UnsupportedOperationException("not implemented");
    }

    public void delete(Long id) {
        throw new UnsupportedOperationException("not implemented");
    }
}
