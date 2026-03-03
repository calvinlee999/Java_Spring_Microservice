# Overall Architecture Diagram — `spring-data-3-5959699`

> Visual overview of both modules, all layers, relationships, query strategies, and infrastructure.  
> Source: [LinkedInLearning/spring-data-3-5959699](https://github.com/LinkedInLearning/spring-data-3-5959699)

---

```mermaid
graph TB
    subgraph REPO["📦 spring-data-3-5959699 (LinkedIn Learning)"]

        subgraph COLLEGE["🍃 college module — Reactive / NoSQL"]
            direction TB
            CA["CollegeApplication\n@SpringBootApplication"]

            subgraph COL_DOMAIN["Domain Layer"]
                CP["Person\n(value object)"]
                CS["Staff\n@Document"]
                CD["Department\n@Document"]
                CP -.embedded.-> CS
                CP -.embedded.-> CD
                CS -.embedded.-> CD
            end

            subgraph COL_REPO["Repository Layer · ReactiveCrudRepository"]
                SR["StaffRepo\nfindByMemberLastName()"]
                DR["DepartmentRepo\nfindByName() · findByChairId()\n@Query JSON regex"]
            end

            subgraph COL_WEB["Web Layer"]
                WF["WebFlux\nspring-boot-starter-webflux"]
                OA1["OpenAPI / Swagger UI\nspringdoc-webflux 2.5.0"]
            end

            MONGO[("MongoDB\nDocker Compose")]

            CA --> COL_DOMAIN
            CA --> COL_REPO
            CA --> COL_WEB
            COL_REPO -- "Flux / Mono" --> MONGO
        end

        subgraph UNIVERSITY["🏛️ university module — Blocking / Relational"]
            direction TB
            UA["UniversityApplication\n@SpringBootApplication"]

            subgraph UNI_DOMAIN["Domain Layer"]
                UP["Person\n(embedded value object)"]
                US["Staff\n@Entity"]
                UST["Student\n@Entity"]
                UD["Department\n@Entity"]
                UC["Course\n@Entity · @ManyToMany prerequisites"]
                SHC["ShowChair\n(Projection interface)"]
                UP -.@Embedded.-> US
                UP -.@Embedded.-> UST
                US -.@ManyToOne.-> UD
                US -.@ManyToOne.-> UC
                UD -.@ManyToOne.-> UC
                UC -.@ManyToMany self.-> UC
            end

            subgraph UNI_REPO["Repository Layer"]
                STAFF_R["StaffRepo\nJpaRepository\n@RepositoryRestResource\nJPQL @Query"]
                STU_R["StudentRepo\nJpaRepository\nDerived · JPQL · NativeSQL"]
                DEPT_R["DepartmentRepo\nJpaRepository\nProjection: ShowChair"]
                CRS_R["CourseRepo\nJpaRepository + JpaSpecificationExecutor\nDerived · Specification"]
                QDS_R["CourseQueryDslRepo\nJpaRepository + QuerydslPredicateExecutor\nQ-types via APT codegen"]
            end

            subgraph UNI_BIZ["Business / Service Layer"]
                USVC["UniversityService\n@Service · CRUD façade"]
                DQS["DynamicQueryService\n@Service\nSpecification vs QueryDSL vs QBE"]
                CF["CourseFilter\nOptional-based filter DTO"]
                CF --> DQS
            end

            subgraph COL_WEB2["Web / API Layer"]
                REST["Spring Data REST\nHAL auto-exposure"]
                OA2["OpenAPI / Swagger UI\nspringdoc-webmvc 2.5.0"]
            end

            subgraph UNI_TEST["Test Layer · @SpringBootTest"]
                T1["SimpleDBCrudTest"]
                T2["FindByOneAttribute"]
                T3["FindByClausesAndExpressions"]
                T4["PagingTest · Pageable · Sort"]
                T5["CriteriaQueryTest\nSpec · QueryDSL · QBE"]
                TF["UniversityFactory\n(shared test fixture)"]
                TF --> T1 & T2 & T3 & T4 & T5
            end

            PG[("PostgreSQL\ndocker-compose.yml")]
            H2[("H2 In-Memory\ntest scope")]

            UA --> UNI_DOMAIN & UNI_REPO & UNI_BIZ & COL_WEB2
            USVC --> STAFF_R & STU_R & DEPT_R & CRS_R
            DQS --> CRS_R & QDS_R
            REST --> STAFF_R & DEPT_R
            UNI_REPO -- "JPA / Hibernate" --> PG
            UNI_TEST -- "JPA / Hibernate" --> H2
        end

    end
```

---

## Layer Legend

| Layer | college | university |
|---|---|---|
| **Entry Point** | `CollegeApplication` | `UniversityApplication` |
| **Domain** | `@Document` (MongoDB) | `@Entity` (JPA/Jakarta) |
| **Repository** | `ReactiveCrudRepository` | `JpaRepository` + Specification + QueryDSL |
| **Service** | — | `UniversityService`, `DynamicQueryService` |
| **Web/API** | WebFlux + OpenAPI | Spring Data REST + Spring MVC + OpenAPI |
| **Database (runtime)** | MongoDB via Docker Compose | PostgreSQL via Docker Compose |
| **Database (test)** | — | H2 in-memory |
| **Tests** | None | 5 test classes + factory |

## Query Strategy Flow

```mermaid
flowchart LR
    Q["Incoming Query Need"]
    Q -->|"single attribute\nknown at compile time"| A["Derived Method\ne.g. findByLastName()"]
    Q -->|"complex join or\nformatting needed"| B["JPQL @Query"]
    Q -->|"DB-specific SQL\nor raw performance"| C["Native @Query\nnativeQuery=true"]
    Q -->|"dynamic filter\nruntime predicates"| D{"Strategy Choice"}
    D --> E["JPA Specification\nJpaSpecificationExecutor\nlambda Predicate"]
    D --> F["QueryDSL\nQuerydslPredicateExecutor\nBooleanBuilder + Q-types"]
    D --> G["Query by Example\nExample.of(entity)"]
    Q -->|"MongoDB\npattern match"| H["@Query JSON\n{ field: { $regex: ?0 } }"]
```

---

*Generated on 2026-03-03 by GitHub Copilot — architectural analysis of [LinkedInLearning/spring-data-3-5959699](https://github.com/LinkedInLearning/spring-data-3-5959699)*
