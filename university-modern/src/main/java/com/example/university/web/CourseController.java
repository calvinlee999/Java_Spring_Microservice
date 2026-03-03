package com.example.university.web;

import com.example.university.business.UniversityService;
import com.example.university.domain.Course;
import com.example.university.repo.CourseRepo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.SequencedCollection;

/**
 * CourseController — the HTTP layer that exposes course data as REST endpoints.
 *
 * <p>All endpoints are under the {@code /api/courses} path.
 * Visit {@code http://localhost:8080/swagger-ui.html} to see them all in an
 * interactive browser UI.
 *
 * <p><b>Spring Boot 3.2+ Features demonstrated:</b>
 * <ul>
 *   <li><b>RestClient</b> — the new fluent HTTP client.  Used in
 *       {@link #getExternalLibraryInfo} to call Open Library's public API.
 *       Replaces the old, verbose {@code RestTemplate}.</li>
 *   <li><b>ProblemDetail</b> — RFC 7807 error responses are handled in
 *       {@link GlobalExceptionHandler}.  Any thrown {@link CourseNotFoundException}
 *       automatically becomes a structured JSON error.</li>
 * </ul>
 *
 * <p><b>Java 21 Feature:</b> The {@link #getCoursesReversed} endpoint returns
 * a {@link SequencedCollection}, using the Java 21 {@code List.reversed()} API.
 */
@Tag(name = "Courses", description = "Endpoints for browsing and searching university courses")
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final UniversityService universityService;
    private final CourseRepo        courseRepo;

    /**
     * Spring Boot 3.2+ auto-configures a {@link RestClient.Builder} bean.
     * We inject the builder and configure a base URL, then call {@code build()}
     * to get a ready-to-use {@link RestClient}.
     */
    private final RestClient restClient;

    public CourseController(UniversityService universityService,
                             CourseRepo courseRepo,
                             RestClient.Builder restClientBuilder) {
        this.universityService = universityService;
        this.courseRepo        = courseRepo;

        // Spring Boot 3.2+ RestClient — fluent, readable HTTP client
        // Much cleaner than old RestTemplate which required boilerplate.
        this.restClient = restClientBuilder
                .baseUrl("https://openlibrary.org")
                .build();
    }

    // -------------------------------------------------------------------------
    // Standard CRUD endpoints
    // -------------------------------------------------------------------------

    /**
     * Returns all courses in the university catalog.
     *
     * <p>{@code GET /api/courses}
     *
     * @return list of all {@link Course} objects as JSON
     */
    @Operation(summary = "List all courses",
               description = "Returns every course currently in the university catalog.")
    @GetMapping
    public List<Course> getAllCourses() {
        return universityService.findAllCourses();
    }

    /**
     * Returns one course by its database ID.
     *
     * <p>{@code GET /api/courses/{id}}
     *
     * <p>If no course with that ID exists, a {@link CourseNotFoundException} is
     * thrown. {@link GlobalExceptionHandler} catches it and returns a
     * 404 JSON response in RFC 7807 ProblemDetail format.
     *
     * @param id  the course's database ID (e.g. 31)
     * @return the matching course
     */
    @Operation(summary = "Get a course by ID",
               description = "Throws a 404 ProblemDetail JSON error if the course is not found.")
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Integer id) {
        Course course = courseRepo.findById(id)
                // orElseThrow() triggers GlobalExceptionHandler → 404 ProblemDetail
                .orElseThrow(() -> new CourseNotFoundException(id));
        return ResponseEntity.ok(course);
    }

    /**
     * Returns all courses with a specific number of credit hours.
     *
     * <p>{@code GET /api/courses/credits/{credits}}
     *
     * @param credits  number of credits to filter by (e.g. 3 or 4)
     * @return list of matching courses
     */
    @Operation(summary = "Find courses by credits")
    @GetMapping("/credits/{credits}")
    public List<Course> getCoursesByCredits(@PathVariable int credits) {
        return courseRepo.findByCredits(credits);
    }

    // -------------------------------------------------------------------------
    // Java 21 — SequencedCollection endpoints
    // -------------------------------------------------------------------------

    /**
     * Returns all courses in reversed order (newest first).
     *
     * <p><b>Java 21 Feature: SequencedCollection.reversed()</b>
     * The service returns a {@link SequencedCollection} — the reversed view
     * of the list without mutating the original.
     *
     * <p>{@code GET /api/courses/reversed}
     *
     * @return courses in reversed order
     */
    @Operation(summary = "List courses in reversed order (Java 21 SequencedCollection demo)")
    @GetMapping("/reversed")
    public SequencedCollection<Course> getCoursesReversed() {
        return universityService.findCoursesReversed();
    }

    /**
     * Returns the alphabetically first course.
     *
     * <p><b>Java 21 Feature: SequencedCollection.getFirst()</b>
     * Cleaner and more expressive than {@code list.get(0)}.
     *
     * <p>{@code GET /api/courses/first}
     *
     * @return the first course alphabetically by name
     */
    @Operation(summary = "Get the alphabetically first course (Java 21 getFirst() demo)")
    @GetMapping("/first")
    public Course getFirstCourse() {
        return universityService.findFirstCourse();
    }

    /**
     * Returns the alphabetically last course.
     *
     * <p>{@code GET /api/courses/last}
     *
     * @return the last course alphabetically by name
     */
    @Operation(summary = "Get the alphabetically last course (Java 21 getLast() demo)")
    @GetMapping("/last")
    public Course getLastCourse() {
        return universityService.findLastCourse();
    }

    // -------------------------------------------------------------------------
    // Spring Boot 3.2+ RestClient demo
    // -------------------------------------------------------------------------

    /**
     * Calls the Open Library public API and returns matching book info for a topic.
     *
     * <p><b>Spring Boot 3.2+ Feature: RestClient</b>
     * {@code RestClient} is the new, fluent HTTP client introduced in Spring Boot 3.2.
     * Compare the old way vs the new way:
     *
     * <p>Old ({@code RestTemplate}):
     * <pre>{@code
     *   RestTemplate rt = new RestTemplate();
     *   String result = rt.getForObject("https://.../{topic}", String.class, topic);
     * }</pre>
     *
     * <p>New ({@code RestClient}):
     * <pre>{@code
     *   restClient.get()
     *       .uri("/search.json?title={topic}&limit=1", topic)
     *       .retrieve()
     *       .body(String.class);
     * }</pre>
     *
     * <p>The new style is chainable, more readable, and supports reactive
     * patterns via {@code .retrieve().toMono()} if needed.
     *
     * <p>{@code GET /api/courses/external/{topic}}
     *
     * @param topic  the subject to look up on Open Library (e.g. "Java")
     * @return raw JSON from Open Library
     */
    @Operation(summary = "Call an external API via Spring Boot 3.2+ RestClient",
               description = "Queries the Open Library API — demonstrates RestClient as RestTemplate replacement.")
    @GetMapping("/external/{topic}")
    public String getExternalLibraryInfo(@PathVariable String topic) {
        // Spring Boot 3.2+ RestClient — fluent HTTP calls in 4 lines
        return restClient.get()
                .uri("/search.json?title={topic}&limit=1", topic)
                .retrieve()
                .body(String.class);
    }
}
