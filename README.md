# Java Spring Microservice — University Modern

> **A production-grade reference implementation demonstrating Java 8 through Java 21 language features,**  
> **Spring Boot 3.2+, multi-strategy data access, and cloud-native deployment patterns.**  
> _Crafted from the perspective of a principal fintech enterprise architect — explained so anyone can follow along._

---

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://adoptium.net/temurin/releases/?version=21)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Maven%203.9-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

</div>

---

## Why This Repository Exists

> **Think of this as the "flight simulator" for enterprise Java engineering.**

In the fintech industry — payments, trading platforms, digital banking, lending systems — Java Spring Boot powers the backbone of hundreds of mission-critical services.  
Every feature in this repository mirrors a real-world production pattern: how a Bank's Course Catalog service, a Portfolio Management API, or a Customer Onboarding microservice is actually architected.

This repo answers five career-defining questions:

| Question | Where to Look |
|---|---|
| _How is a production Java microservice actually structured?_ | [Architecture Guide — 5-Layer Model](#-the-five-layer-architecture) |
| _Which Java version brought which feature and why do I care?_ | [JAVA_FEATURES.md](JAVA_FEATURES.md) |
| _How do HTTP requests travel through the entire stack?_ | [SEQUENCE_DIAGRAMS.md](SEQUENCE_DIAGRAMS.md) |
| _How do I run this on my laptop in under 10 minutes?_ | [GETTING_STARTED.md](GETTING_STARTED.md) |
| _How does each architectural layer fit the C4 model?_ | [Spring_Data/ARCHITECTURE_C4_LEVELS.md](Spring_Data/ARCHITECTURE_C4_LEVELS.md) |

---

## Table of Contents

1. [The Big Picture — What We Built](#-the-big-picture--what-we-built)
2. [The Five-Layer Architecture](#-the-five-layer-architecture)
3. [Project Structure — Every File Explained](#-project-structure--every-file-explained)
4. [Domain Model — The "Things" the App Knows About](#-domain-model--the-things-the-app-knows-about)
5. [Java Language Evolution — Feature by Version](#-java-language-evolution--feature-by-version)
6. [API Endpoints Reference](#-api-endpoints-reference)
7. [Dynamic Query Strategies](#-dynamic-query-strategies)
8. [Technology Stack](#-technology-stack)
9. [Quick Start — Zero to Running in 5 Steps](#-quick-start--zero-to-running-in-5-steps)
10. [Testing Strategy — The Four-Layer Pyramid](#-testing-strategy--the-four-layer-pyramid)
11. [Cloud-Native & Production Readiness](#-cloud-native--production-readiness)
12. [Principal Architect's Self-Reinforcement Guide](#-principal-architects-self-reinforcement-guide)
13. [Evaluation Scorecard](#-evaluation-scorecard)
14. [Further Reading](#-further-reading)

---

## 🏗 The Big Picture — What We Built

Imagine a university registrar's office — but instead of paper folders and filing cabinets, every piece of information is stored in a database and accessible over the internet via a REST API.

```
Who calls us?        What can they do?               What data do we manage?
─────────────        ─────────────────               ───────────────────────
Browser / Postman ──▶ List all courses               📚 Courses  (name, credits, prereqs)
curl / mobile app ──▶ Filter courses by credits      👩‍🏫 Staff    (professors, admins)
Other services    ──▶ Find courses by department      🏛  Departments (Humanities, Sciences)
Swagger UI        ──▶ Get course by ID               👩‍🎓 Students (enrollment status)
                     ▶ Search dynamically (3 ways)
                     ▶ Query an external library API
```

This is a **microservice** — one small, focused program that does one domain well (University Course Catalog), exposes clean HTTP APIs, runs in Docker, reports its own health to Kubernetes, and connects to a relational database.

> **8th-grade analogy:** It's like the school's online course book — except instead of looking it up in a PDF, a computer program serves it over the internet, and another program can ask questions like "Give me all 3-credit Science courses" and instantly get a list back.

---

## 🏛 The Five-Layer Architecture

Every professional Java microservice follows a layered pattern. Think of it like a five-floor skyscraper where each floor has exactly one job and talks only to the floor immediately above or below.

```
┌─────────────────────────────────────────────────────────┐
│  Floor 5 — WEB LAYER                                    │
│  CourseController · GlobalExceptionHandler              │
│  ➜ Takes HTTP requests in, sends HTTP responses out     │
│  ➜ Zero business logic lives here                       │
├─────────────────────────────────────────────────────────┤
│  Floor 4 — BUSINESS / SERVICE LAYER                     │
│  UniversityService · DynamicQueryService · CourseFilter │
│  ➜ All the "what does the app actually do?" code        │
│  ➜ Coordinates between multiple repositories           │
├─────────────────────────────────────────────────────────┤
│  Floor 3 — REPOSITORY LAYER                             │
│  CourseRepo · StudentRepo · StaffRepo · DepartmentRepo  │
│  ➜ Knows how to read/write data from the database       │
│  ➜ Spring Data generates 95% of the code automatically  │
├─────────────────────────────────────────────────────────┤
│  Floor 2 — DOMAIN LAYER                                 │
│  Course · Student · Staff · Department · Person         │
│  ➜ The "nouns" — the things the app understands         │
│  ➜ Mapped to database tables by Hibernate               │
├─────────────────────────────────────────────────────────┤
│  Floor 1 — DATABASE                                     │
│  PostgreSQL (Docker) · H2 In-Memory (tests)             │
│  ➜ The actual storage — rows and columns                │
└─────────────────────────────────────────────────────────┘
```

> **Why this matters in fintech:** A Payment Service, Fraud Detection Service, and Account Balance Service all follow this exact pattern. Learning one means you can read any of them.

Full architecture reference including Mermaid diagrams: [ARCHITECTURE.md](ARCHITECTURE.md)

---

## 📁 Project Structure — Every File Explained

```
Java_Spring_Microservice/
│
├── README.md                          ◀ You are here
├── ARCHITECTURE.md                    ◀ Deep-dive: every layer, Mermaid diagrams, design decisions
├── GETTING_STARTED.md                 ◀ Step-by-step setup (works for 8th graders AND engineers)
├── JAVA_FEATURES.md                   ◀ Java 8→21 features with real code samples from this project
├── SEQUENCE_DIAGRAMS.md               ◀ How HTTP requests travel through every layer, step by step
│
├── Spring_Data/                       ◀ Companion reference documents
│   ├── ARCHITECTURE_C4_LEVELS.md      ◀ C4 model: Context → Container → Component → Code diagrams
│   ├── ARCHITECTURE_DIAGRAM.md        ◀ Architecture overview with Mermaid diagrams
│   ├── REPOSITORY_SUMMARY.md          ◀ All query strategies compared side by side
│   ├── SEQUENCE_DIAGRAMS.md           ◀ Spring Data-specific flow diagrams
│   └── WEBFLUX_VS_VIRTUAL_THREADS.md  ◀ Concurrency comparison: reactive vs virtual threads
│
└── university-modern/                 ◀ THE APPLICATION — all source code lives here
    │
    ├── pom.xml                        ◀ "Ingredients list" — all libraries and their versions
    ├── Dockerfile                     ◀ Multi-stage recipe to package the app as a Docker image
    ├── compose.yaml                   ◀ Runs the whole stack (app + database) with one command
    │
    ├── postgres/
    │   └── schema.sql                 ◀ SQL that creates all tables on first startup
    │
    └── src/
        ├── main/
        │   ├── java/com/example/university/
        │   │   │
        │   │   ├── UniversityApplication.java        ◀ The "power button" — starts everything
        │   │   │
        │   │   ├── web/                              ◀ FLOOR 5: HTTP boundary
        │   │   │   ├── CourseController.java          ◀ All /api/courses endpoints (10 routes)
        │   │   │   ├── GlobalExceptionHandler.java    ◀ Turns exceptions → structured JSON errors
        │   │   │   ├── CourseNotFoundException.java   ◀ Custom error: "course ID 99 not found"
        │   │   │   └── CourseSearchRequest.java       ◀ Java 17 Record DTO for search parameters
        │   │   │
        │   │   ├── business/                         ◀ FLOOR 4: All the logic
        │   │   │   ├── UniversityService.java         ◀ CRUD operations + Java 21 features demo
        │   │   │   ├── DynamicQueryService.java       ◀ 3 query strategies: Spec / QueryDSL / QBE
        │   │   │   └── CourseFilter.java              ◀ Optional-based filter DTO (safe nullability)
        │   │   │
        │   │   ├── repo/                             ◀ FLOOR 3: Database access
        │   │   │   ├── CourseRepo.java                ◀ JPA + Specification + @RepositoryRestResource
        │   │   │   ├── CourseQueryDslRepo.java         ◀ QueryDSL predicate executor
        │   │   │   ├── DepartmentRepo.java             ◀ Department CRUD + HAL auto-endpoint
        │   │   │   ├── StaffRepo.java                  ◀ Staff CRUD + JPQL custom query
        │   │   │   └── StudentRepo.java                ◀ Student CRUD + native SQL + derived methods
        │   │   │
        │   │   ├── domain/                           ◀ FLOOR 2: "The nouns"
        │   │   │   ├── Course.java                    ◀ @Entity: name, credits, prereqs, instructor
        │   │   │   ├── Department.java                ◀ @Entity: name, chair (Staff)
        │   │   │   ├── Staff.java                     ◀ @Entity: embeds Person record
        │   │   │   ├── Student.java                   ◀ @Entity: embeds Person, age, fullTime, status
        │   │   │   ├── Person.java                    ◀ Java 17 Record + @Embeddable (firstName, lastName)
        │   │   │   ├── EnrollmentStatus.java          ◀ Java 17 Sealed Interface: Active/Graduated/Suspended
        │   │   │   └── ShowChair.java                 ◀ Spring Data Projection: ?projection=showChair
        │   │   │
        │   │   └── config/
        │   │       └── AppConfig.java                 ◀ Bean wiring and app-level configuration
        │   │
        │   └── resources/
        │       └── application.properties             ◀ Runtime settings (DB URL, virtual threads, etc.)
        │
        └── test/
            ├── java/com/example/university/
            │   ├── dao/
            │   │   ├── ModernFeaturesTest.java         ◀ Tests every Java 17/21 feature with H2
            │   │   └── SimpleDBCrudTest.java           ◀ Tests basic save / find / update / delete
            │   └── integration/
            │       └── RealPostgresIT.java             ◀ Integration test using a REAL PostgreSQL container
            │
            └── resources/
                ├── application.properties              ◀ Test config: H2 in-memory, schema auto-create
                └── data.sql                            ◀ Seed data loaded before every test
```

---

## 🗄 Domain Model — The "Things" the App Knows About

Here is how the five core entities relate to each other:

```
STAFF_MEMBER ──chairs──▶ DEPARTMENT ──offers──▶ COURSE
     │                                              │
     └──────────instructs──────────────────────────▶│
                                                    │
STUDENT ──────────────────────────enrolled in──────▶│
                                                    │
COURSE ◀──────requires (prerequisites)─────────────┘
```

| Entity | Java Type | Key Design Decision | Fintech Parallel |
|---|---|---|---|
| `Person` | `record` (Java 17) | `@Embeddable` — not its own table; columns live in parent table | Customer record embedded in Account entity |
| `EnrollmentStatus` | `sealed interface` + nested `record`s | Compiler-enforced closed set of states | `TransactionStatus`: Pending / Settled / Reversed |
| `Course` | `@Entity` | `@ManyToMany` self-referential prerequisites | Financial product with prerequisite eligibility rules |
| `ShowChair` | Projection interface | Lightweight read-only view — never loads full entity graph | Summary DTO in a reporting API |

### EnrollmentStatus — Java 17 Sealed Interface (The Fintech Pattern)

> In fintech, every domain object has states (a transaction is `PENDING`, `SETTLED`, or `REVERSED`; a trade is `NEW`, `FILLED`, or `CANCELLED`). The **Sealed Interface** pattern ensures the compiler will alert you if you ever forget to handle a new state.

```java
// Every possible state is declared right here — no surprises
public sealed interface EnrollmentStatus
        permits EnrollmentStatus.Active,
                EnrollmentStatus.Graduated,
                EnrollmentStatus.Suspended {

    record Active(String semester)      implements EnrollmentStatus {} // ← attending classes
    record Graduated(int graduationYear) implements EnrollmentStatus {} // ← mission complete
    record Suspended(String reason)     implements EnrollmentStatus {} // ← paused
}

// Java 21 Pattern-Matching Switch — the compiler FORCES you to handle every case
String describe(EnrollmentStatus status) {
    return switch (status) {
        case Active a      -> "Active in semester " + a.semester();
        case Graduated g   -> "Graduated in " + g.graduationYear();
        case Suspended s   -> "Suspended: " + s.reason();
        // No default needed — compiler verified all cases are covered ✅
    };
}
```

---

## ☕ Java Language Evolution — Feature by Version

> Each Java release solved a real painful problem. Here's the story of how the language grew up — and where you'll find each feature **live in this project**.

```
Java 8  (2014) ──── Lambdas · Streams · Optional · Functional Interfaces
    │               ➜ DynamicQueryService.java (Specification lambdas)
    │               ➜ CourseFilter.java (Optional fields)
    │
Java 11 (2018) ──── var · String.strip/isBlank · HttpClient
    │               ➜ DynamicQueryService.java (var parts = new ArrayList<>())
    │
Java 17 (2021) ──── Records · Sealed Interfaces · Text Blocks · Pattern Matching instanceof
    │               ➜ Person.java (Record @Embeddable)
    │               ➜ EnrollmentStatus.java (Sealed Interface + Records)
    │               ➜ CourseRepo.java (@Query with Text Block)
    │               ➜ UniversityService.java (instanceof pattern)
    │
Java 21 (2023) ──── Virtual Threads · SequencedCollection · Pattern Matching Switch
                    ➜ application.properties (spring.threads.virtual.enabled=true)
                    ➜ UniversityService.java (SequencedCollection, switch with records)
                    ➜ CourseController.java (reversed(), getFirst(), getLast())
```

### Feature Quick-Reference

| Feature | Version | Analogy (8th Grade) | Live Location |
|---|---|---|---|
| **Lambda** | Java 8 | A sticky note: "just do this one thing" | `DynamicQueryService.java` |
| **Stream** | Java 8 | A water pipe with filters attached | `UniversityService.java` |
| **Optional** | Java 8 | A gift box that might be empty | `CourseFilter.java` |
| **var** | Java 11 | "it" — the compiler figures out the type | `DynamicQueryService.java` |
| **Record** | Java 17 | A data form that fills itself in | `Person.java`, `CourseSearchRequest.java` |
| **Sealed Interface** | Java 17 | A closed club — only approved members allowed | `EnrollmentStatus.java` |
| **Text Block** | Java 17 | Copy-pasted text with proper indentation | `CourseRepo.java` (@Query) |
| **Virtual Threads** | Java 21 | 1,000 workers sharing 10 real desks | `application.properties` |
| **SequencedCollection** | Java 21 | A list where "first" and "last" are official concepts | `CourseController.java` |
| **Pattern Matching Switch** | Java 21 | A smart vending machine that knows what you put in | `UniversityService.java` |

Full feature guide with before/after code: [JAVA_FEATURES.md](JAVA_FEATURES.md)

---

## 📡 API Endpoints Reference

Base URL: `http://localhost:8080`  
Interactive docs: `http://localhost:8080/swagger-ui.html`  
HAL browser (Spring Data REST): `http://localhost:8080`

### `/api/courses` — Controller Endpoints

| Method | Path | What it does | Java/Spring Feature |
|---|---|---|---|
| `GET` | `/api/courses` | List all courses | Standard CRUD |
| `GET` | `/api/courses/{id}` | Get one course by ID; 404 if not found | `ProblemDetail` RFC-7807 |
| `GET` | `/api/courses/credits/{n}` | Find courses with exactly N credits | `@Query` JPQL Text Block |
| `GET` | `/api/courses/reversed` | Return courses in reverse order | Java 21 `List.reversed()` |
| `GET` | `/api/courses/first` | Get the first course | Java 21 `SequencedCollection.getFirst()` |
| `GET` | `/api/courses/last` | Get the last course | Java 21 `SequencedCollection.getLast()` |
| `GET` | `/api/courses/filter?credits=3` | Dynamic filter via JPA Specification | `JpaSpecificationExecutor` lambda |
| `GET` | `/api/courses/querydsl?credits=3` | Dynamic filter via QueryDSL | `BooleanBuilder` + `QCourse` APT types |
| `GET` | `/api/courses/qbe?credits=3` | Dynamic filter via Query by Example | `Example.of(probe)` |
| `GET` | `/api/courses/external/{topic}` | Proxy query to Open Library API | Spring Boot 3.2+ `RestClient` |

### Auto-Exposed HAL Endpoints (Spring Data REST — no controller code needed)

| Path | What it does |
|---|---|
| `/courses` | Full CRUD on Course with HAL+JSON |
| `/students` | Full CRUD on Student with HAL+JSON |
| `/staff` | Full CRUD on Staff with HAL+JSON |
| `/departments` | Full CRUD on Department with HAL+JSON |
| `/departments?projection=showChair` | Lightweight view via `ShowChair` projection |

### Health & Operations (Spring Boot Actuator)

| Path | Used for |
|---|---|
| `/actuator/health` | Is the app alive? (**Kubernetes liveness probe**) |
| `/actuator/health/readiness` | Is the DB connected? (**Kubernetes readiness probe**) |
| `/actuator/metrics` | Raw metrics for Prometheus + Grafana |
| `/actuator/info` | App version and build info |

---

## 🔍 Dynamic Query Strategies

One of the most valuable skills in enterprise Java is knowing **which query strategy to use when**. This project demonstrates all five, side by side on the same `Course` entity.

```
Should I query this at compile time or runtime?
       │
       ├── Compile time (field name is fixed)
       │       │
       │       ├── Simple single field ──────────▶ Derived Method   findByCredits(int)
       │       └── Complex JPQL / multi-join ────▶ @Query           @Query("SELECT c FROM...")
       │
       └── Runtime (fields/values chosen by the user)
               │
               ├── Type-safe, IDE auto-complete ──▶ QueryDSL         BooleanBuilder + QCourse
               ├── Flexible lambda predicates ───▶ JPA Specification  JpaSpecificationExecutor
               └── Simple probe object ──────────▶ Query by Example  Example.of(probe)
```

| Strategy | Code Location | Best For | Avoid When |
|---|---|---|---|
| **Derived Methods** | `CourseRepo.findByCredits()` | Simple, fixed criteria | More than 2 conditions (names get long) |
| **JPQL @Query** | `CourseRepo` with Text Block | Multi-join reads, sorted pages | Need portability across DB vendors |
| **JPA Specification** | `DynamicQueryService.filterBySpecification()` | Flexible, testable predicates | Team unfamiliar with Criteria API |
| **QueryDSL** | `DynamicQueryService.filterByQueryDsl()` | Type-safe, refactor-friendly | Small projects (APT codegen setup overhead) |
| **Query by Example** | `DynamicQueryService.filterByExample()` | Rapid prototyping | Null handling, range queries |

> **Fintech context:** A transaction search API in a payment platform typically uses **JPA Specification** or **QueryDSL** — a user may filter by date range, amount, merchant, currency, and status in any combination. Derived methods would require hundreds of permutations.

---

## 🛠 Technology Stack

| Category | Technology | Version | Why This Choice |
|---|---|---|---|
| **Language** | Java | 21 (LTS) | Virtual Threads, Pattern Matching, SequencedCollection |
| **Framework** | Spring Boot | 3.3.5 | Convention-over-configuration, production-ready defaults |
| **Web** | Spring MVC | (Boot-managed) | Battle-tested, thread-per-request, Tomcat embedded |
| **ORM** | Spring Data JPA / Hibernate 6 | (Boot-managed) | Relational data access, JPQL, criteria queries |
| **Type-safe Queries** | QueryDSL 5.0 | 5.0.0 | Compile-time query safety, IDE auto-complete |
| **Database (runtime)** | PostgreSQL | 16 | Production relational DB; full SQL standard support |
| **Database (test)** | H2 In-Memory | (Boot-managed) | Fast, zero-setup test isolation |
| **Containers (test)** | Testcontainers | 1.19.7 | Real PostgreSQL in CI/CD — eliminates H2 dialect drift |
| **API Docs** | SpringDoc OpenAPI / Swagger UI | 2.5.0 | Auto-generated, interactive, zero maintenance |
| **Monitoring** | Spring Boot Actuator | (Boot-managed) | Health, readiness, metrics endpoints for K8s + Prometheus |
| **Validation** | Jakarta Validation 3.0 | (Boot-managed) | Declarative input validation (`@NotBlank`, `@Min`) |
| **Containerization** | Docker + Docker Compose | 24+ / v2 | Reproducible local dev; prod-identical environments |
| **Build** | Apache Maven | 3.9+ | Dependency management, APT codegen, multi-stage builds |

---

## 🚀 Quick Start — Zero to Running in 5 Steps

> Full step-by-step guide with troubleshooting: [GETTING_STARTED.md](GETTING_STARTED.md)

### Prerequisites

```bash
java -version          # needs: Java 21+
mvn -version           # needs: Maven 3.8+
docker --version       # needs: Docker 24+
docker compose version # needs: Compose v2
```

> **macOS shortcut:** `brew install openjdk@21 maven && brew install --cask docker`

### Step 1 — Clone

```bash
git clone https://github.com/calvinlee999/Java_Spring_Microservice.git
cd Java_Spring_Microservice/university-modern
```

### Step 2 — Start the Database

```bash
docker compose up db -d
# ✅ PostgreSQL is running at localhost:5432 with schema auto-applied
```

### Step 3 — Run the Application

```bash
./mvnw spring-boot:run
# ✅ Look for: "Started UniversityApplication in 2.8 seconds"
```

### Step 4 — Verify

```bash
curl http://localhost:8080/api/courses
# ✅ JSON array of courses returned

curl http://localhost:8080/actuator/health
# ✅ {"status":"UP"}
```

### Step 5 — Explore

| URL | What you'll see |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interactive API explorer with all 10 endpoints |
| `http://localhost:8080/api/courses` | JSON list of all courses |
| `http://localhost:8080/api/courses/filter?credits=3` | Filtered by 3 credits (JPA Specification) |
| `http://localhost:8080/api/courses/reversed` | Java 21 in action — reversed list |
| `http://localhost:8080/actuator/health` | Application health status |

### Run Everything in Docker (Production-Like)

```bash
docker compose up --build
# Builds the multi-stage Docker image and starts both app + db
```

---

## 🧪 Testing Strategy — The Four-Layer Pyramid

```
            ▲ LAYER 4 (Integration)
           /│\  RealPostgresIT.java
          / │ \ Real PostgreSQL via Testcontainers
         /  │  \ Eliminates H2 dialect differences
        ────┼────────────────────────────────
       /    │    \ LAYER 3 (Slice)
      /     │     \ Spring MVC Tests (@WebMvcTest)
     /──────┼──────\────────────────────────────
    /       │       \ LAYER 2 (Component)
   /        │        \ SimpleDBCrudTest.java + ModernFeaturesTest.java
  /         │         \ H2 in-memory — fast, no Docker required
 /──────────┼──────────\────────────────────────────────────────────
/           │           \ LAYER 1 (Unit)
             Base — Plain JUnit 5 unit tests
```

| Test File | Layer | Database | What It Tests |
|---|---|---|---|
| `SimpleDBCrudTest.java` | Component | H2 in-memory | Basic save, find, update, delete on all entities |
| `ModernFeaturesTest.java` | Component | H2 in-memory | Every Java 17/21 feature: Records, Sealed interfaces, Virtual Threads, SequencedCollection |
| `RealPostgresIT.java` | Integration | PostgreSQL (Testcontainers) | Full stack with real DB engine — catches SQL dialect issues before production |

### Running Tests

```bash
# All tests (including Testcontainers — requires Docker)
./mvnw test

# Unit + component tests only (no Docker needed)
./mvnw test -Dgroups="unit,component"

# Integration tests only
./mvnw test -Dtest=RealPostgresIT
```

---

## ☁️ Cloud-Native & Production Readiness

This project is not a toy — it implements every pattern required for a production Kubernetes deployment on AWS EKS, Azure AKS, or Google GKE.

### Kubernetes Health Probes

```yaml
# These Actuator endpoints satisfy K8s liveness and readiness requirements
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
```

### Multi-Stage Dockerfile — Production Image Pattern

```
Stage 1 (builder): eclipse-temurin:21-jdk-alpine
  └── mvn package -DskipTests
        └── produces: university-modern-1.0.0.jar

Stage 2 (runtime): eclipse-temurin:21-jre-alpine  ← 190 MB vs 600 MB JDK image
  └── COPY --from=builder target/*.jar app.jar
  └── ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

> **Why multi-stage?** The runtime image doesn't include the compiler, Maven, source code, or test dependencies. Smaller images mean faster pull times, smaller attack surface, and lower ECR/ACR/GCR egress costs — critical at fintech scale.

### Java 21 Virtual Threads — The Game Changer

```properties
# application.properties
spring.threads.virtual.enabled=true
```

| | Traditional Threads | Virtual Threads (Java 21) |
|---|---|---|
| **Size** | ~1 MB per thread | ~few KB per thread |
| **Max concurrent** | ~300 requests (typical 256 MB pod) | 100,000+ requests |
| **Blocking I/O** | Blocks OS thread (wastes CPU) | Parks virtual thread (OS thread free for other work) |
| **Code change** | N/A | Just one config line |
| **Fintech impact** | Throttled at high payment volume | Handles Black Friday traffic spikes |

### RFC 7807 — Structured Error Responses

```json
// Every error returns a machine-readable, human-readable payload
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{
  "type":      "https://api.university.example/errors/course-not-found",
  "title":     "Course Not Found",
  "status":    404,
  "detail":    "Course with ID 99 was not found.",
  "timestamp": "2026-03-05T10:30:00Z",
  "path":      "/api/courses/99"
}
```

> **Why this matters:** Without structured errors, debugging a 500 in a distributed fintech system requires reading raw stack traces in Splunk. With RFC 7807, the `type` URI links directly to the runbook, and every field is parseable by the calling service for automated retry / alert logic.

### Observability Stack Integration

```
Spring Boot Actuator → Micrometer → Prometheus → Grafana
                    ↓
             /actuator/prometheus  (metrics in Prometheus format)
             /actuator/health      (liveness / readiness)
             /actuator/info        (build version, deployment metadata)
```

---

## 🧠 Principal Architect's Self-Reinforcement Guide

> Use this section to deepen your understanding beyond just "running the app."  
> Each challenge maps to a real scenario you'll face in fintech system design interviews or production incidents.

### Level 1 — Understand (8th Grade → Junior Dev)

| Challenge | What to do | Files to Read |
|---|---|---|
| Draw the 5 layers from memory | Close this README and sketch the floors on paper | [ARCHITECTURE.md](ARCHITECTURE.md) §4 |
| Trace a `GET /api/courses` request top to bottom | Follow the call chain in writing | [SEQUENCE_DIAGRAMS.md](SEQUENCE_DIAGRAMS.md) Flow 1 |
| Explain what `@Transactional(readOnly=true)` does | Write 3 bullet points | `UniversityService.java` class Javadoc |
| Describe why tests use H2 but production uses PostgreSQL | Explain the tradeoff | [GETTING_STARTED.md](GETTING_STARTED.md) §4.2 |

### Level 2 — Apply (Junior → Mid-Level Dev)

| Challenge | What to do | Hint |
|---|---|---|
| Add a `POST /api/courses` endpoint | Write a new endpoint + validation | Study `CourseController.java`, add `@PostMapping` |
| Add a 5th enrollment status `OnLeave` to the sealed interface | Update `EnrollmentStatus.java` + fix the switch | Compiler will tell you every place to update |
| Write a Specification that filters by both `credits` AND `department` | Modify `DynamicQueryService` | Chain two predicates in the `BooleanBuilder` |
| Add pagination to `GET /api/courses` | Add `Pageable` parameter | Spring Data's `Page<Course>` return type |

### Level 3 — Architect (Senior → Principal)

| Challenge | What to decide | Design Principle |
|---|---|---|
| When should you use QueryDSL vs Specification? | Write a one-page decision document | Type safety vs. simplicity tradeoff |
| How would you shard this DB if student count reaches 10 billion? | Design a partitioning strategy | [Azure Cosmos DB](https://learn.microsoft.com/azure/cosmos-db/) hierarchical partition keys |
| How would you add caching to `findAll()` without changing controller code? | Propose and implement a caching layer | `@Cacheable` + Redis Sidecar |
| How would you break this monolith into 3 dedicated microservices? | Draw the service boundary diagram | DDD bounded contexts: Curriculum, Enrollment, Staff |
| How does Spring Cloud add to Spring Boot? | Map Circuit Breaker + Config Server to this app | [ARCHITECTURE.md](ARCHITECTURE.md) §4.0b |

### Level 4 — Production Incident Simulation

| Scenario | What would you check first? | Tool |
|---|---|---|
| App pod crashlooping in K8s | `kubectl logs` → check `/actuator/health/liveness` | Actuator liveness probe |
| Queries suddenly 10x slower | Enable `spring.jpa.show-sql=true`, check N+1 | Hibernate SQL log + explain plan |
| 429 errors from the external Open Library API | Add retry logic with exponential backoff | `RestClient` retry config |
| Test passes on H2 but fails on PostgreSQL | Run `RealPostgresIT` — SQL dialect difference | Testcontainers |

---

## 📊 Evaluation Scorecard

> Self-assessment rubric: score yourself after completing each level of the Self-Reinforcement Guide.  
> Target: **9.5 / 10** or above.

| Dimension | Max Score | What a 10 Looks Like |
|---|---|---|
| **Architecture fluency** — Can explain the 5-layer model, responsibility of each layer, and why boundaries exist | 2.0 | Draws from memory, names all classes, explains Spring Boot vs Spring Cloud split |
| **Java language mastery** — Can identify which Java version introduced each feature and explain the "why" | 2.0 | Explains Virtual Thread scheduling, gives sealed interface use case in fintech |
| **Data access depth** — Can compare all 5 query strategies, select the right one for a given problem | 1.5 | Correctly selects QueryDSL for a 4-field dynamic search with refactoring needs |
| **Production patterns** — Understands RFC 7807, Actuator probes, multi-stage Docker, Testcontainers | 1.5 | Can configure K8s probes from scratch, explains why multi-stage images matter |
| **Cloud-native thinking** — Can connect this to EKS/AKS deployment, Prometheus metrics, circuit breakers | 1.5 | Describes the full observability stack: metrics → alerting → runbook |
| **Code fluency** — Reads unfamiliar code confidently, locates features quickly | 1.5 | Finds any feature in under 60 seconds; explains Javadoc for any class |
| **TOTAL** | **10.0** | |

### Scoring Guide

| Score | Meaning |
|---|---|
| 9.5 – 10.0 | Principal-level: Can design, build, review, and operate this system end-to-end |
| 8.0 – 9.4 | Senior-level: Strong ownership; occasionally needs confirmation on edge cases |
| 6.0 – 7.9 | Mid-level: Solid core understanding; needs guidance on architecture decisions |
| Below 6.0 | Junior-level: Start with GETTING_STARTED.md and JAVA_FEATURES.md, then revisit |

---

## 📚 Further Reading

### This Repository's Documentation

| Document | Purpose |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Layer-by-layer breakdown with Mermaid diagrams for every component |
| [GETTING_STARTED.md](GETTING_STARTED.md) | Full setup guide — prerequisites, Docker, Maven, smoke tests, troubleshooting |
| [JAVA_FEATURES.md](JAVA_FEATURES.md) | Java 8→21 feature guide with before/after code and live project examples |
| [SEQUENCE_DIAGRAMS.md](SEQUENCE_DIAGRAMS.md) | 5 end-to-end request flows: happy path, 404, Specification, QueryDSL, SequencedCollection |
| [Spring_Data/WEBFLUX_VS_VIRTUAL_THREADS.md](Spring_Data/WEBFLUX_VS_VIRTUAL_THREADS.md) | Deep-dive concurrency comparison: reactive WebFlux vs Java 21 Virtual Threads |
| [Spring_Data/ARCHITECTURE_C4_LEVELS.md](Spring_Data/ARCHITECTURE_C4_LEVELS.md) | C4 model: Context → Container → Component → Code |

### Official Documentation

| Topic | Link |
|---|---|
| Spring Boot 3.3 Reference | https://docs.spring.io/spring-boot/docs/3.3.x/reference/html/ |
| Spring Data JPA | https://docs.spring.io/spring-data/jpa/reference/jpa.html |
| QueryDSL Reference | http://querydsl.com/static/querydsl/5.0.0/reference/html_single/ |
| Java 21 Feature Overview (JEPs) | https://openjdk.org/projects/jdk/21/ |
| RFC 7807 Problem Details | https://datatracker.ietf.org/doc/html/rfc7807 |
| Testcontainers | https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/ |

---

## License

This repository is released under the [MIT License](LICENSE).  
Use it to learn, build on it for your own projects, or reference it in interviews.

---

<div align="center">

_Built with precision by a principal fintech architect. Explained for everyone._  
_Java 21 · Spring Boot 3.3.5 · PostgreSQL 16 · Docker · Tested to production standards._

</div>
