package com.insurance.policy_evaluator_dsl;

import com.insurance.policy_evaluator_dsl.infrastructure.persistence.PolicyJpaRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;

/**
 * Base class for integration tests that need a running server.
 *
 * <p>Spring's test context cache reuses the same application context — and the same embedded
 * server — for every subclass that inherits this configuration without overriding it. This means
 * the server boots exactly once per test run regardless of how many subclasses exist.
 *
 * <p>The database is wiped before each test so test cases are independent and can run in any order.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseITest {

    @LocalServerPort
    private int port;

    @Autowired
    private PolicyJpaRepository policyJpaRepository;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        policyJpaRepository.deleteAll();
    }

    protected long createPolicy(String name, String eligibilityDsl,
                                double basePremium, String variablePremiumDsl, String currency) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "%s",
                          "eligibilityDsl": "%s",
                          "basePremium": %s,
                          "variablePremiumDsl": "%s",
                          "currency": "%s"
                        }
                        """.formatted(name, eligibilityDsl, basePremium, variablePremiumDsl, currency))
                .when()
                .post("/api/v1/policies")
                .then()
                .statusCode(200)
                .extract()
                .<Number>path("id")
                .longValue();
    }

    protected void updatePremium(long policyId, double basePremium, String variablePremiumDsl, String currency) {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "basePremium": %s,
                          "variablePremiumDsl": "%s",
                          "currency": "%s"
                        }
                        """.formatted(basePremium, variablePremiumDsl, currency))
                .when()
                .put("/api/v1/policies/{id}/update-premium", policyId)
                .then()
                .statusCode(200);
    }
}
