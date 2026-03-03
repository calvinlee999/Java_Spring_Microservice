package com.example.university.web;

/**
 * CourseNotFoundException — thrown when a requested course cannot be found.
 *
 * <p>This is a "runtime exception" (unchecked), meaning callers don't have to
 * declare it with {@code throws}.  Spring's {@link GlobalExceptionHandler}
 * catches it and converts it into a clean JSON error response using
 * {@link org.springframework.http.ProblemDetail} (RFC 7807 format).
 *
 * <p>Example JSON error response:
 * <pre>{@code
 * {
 *   "type":   "about:blank",
 *   "title":  "Course Not Found",
 *   "status": 404,
 *   "detail": "Course with ID 999 was not found.",
 *   "timestamp": "2025-03-03T10:30:00Z"
 * }
 * }</pre>
 */
public class CourseNotFoundException extends RuntimeException {

    /**
     * Creates an exception for a course looked up by numeric ID.
     *
     * @param id  the database ID that was not found
     */
    public CourseNotFoundException(Integer id) {
        super("Course with ID " + id + " was not found.");
    }

    /**
     * Creates an exception for a course looked up by name.
     *
     * @param name  the course name that was not found
     */
    public CourseNotFoundException(String name) {
        super("Course with name '" + name + "' was not found.");
    }
}
