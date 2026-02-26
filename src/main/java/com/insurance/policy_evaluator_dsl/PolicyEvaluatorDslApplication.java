package com.insurance.policy_evaluator_dsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PolicyEvaluatorDslApplication {
    static void main(String[] args) {
        SpringApplication.run(PolicyEvaluatorDslApplication.class, args);
    }
}
