# Sequence Diagrams — `spring-data-3-5959699`

> End-to-end request flows for both modules, from HTTP Client through all application layers down to the database.

---

## Diagram Index

| Diagram | Module | Server | Database | Pattern |
|---|---|---|---|---|
| [college Request Flow](#college-module--request-sequence) | `college` | Netty (WebFlux) | MongoDB :27017 | Reactive, non-blocking, `Flux`/`Mono` |
| [university Request Flow](#university-module--request-sequence) | `university` | Tomcat (Spring MVC) | PostgreSQL :5432 | Blocking, thread-per-request, JPA |

---

## `college` Module — Request Sequence

> **Stack:** `CollegeApplication` → Netty Event Loop → WebFlux Router → `ReactiveCrudRepository` → Spring Data MongoDB Reactive Driver → **MongoDB**  
> **Key characteristic:** No thread is ever blocked during I/O. The Netty event loop thread stays free while MongoDB responds.

```mermaid
sequenceDiagram
    autonumber
    actor Client as HTTP Client
    participant Netty as Netty Event Loop (WebFlux)
    participant Router as WebFlux Router / REST Handler
    participant SwaggerUI as Swagger UI (springdoc-webflux 2.5.0)
    participant StaffRepo as StaffRepo (ReactiveCrudRepository)
    participant DeptRepo as DepartmentRepo (ReactiveCrudRepository)
    participant ReactiveDriver as Spring Data MongoDB Reactive Driver
    participant MongoDB as MongoDB (Docker Compose :27017)

    Note over Client,MongoDB: college module — Reactive Non-Blocking Pipeline

    Client->>+Netty: HTTP GET /swagger-ui.html
    Netty->>+SwaggerUI: serve OpenAPI spec
    SwaggerUI-->>-Netty: HTML/JS assets
    Netty-->>-Client: 200 OK (API docs rendered)

    Client->>+Netty: HTTP GET /staff/{id}
    Netty->>+Router: route to Staff handler
    Router->>+StaffRepo: findById(id) returns Mono of Staff
    StaffRepo->>+ReactiveDriver: reactive find query
    ReactiveDriver->>+MongoDB: _id ObjectId lookup
    MongoDB-->>-ReactiveDriver: BSON document
    ReactiveDriver-->>-StaffRepo: Mono of Staff (subscribed)
    StaffRepo-->>-Router: Mono of Staff
    Router-->>-Netty: Mono of Staff (assembled)
    Netty-->>-Client: 200 OK JSON { id, member }
    Note right of Netty: No thread blocked during I/O wait

    Client->>+Netty: HTTP GET /department?namePattern=.*Science.*
    Netty->>+Router: route to Department handler
    Router->>+DeptRepo: findNameByPattern(pattern) returns Flux of Department
    DeptRepo->>+ReactiveDriver: reactive regex query
    ReactiveDriver->>+MongoDB: name $regex query
    MongoDB-->>-ReactiveDriver: BSON cursor (streaming)
    ReactiveDriver-->>-DeptRepo: Flux of Department (hot stream)
    DeptRepo-->>-Router: Flux of Department
    Router-->>-Netty: Server-Sent Events stream
    Netty-->>-Client: 200 OK (streaming JSON array)
    Note right of MongoDB: Flux streams each document as emitted - backpressure applied

    Client->>+Netty: HTTP POST /staff (body: firstName, lastName)
    Netty->>+Router: route to Staff create handler
    Router->>+StaffRepo: save(new Staff(person)) returns Mono of Staff
    StaffRepo->>+ReactiveDriver: reactive insert
    ReactiveDriver->>+MongoDB: insertOne member document
    MongoDB-->>-ReactiveDriver: inserted ObjectId
    ReactiveDriver-->>-StaffRepo: Mono of Staff (with generated id)
    StaffRepo-->>-Router: Mono of Staff
    Router-->>-Netty: Mono of Staff
    Netty-->>-Client: 201 Created { id, member }
```

### college — Layer Call Chain

```
HTTP Client
    │ HTTP request
    ▼
Netty Event Loop  (spring-boot-starter-webflux → auto-configures Netty)
    │ dispatches via WebFlux dispatcher
    ▼
WebFlux Router / REST Handler
    │ calls repository method returning Mono<T> or Flux<T>
    ▼
StaffRepo / DepartmentRepo  (ReactiveCrudRepository<T, String>)
    │ Spring Data generates reactive query execution
    ▼
Spring Data MongoDB Reactive Driver  (spring-boot-starter-data-mongodb-reactive)
    │ non-blocking TCP to MongoDB
    ▼
MongoDB  (Docker Compose :27017)
    │ BSON response streams back
    ▼
Reactive pipeline assembles result → subscriber (Netty) emits HTTP response
```

---

## `university` Module — Request Sequence

> **Stack:** `UniversityApplication` → Tomcat thread pool → Spring Data REST HAL / Service Layer → Repository Layer → JPA / Hibernate → **PostgreSQL**  
> **Key characteristic:** Each request occupies a Tomcat thread for its full duration. Adding `spring.threads.virtual.enabled=true` (Spring Boot 3.2+) eliminates this bottleneck without any code changes.

```mermaid
sequenceDiagram
    autonumber
    actor Client as HTTP Client
    participant Tomcat as Tomcat Thread Pool (Spring MVC)
    participant DataREST as Spring Data REST HAL Controller
    participant SwaggerUI as Swagger UI (springdoc-webmvc 2.5.0)
    participant UniSvc as UniversityService @Service
    participant DynSvc as DynamicQueryService @Service
    participant StaffRepo as StaffRepo (JpaRepository + @RepositoryRestResource)
    participant StudentRepo as StudentRepo (JpaRepository)
    participant CourseRepo as CourseRepo (JpaRepository + JpaSpecificationExecutor)
    participant QDSLRepo as CourseQueryDslRepo (QuerydslPredicateExecutor)
    participant Hibernate as Hibernate ORM (JPA Provider)
    participant PG as PostgreSQL (Docker Compose :5432)

    Note over Client,PG: university module — Blocking Request/Response (Spring MVC)

    Client->>+Tomcat: HTTP GET /swagger-ui.html
    Tomcat->>+SwaggerUI: serve OpenAPI spec
    SwaggerUI-->>-Tomcat: HTML/JS assets
    Tomcat-->>-Client: 200 OK (API docs rendered)

    Client->>+Tomcat: HTTP GET /staff (HAL)
    Tomcat->>+DataREST: dispatch to StaffRepo HAL handler
    DataREST->>+StaffRepo: findAll() via @RepositoryRestResource
    StaffRepo->>+Hibernate: SELECT s FROM Staff s (JPQL)
    Hibernate->>+PG: SELECT * FROM staff JOIN person
    PG-->>-Hibernate: ResultSet rows
    Hibernate-->>-StaffRepo: List of Staff
    StaffRepo-->>-DataREST: List of Staff
    DataREST-->>-Tomcat: HAL+JSON _embedded _links
    Tomcat-->>-Client: 200 OK HAL response
    Note right of Tomcat: Tomcat thread blocked during full DB round-trip. Add spring.threads.virtual.enabled=true to free the OS thread.

    Client->>+Tomcat: HTTP GET /staff?lastName=Smith
    Tomcat->>+DataREST: dispatch to StaffRepo handler
    DataREST->>+StaffRepo: findByLastName("Smith")
    Note right of StaffRepo: @Query JPQL - SELECT s FROM Staff s WHERE s.member.lastName = :lastName
    StaffRepo->>+Hibernate: execute JPQL query
    Hibernate->>+PG: SELECT s.* FROM staff s WHERE s.last_name = 'Smith'
    PG-->>-Hibernate: ResultSet
    Hibernate-->>-StaffRepo: List of Staff
    StaffRepo-->>-DataREST: List of Staff
    DataREST-->>-Tomcat: JSON array
    Tomcat-->>-Client: 200 OK

    Client->>+Tomcat: POST /courses/filter (credits=3, dept=CS)
    Tomcat->>+DynSvc: filterBySpecification(CourseFilter)
    Note right of DynSvc: Strategy 1 - JPA Specification lambda Predicate list
    DynSvc->>+CourseRepo: findAll(Specification of Course)
    CourseRepo->>+Hibernate: Criteria API dynamic WHERE clause
    Hibernate->>+PG: SELECT c.* FROM course c WHERE c.credits=3 AND c.dept_id=?
    PG-->>-Hibernate: ResultSet
    Hibernate-->>-CourseRepo: List of Course
    CourseRepo-->>-DynSvc: List of Course
    DynSvc-->>-Tomcat: List of Course
    Tomcat-->>-Client: 200 OK

    Client->>+Tomcat: POST /courses/filter/querydsl (credits=3)
    Tomcat->>+DynSvc: filterByQueryDsl(CourseFilter)
    Note right of DynSvc: Strategy 2 - QueryDSL BooleanBuilder + QCourse APT codegen
    DynSvc->>+QDSLRepo: findAll(BooleanBuilder predicate)
    QDSLRepo->>+Hibernate: QueryDSL HQL to SQL translation
    Hibernate->>+PG: SELECT c.* FROM course c WHERE c.credits = 3
    PG-->>-Hibernate: ResultSet
    Hibernate-->>-QDSLRepo: Iterable of Course
    QDSLRepo-->>-DynSvc: List of Course
    DynSvc-->>-Tomcat: List of Course
    Tomcat-->>-Client: 200 OK

    Client->>+Tomcat: POST /courses/filter/example (credits=3)
    Tomcat->>+DynSvc: filterByExample(CourseFilter)
    Note right of DynSvc: Strategy 3 - Query by Example - Example.of(Course probe)
    DynSvc->>+CourseRepo: findAll(Example of Course)
    CourseRepo->>+Hibernate: probe entity - ignore null fields
    Hibernate->>+PG: SELECT c.* FROM course c WHERE c.credits = 3
    PG-->>-Hibernate: ResultSet
    Hibernate-->>-CourseRepo: List of Course
    CourseRepo-->>-DynSvc: List of Course
    DynSvc-->>-Tomcat: List of Course
    Tomcat-->>-Client: 200 OK

    Client->>+Tomcat: GET /students/oldest
    Tomcat->>+UniSvc: findOldest() via service
    UniSvc->>+StudentRepo: findOldest()
    Note right of StudentRepo: @Query nativeQuery=true - SELECT * FROM student ORDER BY age DESC LIMIT 1
    StudentRepo->>+Hibernate: native SQL passthrough
    Hibernate->>+PG: SELECT * FROM student ORDER BY age DESC LIMIT 1
    PG-->>-Hibernate: single row
    Hibernate-->>-StudentRepo: Optional of Student
    StudentRepo-->>-UniSvc: Optional of Student
    UniSvc-->>-Tomcat: Student
    Tomcat-->>-Client: 200 OK { id, attendee, age, fullTime }
```

### university — Layer Call Chain

```
HTTP Client
    │ HTTP request (blocking thread assigned by Tomcat)
    ▼
Tomcat Thread Pool  (spring-boot-starter-web → auto-configures Tomcat)
    │ DispatcherServlet routes to handler
    ▼
Spring Data REST HAL Controller  (@RepositoryRestResource on StaffRepo)
  OR
Service Layer  (UniversityService / DynamicQueryService)
    │ calls repository method returning List<T> / Optional<T>
    ▼
Repository Layer  (StaffRepo / StudentRepo / CourseRepo / CourseQueryDslRepo)
    │ Spring Data generates query (Derived / JPQL / Native / Spec / QueryDSL / QBE)
    ▼
Hibernate ORM  (JPA Provider — translates JPQL/HQL/Criteria → SQL)
    │ JDBC connection from HikariCP pool
    ▼
PostgreSQL  (Docker Compose :5432)
    │ SQL result rows returned
    ▼
Hibernate maps rows → JPA entities → returned up the call stack → HTTP response
```

---

## Side-by-Side Comparison

| Step | `college` (Reactive) | `university` (Blocking) |
|---|---|---|
| **HTTP Server** | Netty event loop | Tomcat thread pool |
| **Thread during I/O** | Event loop thread freed immediately | Tomcat thread blocked until DB responds |
| **Return type** | `Mono<T>` / `Flux<T>` | `T` / `List<T>` / `Optional<T>` |
| **Data access layer** | `ReactiveCrudRepository` | `JpaRepository` + extensions |
| **Query translation** | Spring Data → MongoDB BSON query | Spring Data → JPQL/SQL via Hibernate |
| **ORM / Driver** | Spring Data MongoDB Reactive Driver | Hibernate ORM + JDBC (HikariCP) |
| **Database wire protocol** | MongoDB Binary Protocol (async) | PostgreSQL wire protocol (sync JDBC) |
| **Backpressure** | Built-in via `Flux` publisher/subscriber | Not applicable (synchronous) |
| **Streaming response** | Yes — `Flux` maps to SSE / chunked response | No — full result loaded into memory first |
| **Concurrency upgrade path** | Already maximally concurrent | Add `spring.threads.virtual.enabled=true` |

---

> **See also:**
> - [WEBFLUX_VS_VIRTUAL_THREADS.md](./WEBFLUX_VS_VIRTUAL_THREADS.md) — Deep dive: WebFlux vs Virtual Threads concurrency models
> - [ARCHITECTURE_C4_LEVELS.md](./ARCHITECTURE_C4_LEVELS.md) — C4 Level 0/1/2 diagrams
> - [ARCHITECTURE_DIAGRAM.md](./ARCHITECTURE_DIAGRAM.md) — Full component-level diagram
> - [REPOSITORY_SUMMARY.md](./REPOSITORY_SUMMARY.md) — Module overview and query strategy matrix

*Generated on 2026-03-03 by GitHub Copilot — [LinkedInLearning/spring-data-3-5959699](https://github.com/LinkedInLearning/spring-data-3-5959699)*
