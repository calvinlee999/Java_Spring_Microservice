# Repository Summary: `spring-data-3-5959699` (LinkedIn Learning)

> **Purpose:** Educational demo showcasing multiple Spring Data 3 query strategies across two contrasting persistence paradigms — reactive NoSQL and blocking relational.

---

## Module Overview

| Attribute | `college` | `university` |
|---|---|---|
| **Spring Boot** | 3.3.5 | 3.3.5 |
| **Java** | 21 | 21 |
| **Persistence** | MongoDB (Reactive) | PostgreSQL / H2 (JPA) |
| **Web Stack** | [WebFlux (non-blocking, reactive)](./WEBFLUX_VS_VIRTUAL_THREADS.md) | Spring MVC (blocking, servlet) |
| **API Exposure** | SpringDoc OpenAPI 2.5.0 (WebFlux UI) | SpringDoc OpenAPI 2.5.0 (MVC UI) + Spring Data REST |
| **Build** | Maven + `spring-boot-maven-plugin` | Maven + QueryDSL APT codegen |
| **Local Dev** | `spring-boot-docker-compose` (auto-starts Mongo) | `docker-compose.yml` + `postgres/schema.sql` |
| **Test DB** | — (no tests) | H2 in-memory |

> **Why WebFlux for `college`?** The `college` module uses Spring WebFlux + Project Reactor to demonstrate non-blocking, reactive data access with MongoDB.  
> For a deep-dive comparison of **WebFlux vs Java 21 Virtual Threads** — including concurrency model, backpressure, programming model complexity, and when to choose each — see [WEBFLUX_VS_VIRTUAL_THREADS.md](./WEBFLUX_VS_VIRTUAL_THREADS.md).

---

## Domain Model

| Entity | Module | Type | Key Relationships | Notes |
|---|---|---|---|---|
| `Person` | both | Embedded value object | — | No `@Id`; pure data carrier |
| `Staff` | college | `@Document` | embeds `Person member` | String (ObjectId) PK |
| `Department` | college | `@Document` | references `Staff chair` (embedded doc) | String (ObjectId) PK |
| `Staff` | university | `@Entity` | `@Embedded Person member` | Integer PK, `@GeneratedValue` |
| `Student` | university | `@Entity` | `@Embedded Person attendee` | `fullTime`, `age` fields |
| `Department` | university | `@Entity` | `@ManyToOne Staff chair` | |
| `Course` | university | `@Entity` | `@ManyToOne` instructor + department; `@ManyToMany` self-referential prerequisites | `FetchType.EAGER` on prerequisites |
| `ShowChair` | university | Projection interface | subset of Department | Spring Data projections demo |

> **Design Note:** `Person` is deliberately a plain embedded type (not an entity) — a correct value-object pattern, though use of Java Records or Lombok would reduce boilerplate.

---

## Repository Layer & Query Strategies

| Repo | Module | Extends | Query Mechanisms Demonstrated |
|---|---|---|---|
| `StaffRepo` | college | `ReactiveCrudRepository` | Derived method (`findByMemberLastName`) |
| `DepartmentRepo` | college | `ReactiveCrudRepository` | Derived method, `@Query` with MongoDB JSON regex `{ 'name': { $regex: ?0 } }` |
| `StaffRepo` | university | `JpaRepository` | JPQL `@Query` with `@Param` |
| `StudentRepo` | university | `JpaRepository` | Derived methods, JPQL `@Query`, **native SQL** `@Query(nativeQuery=true)` |
| `CourseRepo` | university | `JpaRepository` + `JpaSpecificationExecutor` | Derived methods (incl. nested path: `findByDepartmentChairMemberLastName`), **JPA Criteria / Specification** |
| `CourseQueryDslRepo` | university | `JpaRepository` + `QuerydslPredicateExecutor` | **QueryDSL predicates** via generated Q-types |
| `DepartmentRepo` | university | `JpaRepository` | Basic CRUD |

---

## Query Strategy Matrix

| Strategy | API | Where Used | Notes |
|---|---|---|---|
| Derived query methods | Spring Data naming convention | `StudentRepo`, `CourseRepo`, `StaffRepo` (college) | Includes deep nested traversal (`findByDepartmentChairMemberLastName`) |
| JPQL `@Query` | `@Query` + JPQL | `StaffRepo`, `StudentRepo` | Named params via `@Param` and positional `?1` |
| Native SQL `@Query` | `@Query(nativeQuery=true)` | `StudentRepo` | `LIMIT`, `ORDER BY` — non-portable |
| JPA Criteria / Specification | `JpaSpecificationExecutor` | `DynamicQueryService` | Inline lambda spec building with `Predicate` list |
| QueryDSL | `QuerydslPredicateExecutor` + `QCourse` | `DynamicQueryService` | `BooleanBuilder` with generated Q-types (APT); jakarta classifier |
| Query by Example (QBE) | `Example.of(entity)` | `DynamicQueryService` | Simple but limited; no null handling strategy shown |
| MongoDB `@Query` / JSON | `@Query` with `{}` syntax | `DepartmentRepo` (college) | Reactive `Flux` return |

---

## Service & Business Layer

| Class | Role | Notes |
|---|---|---|
| `UniversityService` | CRUD facade over all repos | Constructor injection (correct). `@Service` annotation present but also has a stray `@Repository` import. No `@Transactional`. |
| `DynamicQueryService` | Demonstrates 3 dynamic query strategies on `Course` | Side-by-side Specification vs. QueryDSL vs. QBE — pedagogically clear |
| `CourseFilter` | Filter DTO using `Optional<T>` fields | Clean optional-based filter object; avoids null checks |

---

## Test Coverage

| Test Class | Focus | Tech |
|---|---|---|
| `SimpleDBCrudTest` | Basic save/find/delete | `JpaRepository` CRUD, H2 |
| `FindByOneAttribute` | Single-attribute derived queries | Derived method queries |
| `FindByClausesAndExpressions` | Multi-attribute + boolean logic | Complex derived queries |
| `PagingTest` | Pagination / sorting | `Pageable`, `Page<T>`, `Sort` |
| `CriteriaQueryTest` | Dynamic queries | Specification, QueryDSL, QBE |
| `UniversityFactory` | Test data builder | Shared fixture via `UniversityService` |

---

## Architectural Observations (Principal Engineer Lens)

| Area | Observation | Severity |
|---|---|---|
| **No Lombok / Records** | All domain classes use manual getters/setters; Java 21 Records would eliminate boilerplate | Low (intentional for clarity in a course) |
| **No DTO layer** | Entities leaked directly to service layer; acceptable for demo, anti-pattern in production | Medium |
| **Missing `@Transactional`** | `UniversityService` mutating methods lack transaction boundaries | Medium |
| **`FetchType.EAGER` on `@ManyToMany`** | `Course.prerequisites` is `EAGER` — can cause N+1 and performance issues at scale | Medium |
| **No college tests** | Reactor/WebFlux repo layer (`college`) has zero tests — `StepVerifier` would be expected | High |
| **Stray `@Repository` import** | `UniversityService` imports `@Repository` annotation (unused) — minor code smell | Low |
| **Native queries with `LIMIT`** | Non-portable; Spring Data's `Pageable` or `Top`/`First` derived syntax is preferred | Low |
| **Branch structure** | Branch `06_03e` naming convention (`chapter_lesson`) confirms this is a LinkedIn Learning course progression repo | Info |

---

## See Also

| Document | Description |
|---|---|
| [WEBFLUX_VS_VIRTUAL_THREADS.md](./WEBFLUX_VS_VIRTUAL_THREADS.md) | Principal Architect comparison: WebFlux (used in `college`) vs Java 21 Virtual Threads — concurrency models, backpressure, programming model, decision matrix |
| [ARCHITECTURE_C4_LEVELS.md](./ARCHITECTURE_C4_LEVELS.md) | C4 Level 0 (Context), Level 1 (Container), Level 2 (Component) diagrams with full cross-level traceability |
| [ARCHITECTURE_DIAGRAM.md](./ARCHITECTURE_DIAGRAM.md) | Full component-level architecture diagram with query strategy flowchart |

---

*Generated on 2026-03-03 by GitHub Copilot — architectural analysis of [LinkedInLearning/spring-data-3-5959699](https://github.com/LinkedInLearning/spring-data-3-5959699)*
