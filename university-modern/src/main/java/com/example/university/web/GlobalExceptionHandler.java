package com.example.university.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — catches exceptions from ANY controller and turns
 * them into structured JSON error responses.
 *
 * <p><b>Spring Boot 3.2+ Feature: ProblemDetail (RFC 7807)</b>
 * RFC 7807 defines a standard format for HTTP API error responses.
 * Instead of different apps returning errors in different shapes, RFC 7807
 * says every error should have these fields:
 * <ul>
 *   <li>{@code type}    — a URI identifying the error type</li>
 *   <li>{@code title}   — a short, human-readable summary</li>
 *   <li>{@code status}  — the HTTP status code (e.g. 404, 400)</li>
 *   <li>{@code detail}  — a longer explanation of what went wrong</li>
 * </ul>
 *
 * <p><b>Handlers in this class:</b>
 * <ol>
 *   <li>{@link #handleCourseNotFound}    — 404 when a course ID doesn't exist</li>
 *   <li>{@link #handleBadArgument}       — 400 for invalid enum/status values</li>
 *   <li>{@link #handleValidationErrors}  — 400 for {@code @Valid} + {@code @RequestBody} failures</li>
 *   <li>{@link #handleConstraintViolation} — 400 for Bean Validation on path/query params</li>
 *   <li>{@link #handleUnexpected}        — 500 catch-all</li>
 * </ol>
 *
 * <p>{@code @RestControllerAdvice} means this class watches ALL controllers.
 * When any of them throw a listed exception, this handler takes over.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * SLF4J logger — logs error details server-side while returning a safe
     * summary to the client.  Never expose a raw stack trace to a client;
     * that reveals internal implementation details to potential attackers.
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link CourseNotFoundException} — returns HTTP 404 with ProblemDetail body.
     *
     * <p>Spring automatically serialises the {@link ProblemDetail} object to JSON
     * with {@code Content-Type: application/problem+json}.
     *
     * @param ex       the exception thrown by the controller
     * @param request  the original HTTP request (used to include the request path)
     * @return a 404 ProblemDetail JSON response
     */
    @ExceptionHandler(CourseNotFoundException.class)
    public ProblemDetail handleCourseNotFound(CourseNotFoundException ex,
                                               HttpServletRequest request) {
        log.warn("Course not found — path={} message={}", request.getRequestURI(), ex.getMessage());

        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());

        problem.setTitle("Course Not Found");
        problem.setType(URI.create("https://api.university.example/errors/course-not-found"));
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("path",      request.getRequestURI());

        return problem;
    }

    /**
     * Handles general bad-input errors (e.g. invalid status string passed to
     * {@link com.example.university.business.UniversityService#toEnrollmentStatus}).
     * Returns HTTP 400 Bad Request.
     *
     * @param ex  the exception thrown
     * @return a 400 ProblemDetail JSON response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadArgument(IllegalArgumentException ex) {
        log.warn("Bad argument: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());

        problem.setTitle("Invalid Request");
        problem.setType(URI.create("https://api.university.example/errors/bad-request"));
        problem.setProperty("timestamp", Instant.now().toString());

        return problem;
    }

    /**
     * Handles Bean Validation failures on {@code @RequestBody} arguments
     * annotated with {@code @Valid} in a controller method.
     *
     * <p><b>When does this fire?</b>
     * If a JSON request body violates a constraint ({@code @NotBlank},
     * {@code @Min}, etc.) Spring MVC calls this handler instead of the
     * controller method.  The response lists <em>every</em> violation so
     * the client can fix all errors in one round trip.
     *
     * <p>Example response for a blank course name:
     * <pre>{@code
     * {
     *   "title": "Validation Failed",
     *   "status": 400,
     *   "detail": "name: Course name must not be blank",
     *   "violations": ["name: Course name must not be blank"],
     *   "timestamp": "..."
     * }
     * }</pre>
     *
     * @param ex       the validation exception containing all field errors
     * @param request  the incoming HTTP request
     * @return a 400 ProblemDetail listing all violated constraints
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex,
                                                 HttpServletRequest request) {
        // Collect every field error into "fieldName: message" strings
        var violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        String detail = violations.isEmpty() ? ex.getMessage() : violations.get(0);
        log.warn("Validation failed on {} — violations: {}", request.getRequestURI(), violations);

        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);

        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://api.university.example/errors/validation-failed"));
        problem.setProperty("violations", violations);
        problem.setProperty("timestamp",  Instant.now().toString());
        problem.setProperty("path",       request.getRequestURI());

        return problem;
    }

    /**
     * Handles Bean Validation failures on individual path variables or query parameters
     * annotated with {@code @Validated} at the class level.
     *
     * <p>Example: {@code GET /api/courses/-5/...} where the ID has an {@code @Min(1)}
     * constraint — this handler returns a clean 400 instead of a cryptic 500.
     *
     * @param ex  the constraint violation exception
     * @return a 400 ProblemDetail listing all violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        var violations = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());

        log.warn("Constraint violation — violations: {}", violations);

        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, violations.toString());

        problem.setTitle("Constraint Violation");
        problem.setType(URI.create("https://api.university.example/errors/constraint-violation"));
        problem.setProperty("violations", violations);
        problem.setProperty("timestamp",  Instant.now().toString());

        return problem;
    }

    /**
     * Catch-all handler for unexpected server errors.
     * Returns HTTP 500 Internal Server Error.
     *
     * <p>In production, log the full stack trace here rather than
     * exposing the raw exception message to the client.  The client only
     * receives a generic message; the full error is in the pod logs.
     *
     * @param ex  any unhandled exception
     * @return a 500 ProblemDetail JSON response
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        // Log the full exception server-side (visible in K8s pod logs / CloudWatch)
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again.");

        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.university.example/errors/internal"));
        problem.setProperty("timestamp", Instant.now().toString());

        return problem;
    }
}

    /**
     * Handles {@link CourseNotFoundException} — returns HTTP 404 with ProblemDetail body.
     *
     * <p>Spring automatically serialises the {@link ProblemDetail} object to JSON
     * with {@code Content-Type: application/problem+json}.
     *
     * @param ex       the exception thrown by the controller
     * @param request  the original HTTP request (used to include the request path)
     * @return a 404 ProblemDetail JSON response
     */
    @ExceptionHandler(CourseNotFoundException.class)
    public ProblemDetail handleCourseNotFound(CourseNotFoundException ex,
                                               HttpServletRequest request) {
        // ProblemDetail.forStatusAndDetail() creates the object with status + detail pre-filled.
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());

        problem.setTitle("Course Not Found");
        // setType() sets the "type" field — should be a stable URI describing the error.
        problem.setType(URI.create("https://api.university.example/errors/course-not-found"));
        // setProperty() adds custom extension fields beyond the RFC 7807 standard fields.
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("path",      request.getRequestURI());

        return problem;
    }
     *
     * @param ex  any unhandled exception
     * @return a 500 ProblemDetail JSON response
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again.");

        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.university.example/errors/internal"));
        problem.setProperty("timestamp", Instant.now().toString());
        // Only log the real cause internally — don't expose internal details to clients.
        System.err.println("Unexpected error: " + ex.getMessage());

        return problem;
    }
}
