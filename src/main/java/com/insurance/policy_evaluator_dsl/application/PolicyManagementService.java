package com.insurance.policy_evaluator_dsl.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.domain.repository.PolicyRepository;
import com.insurance.policy_evaluator_dsl.domain.service.PolicyDefaults;
import com.insurance.policy_evaluator_dsl.domain.service.PolicyEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyManagementService {

    static final String NOT_FOUND_POLICY = "Policy not found";
    private final PolicyRepository policyRepository;
    private final PolicyEvaluationService policyEvaluationService;
    private final PolicyDefaults policyDefaults;

    @Transactional
    public Policy create(final Policy policy) {
        // Apply configured defaults for any omitted fields
        if (policy.getEligibilityDsl() == null) {
            policy.setEligibilityDsl(policyDefaults.eligibilityDsl());
        }
        if (policy.getBasePremium() == null) {
            policy.setBasePremium(policyDefaults.basePremium());
        }
        if (policy.getVariablePremiumDsl() == null) {
            policy.setVariablePremiumDsl(policyDefaults.variablePremiumDsl());
        }

        // validate the dsl expressions before persist
        policyEvaluationService.validateDsl(policy.getEligibilityDsl());
        if (policy.getVariablePremiumDsl() != null) {
            policyEvaluationService.validateDsl(policy.getVariablePremiumDsl());
        }
        return policyRepository.save(policy);
    }

    public Policy findById(final Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(NOT_FOUND_POLICY));
    }

    /**
     * Should be switched to Paging call in production
     */
    public List<Policy> findAll() {
        return policyRepository.findAll();
    }

    @Transactional
    public Policy updatePremium(final Long id, final BigDecimal basePremium, final String variablePremiumDsl, final String currency) {
        final Policy policy = findById(id);
        policy.setBasePremium(basePremium);

        //validate and set dsl
        policyEvaluationService.validateDsl(variablePremiumDsl);
        policy.setVariablePremiumDsl(variablePremiumDsl);

        policy.setCurrency(currency);
        return policyRepository.save(policy);
    }

    @Transactional
    public void delete(final Long id) {
        policyRepository.deleteById(id);
    }
}
