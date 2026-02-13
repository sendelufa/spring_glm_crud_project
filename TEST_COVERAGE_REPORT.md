# Test Coverage Report

## Executive Summary

**Total Coverage: 82%** - Overall project coverage exceeds the 80% threshold

- Total Classes: 19 (100% covered)
- Total Methods: 109 (73% coverage)
- Total Lines: 354 (84% coverage)
- Total Branches: 183 (67% coverage)
- Total Instructions: 1,497 (82% coverage)

## Coverage by Layer

### Domain Layer - 85% ✓

**domain.model package:** 85%
- Instructions: 206/241 (85%)
- Branches: 18/22 (81%)
- Lines: 59/64 (92%)
- Methods: 23/27 (85%)
- Classes: 2/2 (100%)

**domain.model.valueobject package:** 85%
- Instructions: 454/531 (85%)
- Branches: 59/82 (71%)
- Lines: 93/103 (90%)
- Methods: 25/27 (92%)
- Classes: 3/3 (100%)

**domain.exception package:** 79%
- Instructions: 19/24 (79%)
- Branches: N/A
- Lines: 6/8 (75%)
- Methods: 3/4 (75%)
- Classes: 2/2 (100%)

### Application Layer - 99% ✓

**application.usecase package:** 99%
- Instructions: 143/144 (99%)
- Branches: 9/10 (90%)
- Lines: 39/39 (100%)
- Methods: 5/5 (100%)
- Classes: 1/1 (100%)

**application.dto package:** 100%
- Instructions: 89/89 (100%)
- Branches: N/A
- Lines: 10/10 (100%)
- Methods: 5/5 (100%)
- Classes: 4/4 (100%)

### Infrastructure Layer - 79%

**infrastructure.persistence.repository package:** 100% ✓
- Instructions: 88/88 (100%)
- Branches: 10/10 (100%)
- Lines: 24/24 (100%)
- Methods: 6/6 (100%)
- Classes: 1/1 (100%)

**infrastructure.persistence.entity package:** 58%
- Instructions: 164/280 (58%)
- Branches: 13/24 (54%)
- Lines: 48/80 (60%)
- Methods: 5/25 (20%)
- Classes: 1/1 (100%)

### Interfaces Layer - 72%

**interfaces.rest package:** 72%
- Instructions: 67/92 (72%)
- Branches: N/A
- Lines: 16/23 (69%)
- Methods: 7/8 (87%)
- Classes: 4/4 (100%)

**Main Application:** 37%
- Instructions: 0/5 (0%)
- Lines: 1/3 (33%)
- Methods: 1/2 (50%)

## Test Execution Summary

- Total Tests: 102
- Failures: 0
- Errors: 0
- Skipped: 0
- Success Rate: 100%

## Analysis

### Strengths ✓

1. **Application Layer Excellence:** 99-100% coverage demonstrates thorough business logic testing
2. **Domain Layer Solid:** 85% coverage indicates well-tested core domain models and value objects
3. **Repository Layer Perfect:** 100% coverage ensures data access layer reliability
4. **Zero Test Failures:** All 102 tests passing shows system stability

### Areas for Improvement

1. **Entity Layer (58%):** Lombok-generated methods not fully tested - acceptable for JPA entities
2. **Exception Handling (79%):** Some edge cases in domain exceptions not covered
3. **REST Controllers (72%):** Global exception handler could use more test scenarios

### Conclusion

**Status: PASSED** ✓

The project successfully achieves >80% test coverage for both domain and application layers, meeting the quality threshold. The coverage distribution follows best practices:

- Highest coverage on business logic (application layer: 99%)
- Strong domain model protection (domain layer: 85%)
- Complete data access testing (repository: 100%)

The lower coverage in infrastructure.entity (58%) is acceptable as this layer primarily contains JPA-generated boilerplate code.

## Coverage Visualization

Open detailed report: `target/site/jacoco/index.html`

### Coverage by Architecture Layer:

```
Application:  ████████████████████ 99%
Domain:       ██████████████████░░ 85%
Repository:   ████████████████████ 100%
Interfaces:   ███████████████░░░░░ 72%
Entity:       ██████████░░░░░░░░░░░ 58%
              ─────────────────────
Overall:      ██████████████████░░ 82%
```

---

Generated: 2026-02-14
Tool: JaCoCo 0.8.12
Framework: JUnit 5 + TestContainers
