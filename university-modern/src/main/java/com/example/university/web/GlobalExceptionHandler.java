package com.example.university.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

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
 * <p>Example response for a missing course:
 * <pre>{@code
 * HTTP/1.1 404 Not Found
 * Content-Type: application/problem+json
 *
 * {
 *   "type":      "https://api.university.example/errors/course-not-found",
 *   "title":     "Course Not Found",
 *   "status":    404,
 *   "detail":    "Course with ID 99 was not found.",
 *   "timestamp": "2025-03-03T10:30:00Z",
 *   "path":      "/api/courses/99"
 * }
 * }</pre>
 *
 * <p>{@code @RestControllerAdvice} means this class watches ALL controllers.
 * When any of them throw a listed exception, this handler takes over.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());

        problem.setTitle("Invalid Request");
        problem.setType(URI.create("https://api.university.example/errors/bad-request"));
        problem.setProperty("timestamp", Instant.now().toString());

        return problem;
    }

    /**
     * Catch-all handler for unexpected server errors.
     * Returns HTTP 500 Internal Server Error.
     *
     * <p>In production, you'd want to log the full stack trace here rather than
     * exposing the raw exception message to the client.
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
