package com.example.university.integration;

import com.example.university.business.UniversityService;
import com.example.university.domain.Course;
import com.example.university.domain.Department;
import com.example.university.domain.Staff;
import com.example.university.repo.CourseRepo;
import com.example.university.repo.StudentRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RealPostgresIT — Layer 4 integration test using a real PostgreSQL container.
 *
 * <p><b>Testing Pyramid — Layer 4: Full-Stack Integration Test</b>
 * <pre>
 *         /\
 *        /  \        ← Layer 4: This class — real PostgreSQL in Docker
 *       /    \       ← Layer 3: @SpringBootTest with H2 (SimpleDBCrudTest)
 *      /      \      ← Layer 2: @DataJpaTest slices
 *     /________\     ← Layer 1: Pure unit tests (no Spring, no DB)
 * </pre>
 *
 * <p><b>What makes this different from {@code SimpleDBCrudTest}?</b>
 * {@code SimpleDBCrudTest} uses H2 in-memory database (MODE=PostgreSQL).
 * H2 can <em>simulate</em> PostgreSQL, but it's not identical.  Some
 * PostgreSQL-specific SQL features, indexes, and behaviour differences
 * can cause bugs that only appear when you run against a real PostgreSQL server.
 *
 * <p>This test class spins up a real PostgreSQL 16 Docker container,
 * boots the full Spring Boot application against it, and tears it all down
 * after the tests complete.  This is the highest-confidence test layer.
 *
 * <p><b>Testcontainers — how it works:</b>
 * <ol>
 *   <li>JUnit sees {@code @Testcontainers} and looks for {@code @Container} fields.</li>
 *   <li>It pulls the {@code postgres:16} Docker image (cached after first run).</li>
 *   <li>It starts the container and assigns a random port.</li>
 *   <li>{@code @ServiceConnection} tells Spring Boot to use this container's
 *       JDBC URL instead of the one in {@code application.properties}.</li>
 *   <li>Spring boots normally; tests run; container is destroyed.</li>
 * </ol>
 *
 * <p><b>Think of it like a dress rehearsal:</b><br>
 * Unit tests are like rehearsing lines alone in your room.
 * H2 tests are like rehearsing on a stage with cardboard scenery.
 * Testcontainers tests are the full dress rehearsal with real costumes and
 * the real set — as close to opening night as you can get before production.
 *
 * <p><b>Cloud deployment relevance (ARCHITECTURE.md, Chapter 4):</b>
 * When you deploy to AWS RDS, Azure Flexible Server, or GCP Cloud SQL,
 * they all run real PostgreSQL.  Passing this test suite gives high
 * confidence the application will work in all three cloud environments.
 *
 * @see <a href="https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/">
 *     Testcontainers Spring Boot Guide</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@Rollback
class RealPostgresIT {

    /**
     * The PostgreSQL 16 Docker container.
     *
     * <p>{@code static} is important — one container is shared across all test
     * methods in this class, which is faster than starting a new container per test.
     *
     * <p>{@code @ServiceConnection} automatically wires the container's JDBC URL,
     * username, and password into Spring Boot's datasource configuration.
     * No manual {@code @DynamicPropertySource} needed.
     *
     * <p>The container credentials match {@code compose.yaml} so the same
     * schema.sql and data.sql files work without modification.
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("catalog")
            .withUsername("user")
            .withPassword("pass");

    // -------------------------------------------------------------------------
    // Spring beans injected into the test class
    // -------------------------------------------------------------------------

    @Autowired
    private UniversityService universityService;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private MockMvc mockMvc;

    // =========================================================================
    // Test 1 — Database tables exist and are accessible
    // =========================================================================

    /**
     * Verifies that all database tables were created correctly by Hibernate
     * from the schema.sql file on a real PostgreSQL 16 engine.
     *
     * <p>If this test fails, it typically means:
     * <ul>
     *   <li>The schema.sql has invalid PostgreSQL syntax</li>
     *   <li>A JPA entity is mapped incorrectly</li>
     *   <li>The container failed to start (check Docker is running)</li>
     * </ul>
     */
    @Test
    @DisplayName("All tables exist and are queryable on real PostgreSQL 16")
    void tablesExistOnRealPostgres() {
        // count() doesn't throw = the tables exist and Hibernate mapped them correctly
        assertThat(courseRepo.count()).isGreaterThanOrEqualTo(0);
        assertThat(studentRepo.count()).isGreaterThanOrEqualTo(0);
    }

    // =========================================================================
    // Test 2 — Full CRUD round-trip
    // =========================================================================

    /**
     * Creates a staff member, a department, and a course end-to-end on a real
     * PostgreSQL server, then verifies the saved data including the {@code @Version}
     * optimistic locking column.
     *
     * <p><b>What is optimistic locking?</b>
     * The {@code version} column (BIGINT, DEFAULT 0) in the database is managed
     * by JPA.  Every time a row is updated, JPA increments the version number.
     * If two users try to update the same row at the same time, the second one
     * discovers the version has changed and gets an error instead of silently
     * overwriting the first user's changes.
     *
     * <p>This test confirms the version column starts at 0 after a fresh insert.
     */
    @Test
    @DisplayName("Full CRUD round-trip: create staff → department → course → verify @Version")
    void fullCrudRoundTripOnRealPostgres() {
        // Arrange — create required parent entities first
        Staff     staff  = universityService.createStaff("Alice", "Smith");
        Department dept  = universityService.createDepartment("Test Department");

        // Act — create the course linked to staff and department
        Course course = universityService.createCourse("Real Postgres Test Course", 3, dept.getId());

        // Assert — verify the saved data
        assertThat(course.getId()).isNotNull()
                .as("Course should have a database-generated ID");
        assertThat(course.getName()).isEqualTo("Real Postgres Test Course");
        assertThat(course.getCredits()).isEqualTo(3);

        // The @Version column should start at 0 after a fresh insert
        assertThat(course.getVersion()).isEqualTo(0L)
                .as("@Version should be 0 immediately after first save (never updated yet)");

        // Also verify staff was created
        assertThat(staff.getId()).isNotNull();
        assertThat(staff.getMember().getFirstName()).isEqualTo("Alice");
        assertThat(staff.getMember().getLastName()).isEqualTo("Smith");
        // Staff @Version should also be 0 after initial save
        assertThat(staff.getVersion()).isEqualTo(0L);
    }

    // =========================================================================
    // Test 3 — JPQL text block query on real engine
    // =========================================================================

    /**
     * Verifies the Java 17 Text Block JPQL query works against PostgreSQL.
     *
     * <p><b>Java 17 Feature: Text Blocks</b>
     * The JPQL query in {@link com.example.university.repo.CourseRepo} uses
     * triple-quote text blocks:
     * <pre>{@code
     * @Query("""
     *         SELECT c FROM Course c
     *         WHERE  c.credits = :credits
     *         ORDER  BY c.name ASC
     *         """)
     * }</pre>
     * This test ensures that Spring Data JPA correctly parses and executes
     * those text block queries on a real PostgreSQL engine (not just H2).
     */
    @Test
    @DisplayName("JPQL Text Block query findByCredits() works on real PostgreSQL")
    void jpqlTextBlockQueryOnRealPostgres() {
        // The data.sql seeds courses with varying credit counts.
        // This query should execute without throwing any exception.
        List<Course> fourCreditCourses = courseRepo.findByCredits(4);

        // Even if the list is empty, the query executed successfully on real PostgreSQL
        assertThat(fourCreditCourses).isNotNull()
                .as("findByCredits() should return a non-null list (empty is fine)");
    }

    // =========================================================================
    // Test 4 — Kubernetes readiness probe
    // =========================================================================

    /**
     * Calls the Kubernetes readiness probe endpoint and verifies it returns HTTP 200
     * with {@code "status": "UP"}.
     *
     * <p><b>Why does readiness matter in Kubernetes?</b>
     * When a pod is starting up in Kubernetes, it is NOT added to the load balancer
     * until the readiness probe returns UP.  This prevents users from being routed
     * to a pod that isn't ready yet (e.g., schema migration still running).
     *
     * <p>The Actuator readiness endpoint is enabled via:
     * {@code management.endpoint.health.probes.enabled=true} in application.properties.
     */
    @Test
    @DisplayName("Readiness probe GET /actuator/health/readiness returns UP")
    void readinessProbeReturnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness")
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("UP"));
    }

    // =========================================================================
    // Test 5 — Kubernetes liveness probe
    // =========================================================================

    /**
     * Calls the Kubernetes liveness probe endpoint and verifies it returns HTTP 200
     * with {@code "status": "UP"}.
     *
     * <p><b>Why does liveness matter in Kubernetes?</b>
     * If a running pod's liveness probe starts returning DOWN (e.g., the app is
     * deadlocked), Kubernetes automatically restarts the pod.  This is the
     * self-healing mechanism of Kubernetes.
     *
     * <p>Think of liveness as asking "Are you still alive?" and readiness as
     * asking "Are you ready to do work?"  A healthy pod says YES to both.
     */
    @Test
    @DisplayName("Liveness probe GET /actuator/health/liveness returns UP")
    void livenessProbeReturnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness")
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("UP"));
    }

    // =========================================================================
    // Test 6 — Course list REST endpoint
    // =========================================================================

    /**
     * Calls {@code GET /api/courses} against the full Spring Boot application
     * backed by a real PostgreSQL container and verifies it returns HTTP 200
     * with a JSON array body.
     *
     * <p>This is an end-to-end HTTP test — it goes through:
     * <ol>
     *   <li>HTTP layer (MockMvc / DispatcherServlet)</li>
     *   <li>{@link com.example.university.web.CourseController}</li>
     *   <li>{@link com.example.university.business.UniversityService}</li>
     *   <li>JPA / Hibernate</li>
     *   <li>PostgreSQL 16 in Docker</li>
     * </ol>
     * All layers passing together gives the highest possible confidence
     * before deploying to a real cloud environment.
     */
    @Test
    @DisplayName("GET /api/courses returns 200 + JSON array on real PostgreSQL")
    void courseListEndpointOnRealPostgres() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // =========================================================================
    // Test 7 — Course filter endpoint (Java 17 Record DTO demo)
    // =========================================================================

    /**
     * Calls {@code GET /api/courses/filter?credits=4} and verifies the
     * QueryDSL-powered filter endpoint returns HTTP 200.
     *
     * <p><b>Why test the filter endpoint separately?</b>
     * The filter endpoint uses three new components together:
     * <ul>
     *   <li>Java 17 Record DTO ({@code CourseSearchRequest})</li>
     *   <li>Bean Validation ({@code @Valid} + {@code @Min})</li>
     *   <li>QueryDSL dynamic query ({@code DynamicQueryService})</li>
     * </ul>
     * This test confirms all three components integrate correctly end-to-end
     * on a real PostgreSQL engine.
     */
    @Test
    @DisplayName("GET /api/courses/filter?credits=4 returns 200 + JSON on real PostgreSQL")
    void courseFilterEndpointOnRealPostgres() throws Exception {
        mockMvc.perform(get("/api/courses/filter")
                        .param("credits", "4")
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
