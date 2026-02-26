package com.insurance.policy_evaluator_dsl.domain.model;

public record Applicant(int age, Gender gender, int claimFreeYears) {

    public static Applicant of(int age, Gender gender, int claimFreeYears) {
        return new Applicant(age, gender, claimFreeYears);
    }
}
