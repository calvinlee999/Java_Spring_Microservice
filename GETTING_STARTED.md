# Getting Started — University Modern

> **Who this is for:** Anyone from a curious 8th grader to a working developer.  
> Every step is explained in plain English, with the "why" included alongside the "how."  
> No prior Java experience required — just follow each step in order.

---

## What Are We Building?

We are running a **University Catalog API** — a program that manages:
- Professors (called "Staff")
- Departments (Humanities, Sciences, etc.)
- Courses (with credits, instructors, and prerequisites)
- Students (with enrollment status)

The program speaks to a **PostgreSQL database** (think of it as a very organised filing cabinet) and exposes a **REST API** (a set of web addresses you can call from your browser or a tool like Postman).

---

## Table of Contents

1. [What You Need to Install (Prerequisites)](#step-1-what-you-need-to-install-prerequisites)
2. [Get the Project Files (Clone)](#step-2-get-the-project-files-clone)
3. [Understand the Folder Structure](#step-3-understand-the-folder-structure)
4. [Understand the Configuration Files](#step-4-understand-the-configuration-files)
5. [Start the Database (Docker Compose)](#step-5-start-the-database-docker-compose)
6. [Run the Application (Two Ways)](#step-6-run-the-application-two-ways)
7. [Verify the App is Running (Smoke Tests)](#step-7-verify-the-app-is-running-smoke-tests)
8. [Run the Automated Tests](#step-8-run-the-automated-tests)
9. [Stop Everything](#step-9-stop-everything)
10. [Run the Whole Stack in Docker (Optional)](#step-10-run-the-whole-stack-in-docker-optional)
11. [Troubleshooting](#step-11-troubleshooting)

---

## Step 1: What You Need to Install (Prerequisites)

> Think of prerequisites like tools in a toolbox. Before you can build furniture, you need a hammer and a saw. Before you can run this project, you need the following tools installed.

### 1.1 Check what you already have

Open a terminal (on macOS: press `Cmd + Space`, type `Terminal`, press Enter) and run these commands one at a time:

```bash
java -version
```

You should see something like `openjdk 21.0.x`. We need **Java 21 or higher**.

```bash
mvn -version
```

You should see something like `Apache Maven 3.9.x`. We need **Maven 3.8 or higher**.

```bash
docker --version
```

You should see something like `Docker version 26.x.x`. We need **Docker 24 or higher**.

```bash
docker compose version
```

You should see `Docker Compose version v2.x.x`. We need **Docker Compose v2**.

---

### 1.2 Install anything that's missing

| Tool | What it does | Download link |
|---|---|---|
| **Java 21 JDK** | Runs Java programs. JDK = Java Development Kit — needed to compile and run code. | [Adoptium JDK 21](https://adoptium.net/temurin/releases/?version=21) |
| **Maven 3.9** | A "build tool" — downloads dependencies and compiles the project. Think: foreman on a construction site. | [Maven Downloads](https://maven.apache.org/download.cgi) — or use `brew install maven` on macOS |
| **Docker Desktop** | Runs containers. A container is like a tiny computer inside your computer that runs one specific program (in our case, PostgreSQL). | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| **Git** | Downloads code from GitHub. | [Git Downloads](https://git-scm.com/downloads) — or use `brew install git` on macOS |

> **Shortcut for macOS with Homebrew:**  
> If you have [Homebrew](https://brew.sh) installed, run this single command:
> ```bash
> brew install openjdk@21 maven git && brew install --cask docker
> ```

---

## Step 2: Get the Project Files (Clone)

> "Cloning" means copying all the project files from GitHub to your computer.

```bash
# 1. Choose a folder to put the project in
cd ~/Desktop

# 2. Clone (download) the repository
git clone https://github.com/calvinlee999/Java_Spring_Microservice.git

# 3. Go into the project folder
cd Java_Spring_Microservice/university-modern
```

After this, you are inside the `university-modern` folder. This is where all the commands below must be run.

---

## Step 3: Understand the Folder Structure

> Before cooking a recipe, it helps to know what each ingredient is. Here is what each file and folder does.

```
university-modern/
│
├── pom.xml                         ← The "ingredients list" — declares all Java libraries
│                                     the project needs (Spring Boot, PostgreSQL driver, etc.)
│
├── Dockerfile                      ← Recipe to package the app into a Docker container
├── compose.yaml                    ← Starts both the database AND the app with one command
│
├── postgres/
│   └── schema.sql                  ← Creates the database tables (runs automatically on first start)
│
└── src/
    ├── main/
    │   ├── java/com/example/university/
    │   │   ├── UniversityApplication.java   ← The front door — "turn on" the whole app
    │   │   ├── domain/                      ← The "things" (Course, Student, Staff, etc.)
    │   │   ├── repo/                        ← Database access — CRUD operations
    │   │   ├── business/                    ← The logic — what the app actually does
    │   │   └── web/                         ← HTTP endpoints — what the outside world calls
    │   └── resources/
    │       └── application.properties       ← Settings for when the app runs normally
    │
    └── test/
        ├── java/com/example/university/dao/
        │   ├── ModernFeaturesTest.java      ← Tests every Java 17/21 feature end-to-end
        │   └── SimpleDBCrudTest.java        ← Tests basic save/find/update/delete
        └── resources/
            ├── application.properties       ← Settings used by tests (H2 in-memory DB)
            └── data.sql                     ← Pre-loads test data (staff, courses, etc.)
```

### The Five Layers (like floors in a building)

```
Floor 5 — Web Layer       (CourseController, GlobalExceptionHandler)
             ↕ HTTP requests come in here; JSON responses go out here
Floor 4 — Service Layer   (UniversityService, DynamicQueryService)
             ↕ Business logic lives here
Floor 3 — Repository Layer (CourseRepo, StudentRepo, StaffRepo, etc.)
             ↕ Talks to the database
Floor 2 — Domain Layer    (Course, Student, Staff, Department, etc.)
             ↕ The "nouns" — what the app knows about
Floor 1 — Database        (PostgreSQL — the actual storage)
```

---

## Step 4: Understand the Configuration Files

### 4.1 `src/main/resources/application.properties`

This file controls how the app behaves when you run it normally (not during tests).

```properties
# Show every SQL query Hibernate sends — great for learning
spring.jpa.show-sql=true

# Don't let Hibernate change the database schema — we manage it ourselves via schema.sql
spring.jpa.hibernate.ddl-auto=none

# Where the database lives (matches the values in compose.yaml)
spring.datasource.url=jdbc:postgresql://localhost:5432/catalog
spring.datasource.username=user
spring.datasource.password=pass

# Java 21 Virtual Threads — makes the app handle many users at once, super efficiently
spring.threads.virtual.enabled=true

# Show proper JSON error messages (e.g. { "status": 404, "detail": "Course not found" })
spring.mvc.problemdetails.enabled=true
```

> **What is a property file?**  
> Think of it as a list of settings, like adjusting the brightness and volume on your phone. Each line is one setting in the format `name=value`.

### 4.2 `src/test/resources/application.properties`

Tests use a **different** configuration. The key difference is the database:

| Setting | Normal Run | Tests |
|---|---|---|
| Database | PostgreSQL in Docker | H2 in-memory (no Docker needed) |
| Schema creation | `schema.sql` (manual) | `create-drop` (Hibernate does it) |
| Test data | Not pre-loaded | `data.sql` runs automatically |
| Startup time | ~3-5 seconds | ~1-2 seconds |

> **Why use a different database for tests?**  
> Tests should be fast and isolated. H2 (the test database) starts in under a second inside Java's memory. You don't need Docker running to run tests.

### 4.3 `compose.yaml`

This file tells Docker to start two services with one command:

1. **db** — PostgreSQL database (port 5432)
2. **app** — The Spring Boot application itself (port 8080)

The `db` service has a health check — Docker will wait until PostgreSQL is fully ready before starting `app`.

---

## Step 5: Start the Database (Docker Compose)

> The app needs a running PostgreSQL database to store data. Docker Compose handles this automatically.

Make sure you are in the `university-modern` folder, then run:

```bash
# Start only the database (in the background, so your terminal stays free)
docker compose up db -d
```

**What just happened?**
- Docker pulled the `postgres:16` image from the internet (first time only — ~50 MB)
- A PostgreSQL database started inside a container
- The `schema.sql` file was run automatically — all tables are now created
- The database is now available at `localhost:5432`

**Verify it started correctly:**

```bash
docker compose ps
```

You should see `university-db` with status `Up (healthy)`.

> **Analogy:** Starting Docker Compose is like turning on a filing cabinet. The filing cabinet (PostgreSQL) is now open and ready, but the office manager (our Spring Boot app) hasn't arrived yet.

---

## Step 6: Run the Application (Two Ways)

Choose **Way A** (Maven) for development — it's faster to restart and you see logs immediately.  
Choose **Way B** (Docker Compose) to run everything as a single deployable unit.

---

### Way A: Run with Maven (Best for Development)

```bash
# From the university-modern/ folder
./mvnw spring-boot:run
```

> If `./mvnw` doesn't work on Windows, use `mvnw.cmd spring-boot:run` instead.

**What this does:**
1. Maven downloads all declared dependencies from the internet (first time only — may take 2-3 minutes)
2. The QueryDSL APT plugin generates `QCourse`, `QStaff`, etc. from your `@Entity` classes
3. Maven compiles all `.java` files into `.class` files
4. Spring Boot starts the embedded Tomcat server
5. Your app is live at `http://localhost:8080`

**Watch the logs for this line — it means the app started successfully:**

```
Started UniversityApplication in 2.8 seconds
```

Typical full startup output:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.3.5)

...
Hibernate: create table course ...
Hibernate: insert into ...
Started UniversityApplication in 2.8 seconds (process running for 3.2)
```

---

### Way B: Run with Docker Compose (Full Stack)

This builds the app into a Docker image and runs everything together.

```bash
# Build the image and start both db + app
docker compose up --build
```

Wait for both services to show `healthy` or `Started`. This takes longer the first time because Docker must:
1. Download the Maven + JDK build image (~600 MB, one time only)
2. Compile your code inside the container
3. Build the final JRE image

Access the app at `http://localhost:8080` once you see `Started UniversityApplication`.

---

## Step 7: Verify the App is Running (Smoke Tests)

> A "smoke test" is a quick check — like turning on a light switch to see if the power is on before doing anything else.

### 7.1 Open Swagger UI (Recommended — point-and-click)

Open your browser and go to:

```
http://localhost:8080/swagger-ui.html
```

You should see an interactive page listing all API endpoints. This is the **easiest way to test the API** — no tools needed.

Click on any endpoint → click **"Try it out"** → click **"Execute"** → see the response.

---

### 7.2 Test with curl (Command-Line)

These commands work in any terminal.

**List all courses:**

```bash
curl http://localhost:8080/api/courses
```

Expected response: a JSON array of course objects.

**Get a single course by ID:**

```bash
curl http://localhost:8080/api/courses/31
```

Expected response:
```json
{
  "id": 31,
  "name": "English 101",
  "credits": 3,
  ...
}
```

**Test the 404 error (ProblemDetail RFC-7807):**

```bash
curl http://localhost:8080/api/courses/999
```

Expected response — a structured error, not a plain HTML error page:
```json
{
  "type": "https://api.university.example/errors/course-not-found",
  "title": "Course Not Found",
  "status": 404,
  "detail": "Course with ID 999 was not found.",
  "timestamp": "2026-03-03T10:30:00Z",
  "path": "/api/courses/999"
}
```

**Test courses in reversed order (Java 21 SequencedCollection demo):**

```bash
curl http://localhost:8080/api/courses/reversed
```

Expected: courses listed from highest ID to lowest.

---

### 7.3 Test Spring Data REST (HAL auto-endpoints)

Spring Data REST automatically creates database endpoints. No controller code was written for these:

```bash
# List all staff (auto-generated by @RepositoryRestResource)
curl http://localhost:8080/staff

# List all departments with the "showChair" projection
curl "http://localhost:8080/departments?projection=showChair"
```

---

### 7.4 Full API Endpoint Reference

| Method | URL | What it does |
|---|---|---|
| `GET` | `/api/courses` | List all courses |
| `GET` | `/api/courses/{id}` | Get one course by ID |
| `GET` | `/api/courses/reversed` | Courses reversed (Java 21 demo) |
| `GET` | `/api/courses/filter?credits=3` | Dynamic filter via JPA Specification |
| `GET` | `/api/courses/querydsl?credits=3` | Dynamic filter via QueryDSL |
| `GET` | `/api/courses/qbe?credits=3` | Dynamic filter via Query by Example |
| `GET` | `/api/courses/library` | External API call via RestClient demo |
| `GET` | `/staff` | All staff (Spring Data REST HAL) |
| `GET` | `/students` | All students (Spring Data REST HAL) |
| `GET` | `/departments` | All departments (Spring Data REST HAL) |
| `GET` | `/courses` | All courses (Spring Data REST HAL) |

---

## Step 8: Run the Automated Tests

> Tests are like a checklist that runs automatically. Each test is a question: "Does the app do what it's supposed to?" If all tests pass (green), the code is working correctly.

**Automated tests do NOT need Docker or PostgreSQL running** — they use H2 in-memory instead.

### 8.1 Run all tests

```bash
# From the university-modern/ folder
./mvnw test
```

Maven will:
1. Start an H2 in-memory database
2. Create all tables using Hibernate (`ddl-auto=create-drop`)
3. Load `data.sql` (staff, courses, students)
4. Run every test class
5. Print a summary
6. Drop the H2 database (clean up automatically)

**Successful output looks like:**

```
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Failed test output (example):**

```
[ERROR] Tests run: 14, Failures: 1, Errors: 0, Skipped: 0
[ERROR] ModernFeaturesTest » SequencedCollectionTests » firstCourseMatchesExpected
  AssertionError: expected "English 101" but was "Physics"
```

---

### 8.2 Run a specific test class

```bash
# Run only the modern features test
./mvnw test -Dtest=ModernFeaturesTest

# Run only the basic CRUD test
./mvnw test -Dtest=SimpleDBCrudTest
```

---

### 8.3 Run a single test method

```bash
# Run one specific test
./mvnw test -Dtest="ModernFeaturesTest#recordAccessorsWork"
```

---

### 8.4 What the tests verify

| Test Class | What it tests |
|---|---|
| `ModernFeaturesTest` | All Java 17/21 features — record accessors, sealed interface, pattern matching, SequencedCollection, sealed switch with record deconstruction |
| `SimpleDBCrudTest` | Basic database operations — create a course, find it by ID, update it, delete it |

**Test groups inside `ModernFeaturesTest`:**

| Group (`@Nested`) | What it checks |
|---|---|
| `RecordTests` | `Person` record has correct accessors (`firstName()` not `getFirstName()`), correct `equals()` and `toString()` |
| `SealedInterfaceTests` | `EnrollmentStatus` permits only `Active`, `Graduated`, `Suspended` — no other subclass is possible |
| `PatternMatchingTests` | `instanceof` pattern matching casts without a separate cast line |
| `SequencedCollectionTests` | `list.getFirst()`, `list.getLast()`, `list.reversed()` all work correctly |
| `SealedSwitchTests` | A `switch` over `EnrollmentStatus` with no `default` branch compiles and handles all cases |

---

### 8.5 Generate a test report

```bash
./mvnw test && open target/surefire-reports/
```

This opens the folder containing HTML and XML test result files.

---

## Step 9: Stop Everything

**Stop the Maven-run app:** Press `Ctrl + C` in the terminal where it's running.

**Stop the Docker database:**

```bash
docker compose down
```

**Stop Docker and delete all data (full reset):**

```bash
docker compose down -v
```

> The `-v` flag removes the Docker volumes — this means the database is completely wiped. Next time you start, it will be fresh.

---

## Step 10: Run the Whole Stack in Docker (Optional)

This runs both the database and the Spring Boot app inside Docker — no Java or Maven needed on your machine.

```bash
# Build and start everything
docker compose up --build

# Wait for both containers to show "healthy" / "Started"
# Then open:
# http://localhost:8080/swagger-ui.html
```

**Check that both containers are running:**

```bash
docker compose ps
```

Expected output:
```
NAME              IMAGE                    STATUS
university-db     postgres:16              Up (healthy)
university-app    university-modern-app    Up
```

**View live application logs:**

```bash
docker compose logs -f app
```

**View live database logs:**

```bash
docker compose logs -f db
```

---

## Step 11: Troubleshooting

### Problem: `java: command not found`

Java is not installed or not on your PATH.

```bash
# macOS with Homebrew
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

---

### Problem: `Cannot connect to the Docker daemon`

Docker Desktop is not running. Open Docker Desktop from your Applications folder, wait for the whale icon in the menu bar to show "Docker Desktop is running", then try again.

---

### Problem: Port 5432 already in use

Something else (possibly another PostgreSQL) is already using port 5432.

```bash
# Find what is using port 5432
lsof -i :5432

# Stop the conflicting process (replace PID with the number shown)
kill -9 <PID>
```

Or change the port in `compose.yaml`:
```yaml
ports:
  - "5433:5432"   # use 5433 on your machine instead
```

And update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/catalog
```

---

### Problem: Port 8080 already in use

```bash
# Find what is on port 8080
lsof -i :8080

# Kill it
kill -9 <PID>
```

Or run the app on a different port:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

---

### Problem: Tests fail with `Table "COURSE" not found`

The H2 database is not being created before the test data is loaded. Verify that `src/test/resources/application.properties` contains:

```properties
spring.jpa.hibernate.ddl-auto=create-drop
spring.sql.init.mode=always
```

---

### Problem: `QCourse cannot be resolved` (compile error)

The QueryDSL APT code generator hasn't run yet. Force it:

```bash
./mvnw generate-sources
```

Then rebuild:

```bash
./mvnw clean compile
```

The generated `QCourse`, `QStaff`, etc. classes will appear in `target/generated-sources/`.

---

### Problem: `BUILD FAILURE` — `Could not resolve dependencies`

Maven can't download dependencies. Check your internet connection, then:

```bash
# Clear the local Maven cache and retry
./mvnw clean install -U
```

The `-U` flag forces Maven to re-check for updated snapshots.

---

## Quick Reference Card

```
# ── Get the project ─────────────────────────────────────────────────────
git clone https://github.com/calvinlee999/Java_Spring_Microservice.git
cd Java_Spring_Microservice/university-modern

# ── Start the database ───────────────────────────────────────────────────
docker compose up db -d

# ── Run the app (development) ────────────────────────────────────────────
./mvnw spring-boot:run

# ── Run all tests (no Docker needed) ─────────────────────────────────────
./mvnw test

# ── Open the API browser ─────────────────────────────────────────────────
open http://localhost:8080/swagger-ui.html

# ── Stop the database ────────────────────────────────────────────────────
docker compose down

# ── Run everything in Docker ─────────────────────────────────────────────
docker compose up --build
```

---

*Written as of 2026-03-03 · `university-modern` with Spring Boot 3.3.5 · Java 21*
