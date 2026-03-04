package com.example.university.web;

import com.example.university.business.CourseFilter;
import com.example.university.business.DynamicQueryService;
import com.example.university.business.UniversityService;
import com.example.university.domain.Course;
import com.example.university.repo.CourseRepo;
import com.example.university.repo.DepartmentRepo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 *   <li><b>RestClient</b> — the new fluent HTTP client, replaces {@code RestTemplate}.
 *       Used in {@link #getExternalLibraryInfo} to query Open Library.</li>
 *   <li><b>ProblemDetail</b> — RFC 7807 structured error responses via
 *       {@link GlobalExceptionHandler}. Bad data → 400 JSON, missing ID → 404 JSON.</li>
 * </ul>
 *
 * <p><b>Java 21 Features:</b>
 * <ul>
 *   <li>{@link SequencedCollection} — {@link #getCoursesReversed} uses
 *       {@code List.reversed()} without mutating the original list.</li>
 * </ul>
 *
 * <p><b>Java 17 Feature: Record DTO</b>
 * {@link #filterCourses} accepts a {@link CourseSearchRequest} — a Java 17
 * {@code record}, which is an immutable data carrier that replaces verbose
 * POJO DTOs.  Combined with {@code @Valid} it demonstrates Bean Validation on
 * query parameters.
 *
 * <p><b>Production pattern: Constructor Injection</b>
 * All dependencies are injected through the constructor (not {@code @Autowired}
 * fields) so they can be made {@code final} — preventing accidental reassignment
 * and making unit testing easy with plain {@code new} calls.
 */
@Tag(name = "Courses", description = "Endpoints for browsing and searching university courses")
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final UniversityService  universityService;
    private final CourseRepo          courseRepo;
    private final DynamicQueryService dynamicQueryService;
    private final DepartmentRepo      departmentRepo;

    /**
     * Spring Boot 3.2+ auto-configures a {@link RestClient.Builder} bean.
     * We inject the builder and configure a base URL, then call {@code build()}
     * to get a ready-to-use {@link RestClient}.
     */
    private final RestClient restClient;

    /**
     * Constructor injection — Spring automatically finds and provides all listed beans.
     * Using a constructor (instead of {@code @Autowired} fields) means every
     * dependency is {@code final}: it cannot be changed after the object is created.
     * That makes the controller thread-safe and easy to unit-test.
     *
     * @param universityService   business logic for course/staff/student operations
     * @param courseRepo          direct JPA repository for course data
     * @param dynamicQueryService QueryDSL-powered dynamic search service
     * @param departmentRepo      JPA repository for department lookups by name
     * @param restClientBuilder   Spring-provided builder for the Spring Boot 3.2+ RestClient
     */
    public CourseController(UniversityService  universityService,
                             CourseRepo         courseRepo,
                             DynamicQueryService dynamicQueryService,
                             DepartmentRepo      departmentRepo,
                             RestClient.Builder restClientBuilder) {
        this.universityService  = universityService;
        this.courseRepo         = courseRepo;
        this.dynamicQueryService = dynamicQueryService;
        this.departmentRepo      = departmentRepo;

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
    // Java 17 Record DTO + Bean Validation filter endpoint
    // -------------------------------------------------------------------------

    /**
     * Filter courses by name (partial), credit count, or department.
     * Any combination of the three params is allowed; omit a param to skip that filter.
     *
     * <p><b>Java 17 Feature: Record DTO ({@link CourseSearchRequest})</b>
     * {@code CourseSearchRequest} is a Java 17 {@code record} — a compact,
     * immutable data class.  Instead of a full POJO (private fields, getters,
     * constructor, equals/hashCode, toString), a record needs one line:
     * {@code record CourseSearchRequest(String name, Integer credits, String department)}.
     * Spring MVC maps query parameters to it via {@code @ModelAttribute}.
     *
     * <p><b>Bean Validation + {@code @Valid}</b>
     * The {@code @Valid} annotation tells Spring to run the Bean Validation
     * constraints declared on {@link CourseSearchRequest} before the method body
     * runs.  If any constraint fails, {@link GlobalExceptionHandler#handleValidationErrors}
     * returns a 400 response listing every violation — no manual if-statements needed.
     *
     * <p><b>Example requests:</b>
     * <pre>
     *   GET /api/courses/filter?name=english
     *   GET /api/courses/filter?credits=3
     *   GET /api/courses/filter?department=Humanities
     *   GET /api/courses/filter?name=math&amp;credits=4
     * </pre>
     *
     * @param search  validated query-param DTO (name max 100 chars, credits ≥ 1)
     * @return filtered list of {@link Course} objects
     */
    @Operation(
        summary     = "Filter courses by name, credits, or department",
        description = "Java 17 Record DTO + @Valid Bean Validation + QueryDSL dynamic query demo. "
                    + "All three parameters are optional; combine them freely."
    )
    @GetMapping("/filter")
    public List<Course> filterCourses(@Valid @ModelAttribute CourseSearchRequest search) {
        // Build the filter step-by-step (Builder pattern using CourseFilter).
        // Each null-check below guards against optional params not being provided.
        CourseFilter filter = CourseFilter.filterBy();

        // If a name was provided, add partial-match filter
        if (search.name() != null && !search.name().isBlank()) {
            filter.nameLike(search.name());
        }

        // If credits were provided, add exact-match filter
        if (search.credits() != null) {
            filter.credits(search.credits());
        }

        // If department was provided, look it up by name and add the filter
        if (search.department() != null && !search.department().isBlank()) {
            departmentRepo.findByName(search.department())
                          .ifPresent(filter::department);
        }

        // Run the QueryDSL query with whatever filters were set
        return dynamicQueryService.filterBySpecification(filter);
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
