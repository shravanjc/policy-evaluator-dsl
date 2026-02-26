package com.insurance.policy_evaluator_dsl.domain.repository;

import java.util.List;
import java.util.Optional;

import com.insurance.policy_evaluator_dsl.domain.model.Policy;

public interface PolicyRepository {

    Policy save(Policy policy);

    Optional<Policy> findById(Long id);

    List<Policy> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
