# Policy Evaluation DSL Service

- Evaluates insurance policy eligibility for member details and computes premiums using a custom
  DSL 'rule engine' (Spring Expression Language).
- They can be set at individual policy level.
- Uses H2 in-memory SQL database for demo purposes.

## Stack:

- Spring Boot 4.0.3
- Java 25
- Gradle 9.3.1
- DDD (Domain driven development) albeit not classic (Domain objects are JPA entity to reduce
  overkill of additional needed mapping)
- Custom DSL

## Architecture (DDD)

> **Pragmatic DDD**: `Policy` serves as both the domain model and JPA entity (`@Entity`) to
> avoid a redundant mapping layer. The public API contract (OpenAPI-generated DTOs) is kept
> separate so the HTTP shape can evolve independently of the domain.

### Dependency rule

```
api -> application -> domain <- infrastructure
```

Infrastructure depends on the domain - not the other way around - by implementing
its ports (`PolicyRepository`, `DslEvaluator`).

> **Tradeoff**: because `Policy` is also the JPA entity, the domain carries `@Entity`/`@Column`
> annotations - a deliberate JPA coupling accepted in exchange for eliminating the
> domain ↔ persistence mapping layer.

### Layer breakdown

| Layer                        | Package                       | Key classes                                                                 | Responsibility                                                        |
|------------------------------|-------------------------------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------|
| API                          | `api/`                        | `PolicyController`, `EvaluationController`, `GlobalExceptionHandler`        | HTTP in/out; maps API DTOs <-> domain; delegates to application layer |
| API contract (generated)     | `dto/`                        | `CreatePolicyRequest`, `PolicyResponse`, `EvaluationRequest/Response`, etc. | Generated from `openapi.yaml`; represent the public HTTP contract     |
| API mapper                   | `api/mapper/`                 | `PolicyApiMapper`                                                           | Maps between API DTOs and domain objects                              |
| Application                  | `application/`                | `PolicyManagementService`, `EvaluationService`                              | Use-case orchestration: load -> execute -> persist -> return          |
| Domain model                 | `domain/model/`               | `Policy` (@Entity), `Applicant`, `EligibilityResult`, `Gender`              | Domain state, invariants, and business behaviour                      |
| Domain service               | `domain/service/`             | `DslEvaluator` (port), `PolicyEvaluationService`                            | Business rules spanning multiple objects; no framework dependencies   |
| Domain repository            | `domain/repository/`          | `PolicyRepository` (port)                                                   | Persistence abstraction; implemented in infrastructure                |
| Infrastructure – persistence | `infrastructure/persistence/` | `PolicyJpaRepository`                                                       | Spring Data JPA implementation of `PolicyRepository`                  |
| Infrastructure – DSL         | `infrastructure/dsl/`         | `SpelDslEvaluator`                                                          | SpEL implementation of `DslEvaluator`                                 |
| Infrastructure – config      | `infrastructure/config/`      | `PolicyProperties`, `DefaultPolicySeeder`                                   | Config-property binding and default data seeding                      |

### Tests covered

| Type                               | Why                                                                                                                                                                                                                                                                                                                 | Location                                                                                                                                                                                                                                                                   |
|------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Controller contract**            | Verifies HTTP status codes, request/response shapes and JSON field mapping without a running server. MockMvc + Mockito; services are mocked so failures are always in the controller layer.                                                                                                                         | [`api/PolicyControllerTest`](src/test/java/com/insurance/policy_evaluator_dsl/api/PolicyControllerTest.java), [`api/EvaluationControllerTest`](src/test/java/com/insurance/policy_evaluator_dsl/api/EvaluationControllerTest.java)                                         |
| **Global exception handler**       | Proves the advice maps each exception type to the correct HTTP status and that internal details are never leaked (500 returns a generic message). Uses real controllers with mocked services to keep ArchUnit happy.                                                                                                | [`api/GlobalExceptionHandlerTest`](src/test/java/com/insurance/policy_evaluator_dsl/api/GlobalExceptionHandlerTest.java)                                                                                                                                                   |
| **Application service**            | Validates use-case orchestration: correct delegation to repository and domain service, `NoSuchElementException` on missing policy, DSL validation on create/update.                                                                                                                                                 | [`application/PolicyManagementServiceTest`](src/test/java/com/insurance/policy_evaluator_dsl/application/PolicyManagementServiceTest.java), [`application/EvaluationServiceTest`](src/test/java/com/insurance/policy_evaluator_dsl/application/EvaluationServiceTest.java) |
| **Domain service (parameterised)** | Data-driven eligibility + premium scenarios loaded from CSV, covering eligible, ineligible and boundary cases. Adding a new scenario requires only a new CSV row.                                                                                                                                                   | [`domain/service/PolicyEvaluationServiceTest`](src/test/java/com/insurance/policy_evaluator_dsl/domain/service/PolicyEvaluationServiceTest.java), [`policy_premium_details.csv`](src/test/resources/policy_premium_details.csv)                                            |
| **DSL evaluator (parameterised)**  | Tests the SpEL implementation directly against real expression/applicant combinations from CSV; also tests that DSLs referencing unknown variables are rejected with `IllegalArgumentException`.                                                                                                                    | [`infrastructure/dsl/SpelDslEvaluatorTest`](src/test/java/com/insurance/policy_evaluator_dsl/infrastructure/dsl/SpelDslEvaluatorTest.java), [`evaluation_test_details.csv`](src/test/resources/evaluation_test_details.csv)                                                |
| **DSL evaluator (property-based)** | jqwik generates thousands of inputs to verify invariants that example tests cannot exhaustively cover: sandbox safety (arbitrary strings only surface typed exceptions), idempotency (cache correctness), null-input defaults, known-variable contract, unknown-variable rejection, and malformed-syntax rejection. | [`infrastructure/dsl/SpelDslEvaluatorPropertyTest`](src/test/java/com/insurance/policy_evaluator_dsl/infrastructure/dsl/SpelDslEvaluatorPropertyTest.java)                                                                                                                 |
| **Architecture**                   | ArchUnit rules enforced on every build: layer dependency direction, all `@RestController` classes must implement a generated API interface, all `@Service` classes must be `@Transactional`. Prevents accidental cross-layer shortcuts from being silently introduced.                                              | [`architecture/ArchitectureRulesTest`](src/test/java/com/insurance/policy_evaluator_dsl/architecture/ArchitectureRulesTest.java)                                                                                                                                           |
| **Integration (full stack)**       | Boots a real Spring context against an in-memory H2 database, exercises the HTTP stack end-to-end via REST Assured. Covers create → evaluate, update-premium → re-evaluate, and error paths against live data.                                                                                                      | [`src/itest`](src/itest/java/com/insurance/policy_evaluator_dsl/api/)                                                                                                                                                                                                      |

---

## Quick Start

Run the application. It also runs the ./gradlew openApiGenerate task which generates the controller
interfaces and dtos based on the: [OpenApiSpec](./src/main/resources/openapi.yaml)

```bash 
./gradlew bootRun
```

Open http://localhost:8080/swagger-ui.html for accessing and trying all APIs.

---

## Build & Test

```bash
# Unit tests only (controller, service, domain, DSL evaluator, architecture)
./gradlew test
```

```
# Integration tests only (full-stack REST Assured against embedded H2)
./gradlew itest
```

```
# All tests
./gradlew check
```

```
# Generate jacoco coverage reports
./gradlew jacocoTestReport
```

---

## DSL Reference

Three variables are available in all expressions:

| Variable         | Type     | Required?      | Example               |
|------------------|----------|----------------|-----------------------|
| `age`            | `int`    | Y              | `age >= 18`           |
| `gender`         | `String` | Y              | `gender == 'MALE'`    |
| `claimFreeYears` | `int`    | N (default: 0) | `claimFreeYears >= 2` |

**Eligibility DSL** – must return `boolean`

```
age >= 18 AND age <= 65 AND claimFreeYears >= 2
age >= 18 AND (gender == 'MALE' OR gender == 'FEMALE')
(age < 25 OR age > 60) AND claimFreeYears >= 1
```

**Premium DSL** – must return a number; rounded to 2 decimal places

```
500 + (age * 8) - (claimFreeYears * 20)
gender == 'FEMALE' ? 400 + (age * 6) : 420 + (age * 7)
age < 30 ? 250 : 500
```

---

## API

Full specification can be seen here: [OpenApiSpec](./src/main/resources/openapi.yaml)

| Method   | Path                                   | Description        |
|----------|----------------------------------------|--------------------|
| `POST`   | `/api/v1/policies`                     | Create policy      |
| `GET`    | `/api/v1/policies`                     | List all           |
| `GET`    | `/api/v1/policies/{id}`                | Get by ID          |
| `PUT`    | `/api/v1/policies/{id}/update-premium` | Update premium     |
| `DELETE` | `/api/v1/policies/{id}`                | Delete             |
| `POST`   | `/api/v1/policies/{id}/evaluate`       | Evaluate applicant |

----------------------------------------------------------------------------------------------
**Create policy:**
POST /api/v1/policies

```json
{
  "name": "My Policy",
  "description": "Description",
  "eligibilityDsl": "age >= 18 AND age <= 60 AND claimFreeYears >= 1",
  "basePremium": 400,
  "variablePremiumDsl": "(age * 7) - (claimFreeYears * 15)",
  "currency": "EUR"
}
```

----------------------------------------------------------------------------------------------
**Update premium:**
POST /api/v1/policies/{id}/update-premium

```json
{
  "basePremium": 400,
  "variablePremiumDsl": "(age * 7) - (claimFreeYears * 15)",
  "currency": "EUR"
}
```

----------------------------------------------------------------------------------------------
**Evaluate request:**
POST /api/v1/policies/{id}/evaluate

```json
{
  "gender": "MALE",
  "age": 35,
  "claimFreeYears": 5
}
```

**Eligible response:**

```json
{
  "policyId": 123,
  "eligible": true,
  "premium": 640.00,
  "currency": "EUR",
  "reason": null
}
```

**Ineligible response:**

```json
{
  "policyId": 123,
  "eligible": false,
  "reason": "Not eligible"
}
```

---
## Future Design Goals

This section outlines how the system could evolve from a demo into a production-grade, multi-tenant policy evaluation platform.

### 1. Scale and Performance

- Support high throughput (tens of thousands of evaluations per second) by keeping the evaluator stateless and horizontally scalable.
- Introduce Caffeine (for single pod) or Redis (for full scale distributed caching) of compiled DSL expressions to avoid re-parsing rules on every request while keeping cache invalidation explicit and test-covered.
- Indexing and optimizing database access patterns for any bulk evaluations requests (e.g., batched fetches of policies, read-only replicas for evaluation traffic).

### 2. Safety and Correctness

- Harden the DSL sandbox so rules cannot access arbitrary classes, reflection, I/O, or network calls, and can only see a carefully curated set of variables and functions.
- Enable policy audit that captures policy version, input snapshot, and final outcome to enable post-hoc analysis and reproducibility.

### 3. Domain and DSL Evolution

- Introduce a versioned policy lifecycle with states such as `DRAFT`, `PUBLISHED`, and `DEPRECATED`, keeping historical versions immutable so past decisions can be replayed exactly.
- Define a formal “DSL schema” describing allowed variables, functions, and operators, and control new features via feature flags per environment or tenant.
- Generalize from a single policy type to multiple product lines with configurable DSL variables used in evaluation while reusing the same evaluation engine.

### 4. API, Contracts, and Evolution

- Document an API evolution strategy: additive changes first, explicit deprecation periods, and (if needed) versioned endpoints or resource representations.
- Add contract tests that verify the generated OpenAPI DTOs and controllers remain backward compatible as the domain model evolves.
- Define compatibility tests between DSL versions so existing policies continue to evaluate correctly when new DSL features are introduced.

### 5. Observability and Operations

- Expose metrics via prometheus and grafana such as evaluation latency (p50, p95, p99), error rates by error category, cache hit ratio, and evaluation volume per policy version.
- Set explicit SLOs for availability and latency, and implement timeouts, retries, and circuit breakers for external dependencies such as databases.

### 6. Security and Multi-Tenancy

- Add first-class tenant concepts to the domain and persistence layers, ensuring strict tenant scoping for policies and evaluations and preventing cross-tenant data access.
- Integrate a RBAC (role based access control) authorization layer that controls who can create, edit, and publish policies or rules, and who can invoke evaluations, with all actions audited.
- Document and test the security model of the SpEL configuration, including explicit tests for attempts to bypass the sandbox.

### 7. Platform and Ecosystem

- Extend the `DslEvaluator` port truly custom and domain specific when the criteria goes beyond Spring's Expression Language set. Current design supports this loosely coupled system via interfaces.
- Dockerize the application and create Github actions for automatic build and artifact deployment to AWS (or other cloud provider) as part of CI/CD orchestration. 
- Describe deployment and rollout strategies (blue/green, canary) for new policy versions and DSL features, including schema migration and rollback plans.
