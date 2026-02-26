package com.insurance.policy_evaluator_dsl.infrastructure.persistence;

import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.domain.repository.PolicyRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyJpaRepository extends JpaRepository<Policy, Long>, PolicyRepository {
}
