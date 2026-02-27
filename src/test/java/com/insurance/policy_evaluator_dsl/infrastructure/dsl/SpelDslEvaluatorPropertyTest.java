package com.insurance.policy_evaluator_dsl.infrastructure.dsl;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.Gender;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Property-based tests for {@link SpelDslEvaluator} covering invariants:
 * 1. Sandbox - Arbitrary strings as DSL never produce unexpected exception types.
 * 2. Idempotency – the same DSL + applicant always produces the same result (caching correctness).
 * 3. Null safety - null DSL or null applicant returns the declared default, never an exception.
 * 4. Known-variable contract – DSLs that only reference known variables are never rejected.
 *    Unknown-variable contract - Failure with type-mismatch at evaluation time.
 */
class SpelDslEvaluatorPropertyTest {

    private final SpelDslEvaluator evaluator = new SpelDslEvaluator();

    // Invariant 1: Sandbox: arbitrary strings only surface typed exceptions
    @Property
    void arbitraryDsl_eligibility_onlyThrowsTypedExceptions(
            @ForAll String dsl,
            @ForAll("applicants") Applicant applicant) {
        assertThatCode(() -> evaluator.evaluateEligibility(dsl, applicant))
                .satisfiesAnyOf(
                        code -> assertThat(code).doesNotThrowAnyException(),
                        code -> assertThat(code).isInstanceOfAny(
                                IllegalArgumentException.class,
                                SpelParseException.class,
                                SpelEvaluationException.class));
    }

    @Property
    void arbitraryDsl_premium_onlyThrowsTypedExceptions(
            @ForAll String dsl,
            @ForAll("applicants") Applicant applicant) {
        assertThatCode(() -> evaluator.evaluatePremium(dsl, applicant))
                .satisfiesAnyOf(
                        code -> assertThat(code).doesNotThrowAnyException(),
                        code -> assertThat(code).isInstanceOfAny(
                                IllegalArgumentException.class,
                                SpelParseException.class,
                                SpelEvaluationException.class));
    }

    // Invariant 2: Idempotency: same inputs, same output (validates caching correctness)
    @Property
    void eligibility_isIdempotent(
            @ForAll("validBooleanDsls") String dsl,
            @ForAll("applicants") Applicant applicant) {
        assertThat(evaluator.evaluateEligibility(dsl, applicant))
                .isEqualTo(evaluator.evaluateEligibility(dsl, applicant));
    }

    @Property
    void premium_isIdempotent(
            @ForAll("validPremiumDsls") String dsl,
            @ForAll("applicants") Applicant applicant) {
        assertThat(evaluator.evaluatePremium(dsl, applicant))
                .isEqualByComparingTo(evaluator.evaluatePremium(dsl, applicant));
    }

    // Invariant 3: Null safety: declared defaults, never exceptions
    @Property
    void nullDsl_eligibility_returnsFalse(@ForAll("applicants") Applicant applicant) {
        assertThat(evaluator.evaluateEligibility(null, applicant)).isFalse();
    }

    @Property
    void nullApplicant_eligibility_returnsFalse(@ForAll String dsl) {
        assertThat(evaluator.evaluateEligibility(dsl, null)).isFalse();
    }

    @Property
    void nullDsl_premium_returnsZero(@ForAll("applicants") Applicant applicant) {
        assertThat(evaluator.evaluatePremium(null, applicant))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Property
    void nullApplicant_premium_returnsZero(@ForAll String dsl) {
        assertThat(evaluator.evaluatePremium(dsl, null))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    // Invariant 4: Known-variable contract: never rejected for "unknown variable"
    @Property
    void validBooleanDsl_neverRejectsKnownVariables(
            @ForAll("validBooleanDsls") String dsl,
            @ForAll("applicants") Applicant applicant) {
        // only a SpEL type-mismatch at runtime is admissible
        assertThatCode(() -> evaluator.evaluateEligibility(dsl, applicant))
                .satisfiesAnyOf(
                        code -> assertThat(code).doesNotThrowAnyException(),
                        code -> assertThat(code).isInstanceOf(SpelEvaluationException.class));
    }

    @Property
    void validPremiumDsl_returnsNonNull(
            @ForAll("validPremiumDsls") String dsl,
            @ForAll("applicants") Applicant applicant) {
        assertThat(evaluator.evaluatePremium(dsl, applicant)).isNotNull();
    }

    @Property
    void unknownVariableDsl_alwaysThrowsIllegalArgumentException(
            @ForAll("unknownVariableDsls") String dsl,
            @ForAll("applicants") Applicant applicant) {
        assertThatCode(() -> evaluator.evaluateEligibility(dsl, applicant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown variable");
    }

    @Property
    void malformedDsl_alwaysThrowsSpelParseException(
            @ForAll("malformedDsls") String dsl,
            @ForAll("applicants") Applicant applicant) {
        assertThatCode(() -> evaluator.evaluateEligibility(dsl, applicant))
                .isInstanceOf(SpelParseException.class);
    }

    @Provide
    Arbitrary<Applicant> applicants() {
        return Combinators.combine(
                Arbitraries.integers().between(0, 120),
                Arbitraries.of(Gender.class),
                Arbitraries.integers().between(0, 50)
        ).as(Applicant::of);
    }
    
    @Provide
    Arbitrary<String> validBooleanDsls() {
        Arbitrary<String> intVar = Arbitraries.of("age", "claimFreeYears");
        Arbitrary<String> op = Arbitraries.of(">=", "<=", ">", "<", "==", "!=");
        Arbitrary<Integer> val = Arbitraries.integers().between(0, 100);

        Arbitrary<String> intComparison = Combinators.combine(intVar, op, val)
                .as("%s %s %d"::formatted);

        Arbitrary<String> genderCheck = Arbitraries.of(
                "gender == 'MALE'", "gender == 'FEMALE'", "gender != 'MALE'");

        Arbitrary<String> simple = Arbitraries.oneOf(intComparison, genderCheck);
        Arbitrary<String> compound = Combinators.combine(simple, simple)
                .as("(%s) AND (%s)"::formatted);

        return Arbitraries.oneOf(simple, compound);
    }

    @Provide
    Arbitrary<String> unknownVariableDsls() {
        Arbitrary<String> unknownVar = Arbitraries.of("salary", "income", "score", "risk", "bmi");
        Arbitrary<String> op = Arbitraries.of(">=", "<=", ">", "<", "==");
        Arbitrary<Integer> val = Arbitraries.integers().between(0, 100000);
        return Combinators.combine(unknownVar, op, val)
                .as("%s %s %d"::formatted);
    }

    @Provide
    Arbitrary<String> malformedDsls() {
        Arbitrary<String> validToken = Arbitraries.of("age", "claimFreeYears", "18");
        Arbitrary<String> danglingOp = Arbitraries.of(" >=", " <=", " ==", " +", " AND");
        return Combinators.combine(validToken, danglingOp)
                .as((token, op) -> token + op);
    }

    @Provide
    Arbitrary<String> validPremiumDsls() {
        Arbitrary<Integer> base = Arbitraries.integers().between(0, 1000);
        Arbitrary<Integer> ageFactor = Arbitraries.integers().between(0, 50);
        Arbitrary<Integer> cfyDiscount = Arbitraries.integers().between(0, 30);

        return Combinators.combine(base, ageFactor, cfyDiscount)
                .as("%d + (age * %d) - (claimFreeYears * %d)"::formatted);
    }
}
