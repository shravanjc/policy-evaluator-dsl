package com.insurance.policy_evaluator_dsl.infrastructure.config;

import com.insurance.policy_evaluator_dsl.domain.service.DslEvaluator;
import com.insurance.policy_evaluator_dsl.domain.service.PolicyEvaluationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    //We add it to the Spring context here to keep domain package as framework independent as possible.
    @Bean
    public PolicyEvaluationService policyEvaluationService(DslEvaluator dslEvaluator) {
        return new PolicyEvaluationService(dslEvaluator);
    }
}