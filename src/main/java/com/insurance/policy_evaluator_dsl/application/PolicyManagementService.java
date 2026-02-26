package com.insurance.policy_evaluator_dsl.application;

import java.util.List;
import java.util.NoSuchElementException;

import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.domain.repository.PolicyRepository;
import com.insurance.policy_evaluator_dsl.domain.service.PolicyEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyManagementService {

    public static final String NOT_FOUND_POLICY = "Policy not found";
    private final PolicyRepository policyRepository;
    private final PolicyEvaluationService policyEvaluationService;

    public Policy create(Policy policy) {
        return policyRepository.save(policy);
    }

    public Policy findById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(NOT_FOUND_POLICY));
    }

    /**
     * Should be switched to Paging call in production
     */
    public List<Policy> findAll() {
        return policyRepository.findAll();
    }

    public Policy updatePremium(final Long id, final Double basePremium, final String variablePremiumDsl, final String currency) {
        final Policy policy = findById(id);
        policy.setBasePremium(basePremium);

        //validate and set dsl
        policyEvaluationService.validateDsl(variablePremiumDsl);
        policy.setVariablePremiumDsl(variablePremiumDsl);

        policy.setCurrency(currency);
        return policyRepository.save(policy);
    }

    public void delete(Long id) {
        final boolean exists = policyRepository.existsById(id);
        if (!exists) {
            throw new NoSuchElementException(NOT_FOUND_POLICY);
        }
        policyRepository.deleteById(id);
    }
}
