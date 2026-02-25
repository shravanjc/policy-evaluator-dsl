# Policy Evaluation DSL Service
- Evaluates insurance policy eligibility for member details and computes premiums using a custom
  DSL 'rule engine' (Spring Expression Language).
- Default rules are set in [application.properties](./src/main/resources/application.properties)
    - They can be updated at runtime (without a server restart to simulate an external config setup
      in production)
- Policy specific rules can be set to override that.
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
api → application → domain ← infrastructure
```

Infrastructure depends on the domain - not the other way around - by implementing
its ports (`PolicyRepository`, `DslEvaluator`).

> **Tradeoff**: because `Policy` is also the JPA entity, the domain carries `@Entity`/`@Column`
> annotations - a deliberate JPA coupling accepted in exchange for eliminating the
> domain ↔ persistence mapping layer.

### Layer breakdown

| Layer                        | Package                       | Key classes                                                                 | Responsibility                                                      |
|------------------------------|-------------------------------|-----------------------------------------------------------------------------|---------------------------------------------------------------------|
| API                          | `api/`                        | `PolicyController`, `EvaluationController`, `GlobalExceptionHandler`        | HTTP in/out; maps API DTOs ↔ domain; delegates to application layer |
| API contract (generated)     | `dto/`                        | `CreatePolicyRequest`, `PolicyResponse`, `EvaluationRequest/Response`, etc. | Generated from `openapi.yaml`; represent the public HTTP contract   |
| API mapper                   | `api/mapper/`                 | `PolicyApiMapper`                                                           | Maps between API DTOs and domain objects                            |
| Application                  | `application/`                | `PolicyManagementService`, `EvaluationService`                              | Use-case orchestration: load → execute → persist → return           |
| Domain model                 | `domain/model/`               | `Policy` (@Entity), `Applicant`, `EligibilityResult`, `Gender`              | Domain state, invariants, and business behaviour                    |
| Domain service               | `domain/service/`             | `DslEvaluator` (port), `PolicyEvaluationService`                            | Business rules spanning multiple objects; no framework dependencies |
| Domain repository            | `domain/repository/`          | `PolicyRepository` (port)                                                   | Persistence abstraction; implemented in infrastructure              |
| Infrastructure – persistence | `infrastructure/persistence/` | `PolicyJpaRepository`, `PolicyPersistenceAdapter`                           | Spring Data JPA implementation of `PolicyRepository`                |
| Infrastructure – DSL         | `infrastructure/dsl/`         | `SpelDslEvaluator`                                                          | SpEL implementation of `DslEvaluator`                               |
| Infrastructure – config      | `infrastructure/config/`      | `PolicyProperties`, `DefaultPolicySeeder`                                   | Config-property binding and default data seeding                    |

---

## Quick Start

Run the application. It also runs the ./gradlew openApiGenerate task which generates the controller
interfaces and dtos based on the: [OpenApiSpec](./src/main/resources/openapi.yaml)
```bash 
./gradlew bootRun
```

Open http://localhost:8080/swagger-ui.html

---

## Build & Test

```bash
# All unit + integration tests
./gradlew test
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
  "variablePremium": "(age * 7) - (claimFreeYears * 15)",
  "currency": "EUR"
}
```

----------------------------------------------------------------------------------------------
**Update premium:**
POST /api/v1/policies/{id}/update-premium

```json
{
  "basePremium": 400,
  "variablePremium": "(age * 7) - (claimFreeYears * 15)",
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