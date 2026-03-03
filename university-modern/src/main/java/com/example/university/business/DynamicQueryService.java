package com.example.university.business;

import com.example.university.domain.Course;
import com.example.university.domain.QCourse;
import com.example.university.repo.CourseQueryDslRepo;
import com.example.university.repo.CourseRepo;
import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * DynamicQueryService — demonstrates three different ways to build dynamic queries.
 *
 * <p>A "dynamic query" is one where the search criteria aren't known at compile
 * time — the user picks what to filter by at runtime.  Spring Data provides
 * three strategies for this:
 *
 * <ol>
 *   <li><b>Specification API</b> — uses JPA Criteria API under the hood.
 *       You build conditions programmatically using lambdas.</li>
 *   <li><b>QueryDSL</b> — uses auto-generated Q-classes for type-safe queries.
 *       No string-based field names that can have typos.</li>
 *   <li><b>Query By Example (QBE)</b> — create a "probe" object with the
 *       values you want, and Spring matches rows that look like it.</li>
 * </ol>
 *
 * <p><b>New Java 17 feature demonstrated:</b> Pattern Matching for {@code instanceof}
 * in {@link #describeFilter(Object)}.
 */
@Service
public class DynamicQueryService {

    private final CourseRepo         courseRepo;
    private final CourseQueryDslRepo queryDslRepo;

    public DynamicQueryService(CourseRepo courseRepo, CourseQueryDslRepo queryDslRepo) {
        this.courseRepo    = courseRepo;
        this.queryDslRepo  = queryDslRepo;
    }

    // =========================================================================
    // Strategy 1: JPA Specification API
    // =========================================================================

    /**
     * Finds courses using the JPA Specification API (dynamic WHERE clauses).
     *
     * <p>The lambda {@code (root, query, criteriaBuilder) -> ...} is a
     * {@link org.springframework.data.jpa.domain.Specification}.  It builds
     * SQL predicates programmatically: only the criteria that are present
     * in the filter are added to the WHERE clause.
     *
     * <p><b>Java 8 features:</b> Lambda expression, method reference, {@code Optional.ifPresent}.
     *
     * @param filter  the filter criteria (empty Optionals are ignored)
     * @return list of courses matching the filter
     */
    public List<Course> filterBySpecification(CourseFilter filter) {
        return courseRepo.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only add each predicate if the filter has a value for it.
            // ifPresent() is a Java 8 Optional method — runs the lambda only if value is present.
            filter.getDepartment().ifPresent(d ->
                    predicates.add(criteriaBuilder.equal(root.get("department"), d)));
            filter.getCredits().ifPresent(c ->
                    predicates.add(criteriaBuilder.equal(root.get("credits"), c)));
            filter.getInstructor().ifPresent(i ->
                    predicates.add(criteriaBuilder.equal(root.get("instructor"), i)));

            // Combine all predicates with AND.
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

    // =========================================================================
    // Strategy 2: QueryDSL
    // =========================================================================

    /**
     * Finds courses using QueryDSL's type-safe predicate builder.
     *
     * <p>{@code QCourse} is auto-generated at compile time from the {@link Course}
     * entity.  It gives you a strongly-typed "Q-object" where every field is
     * a predicate factory:
     * <ul>
     *   <li>{@code QCourse.course.credits.eq(3)} → {@code WHERE credits = 3}</li>
     *   <li>{@code QCourse.course.department.eq(d)} → {@code WHERE department_id = ?}</li>
     * </ul>
     *
     * @param filter  the filter criteria
     * @return list of courses matching the filter
     */
    public List<Course> filterByQueryDsl(CourseFilter filter) {
        QCourse qCourse = QCourse.course;
        BooleanBuilder pred = new BooleanBuilder();

        // Build the predicate by adding clauses only when filter has a value.
        filter.getDepartment().ifPresent(d -> pred.and(qCourse.department.eq(d)));
        filter.getCredits().ifPresent(c   -> pred.and(qCourse.credits.eq(c)));
        filter.getInstructor().ifPresent(i -> pred.and(qCourse.instructor.eq(i)));

        // Iterate the Iterable<Course> result and collect it into a List.
        List<Course> courses = new ArrayList<>();
        queryDslRepo.findAll(pred).forEach(courses::add);
        return courses;
    }

    // =========================================================================
    // Strategy 3: Query By Example (QBE)
    // =========================================================================

    /**
     * Finds courses using Query By Example (QBE).
     *
     * <p>Create a "probe" Course object with only the fields you want to match,
     * leave the rest as {@code null}.  Spring Data generates a WHERE clause
     * for each non-null field automatically.
     *
     * <p>Simple, but limited: QBE can't do {@code > <} comparisons or JOINs.
     * It's best for simple exact-match queries.
     *
     * @param filter  the filter criteria
     * @return list of courses matching the filter
     */
    public List<Course> filterByExample(CourseFilter filter) {
        Course probe = new Course(
                null,                                   // name  = null → not filtered
                filter.getCredits().orElse(null),       // credits (if present)
                filter.getInstructor().orElse(null),    // instructor (if present)
                filter.getDepartment().orElse(null)     // department (if present)
        );
        return courseRepo.findAll(Example.of(probe));
    }

    // =========================================================================
    // Java 17: Pattern Matching for instanceof
    // =========================================================================

    /**
     * Produces a human-readable description of what a filter is searching for.
     *
     * <p><b>Java 17 Feature: Pattern Matching for instanceof</b>
     * <br>Before Java 16, to safely cast an object you had to write:
     * <pre>{@code
     *   if (filterObject instanceof CourseFilter) {
     *       CourseFilter cf = (CourseFilter) filterObject;  // redundant cast
     *       ...
     *   }
     * }</pre>
     * With Java 17 pattern matching, the cast and variable binding happen in one step:
     * <pre>{@code
     *   if (filterObject instanceof CourseFilter cf) {
     *       // cf is automatically a CourseFilter, no cast needed
     *   }
     * }</pre>
     *
     * @param filterObject  any object (may or may not be a {@link CourseFilter})
     * @return a description of the active filters, or a fallback message
     */
    public String describeFilter(Object filterObject) {
        // Java 17: Pattern Matching for instanceof — cf is bound automatically
        if (filterObject instanceof CourseFilter cf) {
            var parts = new ArrayList<String>();
            // Java 8 method reference + lambda
            cf.getDepartment().ifPresent(d -> parts.add("department='" + d.getName() + "'"));
            cf.getCredits().ifPresent(c    -> parts.add("credits=" + c));
            cf.getInstructor().ifPresent(i -> parts.add(
                    "instructor='" + i.getMember().firstName() + " " + i.getMember().lastName() + "'"));
            return parts.isEmpty() ? "(no active filters)" : String.join(", ", parts);
        }
        // Called with something that is NOT a CourseFilter
        return "Unknown filter type: " + filterObject.getClass().getSimpleName();
    }
}
