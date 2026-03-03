# C4 Architecture Diagrams — `spring-data-3-5959699`

> Multi-level architecture following the **C4 Model** (Context → Container → Component).  
> Each level zooms in from the previous, providing increasing detail about the system.

---

## Diagram Level Map

```
┌─────────────────────────────────────────────────────────────────┐
│  LEVEL 0 — System Context    (Who uses it? What does it touch?) │
│     ↓  zoom in                                                   │
│  LEVEL 1 — Container         (What Spring Boot apps + DBs?)     │
│     ↓  zoom in                                                   │
│  LEVEL 2 — Component         (Layers, repos, services, tests)   │
│             → See ARCHITECTURE_DIAGRAM.md                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## Level 0 — System Context Diagram

> **Scope:** The entire demo platform as a single black box.  
> Shows: who interacts with it, and which external systems it depends on.

```mermaid
C4Context
    title Level 0 — System Context Diagram · spring-data-3-5959699

    Person(dev, "Developer / Learner", "Java engineer studying\nSpring Data 3 patterns")

    System(springdata, "Spring Data 3 Demo Platform", "Demonstrates reactive NoSQL\nand relational JPA data access\npatterns using Spring Boot 3.3.5 / Java 21")

    System_Ext(mongo, "MongoDB", "Document database\nfor reactive NoSQL patterns")
    System_Ext(postgres, "PostgreSQL", "Relational database\nfor JPA/Hibernate patterns")
    System_Ext(docker, "Docker / Compose", "Local infrastructure\ncontainer orchestration")
    System_Ext(openapi, "OpenAPI / Swagger UI", "Auto-generated REST API\ndocumentation & testing UI")
    System_Ext(github, "GitHub", "Source control\n& course branch versioning")

    Rel(dev, springdata, "Writes / runs / studies", "IDE / Codespaces")
    Rel(springdata, mongo, "Reads & writes documents", "Spring Data MongoDB Reactive")
    Rel(springdata, postgres, "Reads & writes rows", "Spring Data JPA / Hibernate")
    Rel(springdata, openapi, "Exposes REST APIs via", "HTTP/JSON + HAL")
    Rel(docker, mongo, "Starts & manages")
    Rel(docker, postgres, "Starts & manages")
    Rel(dev, github, "Pushes / pulls branches", "Git")
    Rel(github, springdata, "Course progression branches\n(42 branches: xx_xxe format)")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

### Level 0 — Element Descriptions

| Element | Type | Description |
|---|---|---|
| **Developer / Learner** | Person | Primary user — a Java engineer working through the LinkedIn Learning course or referencing patterns |
| **Spring Data 3 Demo Platform** | System (ours) | Two Spring Boot 3.3.5 / Java 21 applications demonstrating data access patterns |
| **MongoDB** | External System | Document store used by the `college` module; managed via Docker Compose |
| **PostgreSQL** | External System | Relational DB used by the `university` module (runtime); managed via Docker Compose |
| **Docker / Compose** | External System | Infrastructure orchestrator that starts MongoDB and PostgreSQL locally |
| **OpenAPI / Swagger UI** | External System | Auto-generated API documentation exposed by both modules via springdoc-openapi 2.5.0 |
| **GitHub** | External System | Hosts 42 course-progression branches (`06_03e` = chapter 6, lesson 3, end state) |

---

## Level 1 — Container Diagram

> **Scope:** Inside the Spring Data 3 Demo Platform.  
> Shows: the two Spring Boot containers, their databases, and how they relate.

```mermaid
C4Container
    title Level 1 — Container Diagram · spring-data-3-5959699

    Person(dev, "Developer / Learner", "Studies Spring Data 3\npatterns via running tests\nand Swagger UI")

    System_Boundary(platform, "Spring Data 3 Demo Platform") {

        Container(college, "college", "Spring Boot 3.3.5 · Java 21 · WebFlux", "Reactive NoSQL module.\nExposes reactive REST APIs for Staff & Department\nusing MongoDB Reactive Repositories")

        Container(university, "university", "Spring Boot 3.3.5 · Java 21 · Spring MVC", "Relational JPA module.\nDemonstrates 6 query strategies:\nDerived · JPQL · Native SQL · Specification · QueryDSL · QBE")

    }

    ContainerDb(mongo, "MongoDB", "Document Store · Docker Compose", "Stores Staff and Department documents.\nSchema-less, embedded sub-documents.")

    ContainerDb(postgres, "PostgreSQL", "Relational DB · Docker Compose", "Stores Course, Department, Staff, Student tables.\nRelational schema with foreign keys.")

    ContainerDb(h2, "H2 In-Memory", "Embedded SQL DB · test scope only", "Used by university test suite.\nAuto-schema from JPA entities.\nNo Docker required for tests.")

    System_Ext(swagger1, "Swagger UI (WebFlux)", "springdoc-openapi webflux 2.5.0", "API docs for college module")
    System_Ext(swagger2, "Swagger UI + HAL (WebMVC)", "springdoc-openapi webmvc 2.5.0 + Spring Data REST", "API docs + HAL browser for university module")

    Rel(dev, college, "HTTP requests / runs application", "Port 8080")
    Rel(dev, university, "HTTP requests / runs tests", "Port 8080")
    Rel(college, mongo, "Reactive CRUD · Flux / Mono", "Spring Data MongoDB Reactive")
    Rel(university, postgres, "Blocking CRUD · EntityManager / JPA", "Spring Data JPA · Hibernate ORM")
    Rel(university, h2, "Test-scope CRUD", "Spring Data JPA · H2 dialect")
    Rel(college, swagger1, "Auto-exposes API spec")
    Rel(university, swagger2, "Auto-exposes API spec + HAL endpoints")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

### Level 1 — Container Descriptions

| Container | Technology | Responsibility | Port |
|---|---|---|---|
| **college** | Spring Boot WebFlux + Spring Data MongoDB Reactive | Reactive, non-blocking REST API; demonstrates `ReactiveCrudRepository`, `Flux`/`Mono` return types, MongoDB `@Query` JSON syntax | 8080 |
| **university** | Spring Boot MVC + Spring Data JPA + QueryDSL | Blocking REST API; demonstrates 6 query strategies, Spring Data REST HAL exposure, JPA entity relationships | 8080 |
| **MongoDB** | MongoDB (Docker Compose) | Persists `Staff` and `Department` documents; schema-free, sub-document embedding | 27017 |
| **PostgreSQL** | PostgreSQL (Docker Compose) | Persists `Course`, `Department`, `Staff`, `Student` tables; relational integrity via FK constraints | 5432 |
| **H2 In-Memory** | H2 (test scope) | Lightweight in-memory DB for the `university` test suite; no infrastructure required | — |
| **Swagger UI (WebFlux)** | springdoc-openapi-starter-webflux-ui 2.5.0 | Auto-generated OpenAPI spec UI for `college` module | 8080/swagger-ui |
| **Swagger UI + HAL** | springdoc-openapi-starter-webmvc-ui 2.5.0 + Spring Data REST | Auto-generated OpenAPI spec + HAL browser for `university` module | 8080/swagger-ui |

---

## Level 0 → Level 1 → Level 2 Mapping

```mermaid
flowchart TD
    L0["🌐 LEVEL 0 — System Context\nSpring Data 3 Demo Platform\n(one black box)\n+\nMongoDB · PostgreSQL · Docker\nOpenAPI · GitHub · Developer"]

    L1A["📦 LEVEL 1 — Container: college\nSpring Boot WebFlux\n→ MongoDB"]
    L1B["📦 LEVEL 1 — Container: university\nSpring Boot MVC\n→ PostgreSQL / H2"]

    L2A["🔍 LEVEL 2 — Components: college\nDomain: @Document Staff, Department, Person\nRepo: ReactiveCrudRepository\n  StaffRepo · DepartmentRepo\nWeb: WebFlux + OpenAPI"]

    L2B_DOM["🔍 LEVEL 2 — Components: university · Domain\n@Entity: Course · Department · Staff · Student\nEmbedded: Person (value object)\nProjection: ShowChair\nRelationships: @ManyToOne · @ManyToMany"]

    L2B_REPO["🔍 LEVEL 2 — Components: university · Repository\nStaffRepo: JPQL @Query\nStudentRepo: Derived + JPQL + Native SQL\nDeptRepo: Projection\nCourseRepo: JpaSpecificationExecutor\nCourseQueryDslRepo: QuerydslPredicateExecutor"]

    L2B_SVC["🔍 LEVEL 2 — Components: university · Service\nUniversityService: CRUD façade\nDynamicQueryService: Spec vs QueryDSL vs QBE\nCourseFilter: Optional-based DTO"]

    L2B_TEST["🔍 LEVEL 2 — Components: university · Tests\nSimpleDBCrudTest · FindByOneAttribute\nFindByClausesAndExpressions · PagingTest\nCriteriaQueryTest · UniversityFactory"]

    L0 --> L1A & L1B
    L1A --> L2A
    L1B --> L2B_DOM & L2B_REPO & L2B_SVC & L2B_TEST
```

### Cross-Level Traceability Table

| Level 0 Element | Level 1 Container | Level 2 Component | Source File(s) |
|---|---|---|---|
| Spring Data 3 Demo Platform | `college` | `CollegeApplication` | `CollegeApplication.java` |
| Spring Data 3 Demo Platform | `university` | `UniversityApplication` | `UniversityApplication.java` |
| MongoDB | `college` → MongoDB | `StaffRepo`, `DepartmentRepo` (ReactiveCrudRepository) | `repo/StaffRepo.java`, `repo/DepartmentRepo.java` |
| MongoDB | `college` → MongoDB | `Staff @Document`, `Department @Document`, `Person` value object | `domain/Staff.java`, `domain/Department.java`, `domain/Person.java` |
| PostgreSQL | `university` → PostgreSQL | `StaffRepo`, `StudentRepo`, `DepartmentRepo`, `CourseRepo`, `CourseQueryDslRepo` | `repo/*.java` |
| PostgreSQL | `university` → PostgreSQL | `Course @Entity`, `Department @Entity`, `Staff @Entity`, `Student @Entity` | `domain/*.java` |
| H2 In-Memory | `university` (test) | All 5 test classes + `UniversityFactory` | `test/dao/*.java` |
| OpenAPI / Swagger UI | `college` Swagger | WebFlux UI via springdoc | `pom.xml` + auto-config |
| OpenAPI / Swagger UI + HAL | `university` Swagger + REST | Spring Data REST `@RepositoryRestResource` on `StaffRepo` | `repo/StaffRepo.java` |
| Docker / Compose | `college` → `compose.yaml` | Docker Compose file starts MongoDB | `college/compose.yaml` |
| Docker / Compose | `university` → `docker-compose.yml` | Docker Compose file starts PostgreSQL; schema from `postgres/schema.sql` | `university/docker-compose.yml` |
| GitHub | All | 42 course branches (`xx_xxe` naming = chapter_lesson_end) | `.git/` |

---

## Architecture Decision Records (Key Choices)

| Decision | Choice | Rationale |
|---|---|---|
| **Two separate modules** | `college` (Reactive) vs `university` (Blocking) | Explicitly contrasts reactive vs. blocking paradigms for educational clarity |
| **ReactiveCrudRepository** in college | Non-blocking `Flux`/`Mono` return types | Demonstrates Project Reactor + MongoDB reactive driver integration |
| **JpaSpecificationExecutor** in university | Inline lambda `Predicate` building | Type-safe dynamic queries without a separate DSL dependency |
| **QueryDSL with APT codegen** | Generated `Q`-types (`QCourse`, `QStudent`, etc.) | Compile-time safe dynamic query composition via `BooleanBuilder` |
| **Query by Example (QBE)** | `Example.of(entity)` | Simplest dynamic query approach — illustrates tradeoff vs. Specification/QueryDSL |
| **Spring Data REST** | `@RepositoryRestResource` on `StaffRepo` | Zero-code HAL-compliant REST endpoint exposure |
| **H2 for tests** | `scope=test` in `pom.xml` | Eliminates Docker dependency in test pipeline; fast in-memory execution |
| **Java 21** | LTS release | Enables future use of Records, Virtual Threads, Sealed Classes |

---

> **See also:**
> - [ARCHITECTURE_DIAGRAM.md](./ARCHITECTURE_DIAGRAM.md) — Full Level 2 component diagrams with all layers  
> - [REPOSITORY_SUMMARY.md](./REPOSITORY_SUMMARY.md) — Tabular summary with architectural observations

*Generated on 2026-03-03 by GitHub Copilot — [LinkedInLearning/spring-data-3-5959699](https://github.com/LinkedInLearning/spring-data-3-5959699)*
