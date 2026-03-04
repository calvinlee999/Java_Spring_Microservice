package com.example.university.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * CourseSearchRequest — a Java 17 {@code record} DTO for filtering courses.
 *
 * <p><b>Java 17 Feature: Records</b>
 * A {@code record} is a special kind of Java class designed to hold data.
 * Before records, you had to write a full POJO:
 * <pre>{@code
 *   public class CourseSearchRequest {
 *       private final String name;
 *       private final Integer credits;
 *       private final String department;
 *       // + constructor, getters, equals(), hashCode(), toString()...
 *   }
 * }</pre>
 *
 * With a record, that entire class collapses into ONE line:
 * <pre>{@code
 *   public record CourseSearchRequest(String name, Integer credits, String department) {}
 * }</pre>
 *
 * <p>The Java compiler automatically generates:
 * <ul>
 *   <li>An all-args constructor</li>
 *   <li>Accessor methods: {@code name()}, {@code credits()}, {@code department()}</li>
 *   <li>{@code equals()} and {@code hashCode()} based on all components</li>
 *   <li>A readable {@code toString()}</li>
 * </ul>
 *
 * <p><b>Bean Validation annotations on record components</b>
 * You can put validation annotations directly on record components.
 * When {{@code @Valid}} is used in the controller, Spring validates these
 * constraints before the endpoint body runs:
 * <ul>
 *   <li>{@code @Size(max=100)} — ensures the name search isn't too long</li>
 *   <li>{@code @Min(1)}        — ensures credits filter is at least 1</li>
 * </ul>
 *
 * <p><b>Think of it like a search form:</b><br>
 * If you were searching a library catalog, this record holds what you typed
 * into the "Title", "Credits", and "Department" boxes.  It's a snapshot of
 * the search criteria, nothing more.
 *
 * @param name        partial course name match (case-insensitive, max 100 chars)
 * @param credits     exact credit-hour count (e.g. 3 or 4); must be ≥ 1 if provided
 * @param department  exact department name (e.g. "Humanities", "Sciences")
 *
 * @see com.example.university.web.CourseController#filterCourses
 * @see com.example.university.business.DynamicQueryService#filterBySpecification
 */
@Schema(
    description = "Query parameters for filtering courses. All fields are optional — "
                + "provide any combination and only matching courses are returned."
)
public record CourseSearchRequest(

    /**
     * Partial course name to search for (case-insensitive contains match).
     * For example, "eng" would match "English Literature" and "Engineering Math".
     * Maximum 100 characters to prevent expensive wildcard queries.
     */
    @Schema(
        description = "Partial course name match (case-insensitive contains)",
        example     = "English",
        maxLength   = 100
    )
    @Size(
        max     = 100,
        message = "Name search term must be 100 characters or fewer"
    )
    String name,

    /**
     * Exact credit-hour count.
     * "Credits" means how many hours per week a course meets (1 = one lecture
     * per week, 4 = a lab-heavy course with 4 hours of instruction).
     * Must be at least 1 if provided.
     */
    @Schema(
        description = "Exact credit-hour count",
        example     = "3",
        minimum     = "1"
    )
    @Min(
        value   = 1,
        message = "Credits filter must be at least 1"
    )
    Integer credits,

    /**
     * Exact department name (case-sensitive).
     * Must match a department stored in the database, e.g. "Humanities".
     * If no department with this name exists, the filter returns an empty list.
     */
    @Schema(
        description = "Exact department name",
        example     = "Humanities"
    )
    String department

) {}
