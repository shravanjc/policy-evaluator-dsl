package com.insurance.policy_evaluator_dsl.infrastructure.config;

import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.domain.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultPolicySeeder implements ApplicationRunner {

    private final PolicyProperties policyProperties;
    private final PolicyRepository policyRepository;

    @Override
    public void run(@NonNull final ApplicationArguments args) {
        if (policyRepository.count() > 0) {
            return;
        }
        policyRepository.save(Policy.builder()
                .name(policyProperties.name())
                .description(policyProperties.description())
                .eligibilityDsl(policyProperties.eligibilityDsl())
                .basePremium(policyProperties.basePremium())
                .variablePremiumDsl(policyProperties.variablePremiumDsl())
                .currency(policyProperties.currency())
                .build());
    }
}
