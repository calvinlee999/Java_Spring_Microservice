package com.example.university.business;

import com.example.university.domain.Course;
import com.example.university.domain.Department;
import com.example.university.domain.Staff;

import java.util.Optional;

/**
 * CourseFilter — a simple filter object that holds search criteria for courses.
 *
 * <p>Each field is wrapped in an {@link Optional}.  When a field is empty
 * ({@code Optional.empty()}), it means "don't filter by this field".
 * When it has a value, we only return courses that match.
 *
 * <p><b>Java 8 Feature: Optional</b>
 * {@code Optional<T>} is a box that either holds a value or is empty.
 * It forces callers to think about the "no value" case instead of
 * getting a surprise {@code NullPointerException}.
 *
 * <p>Usage example:
 * <pre>{@code
 *   CourseFilter filter = CourseFilter.filterBy()
 *       .department(scienceDept)
 *       .credits(3);
 *   // instructor is not set, so all instructors are included
 * }</pre>
 */
public class CourseFilter {

    /** Filter by department — empty means "any department". */
    private Optional<Department> department = Optional.empty();

    /** Filter by credit-hour count — empty means "any credits". */
    private Optional<Integer> credits = Optional.empty();

    /** Filter by instructor — empty means "any instructor". */
    private Optional<Staff> instructor = Optional.empty();

    /**
     * Filter by a partial course name (case-insensitive contains match).
     * Empty means "any name".
     *
     * <p>Example: setting {@code nameLike} to {@code "eng"} would match
     * "English Literature", "Engineering Math", etc.
     * The database query uses SQL {@code LIKE '%eng%'} (case-insensitive).
     */
    private Optional<String> nameLike = Optional.empty();

    /**
     * Factory method — creates a blank filter as the starting point.
     * Use the fluent setters below to add criteria.
     *
     * @return a new empty {@code CourseFilter}
     */
    public static CourseFilter filterBy() {
        return new CourseFilter();
    }

    /** Set the department to filter by. Returns {@code this} for fluent chaining. */
    public CourseFilter department(Department department) {
        this.department = Optional.of(department);
        return this;
    }

    /** Set the credit-hour count to filter by. Returns {@code this} for fluent chaining. */
    public CourseFilter credits(Integer credits) {
        this.credits = Optional.of(credits);
        return this;
    }

    /** Set the instructor to filter by. Returns {@code this} for fluent chaining. */
    public CourseFilter instructor(Staff instructor) {
        this.instructor = Optional.of(instructor);
        return this;
    }

    /**
     * Set a partial name search term (case-insensitive).
     * Returns {@code this} for fluent chaining.
     *
     * @param nameLike  the substring to search for within course names
     */
    public CourseFilter nameLike(String nameLike) {
        this.nameLike = Optional.ofNullable(nameLike);
        return this;
    }

    public Optional<Department> getDepartment()  { return department; }
    public Optional<Integer>    getCredits()      { return credits; }
    public Optional<Staff>      getInstructor()   { return instructor; }
    public Optional<String>     getNameLike()     { return nameLike; }

    /**
     * Tests whether a specific course satisfies all the criteria in this filter.
     *
     * <p><b>Java 8 Feature: {@code Optional.map().orElse()}</b>
     * {@code instructor.map(i -> course.getInstructor().equals(i)).orElse(true)}
     * means: "if an instructor filter is set, check it; otherwise pass through".
     *
     * @param course  the course to evaluate
     * @return {@code true} if the course matches all set criteria
     */
    public boolean meetsCriteria(Course course) {
        boolean nameOk = nameLike
                .map(n -> course.getName().toLowerCase().contains(n.toLowerCase()))
                .orElse(true);
        return nameOk
            && instructor.map(i -> course.getInstructor().equals(i)).orElse(true)
            && credits.map(c -> course.getCredits().equals(c)).orElse(true)
            && department.map(d -> course.getDepartment().equals(d)).orElse(true);
    }
}
