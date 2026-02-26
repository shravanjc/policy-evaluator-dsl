package com.insurance.policy_evaluator_dsl.infrastructure.config;

import com.insurance.policy_evaluator_dsl.domain.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultPolicySeeder implements ApplicationRunner {

    private final PolicyProperties policyProperties;
    private final PolicyRepository policyRepository;

    @Override
    public void run(ApplicationArguments args) {
        throw new UnsupportedOperationException("not implemented");
    }
}
