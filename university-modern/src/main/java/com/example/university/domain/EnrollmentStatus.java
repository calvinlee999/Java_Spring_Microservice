package com.example.university.domain;

/**
 * EnrollmentStatus — describes every possible state a student can be in.
 *
 * <p><b>Java 17 Feature: Sealed Interface</b>
 * <br>A {@code sealed} interface is like a closed club — only the classes
 * listed in the {@code permits} clause are allowed to implement it.
 * No other class anywhere in the codebase can create a new status.
 *
 * <p>Why is that useful?
 * <ul>
 *   <li>The compiler knows ALL possible cases, so a {@code switch} expression
 *       that handles all three is <em>exhaustive</em> — no default branch needed.</li>
 *   <li>Adding a new status (e.g. {@code OnLeave}) forces the developer to
 *       update every switch that uses it.  No accidental forgotten cases.</li>
 * </ul>
 *
 * <p>Each permitted type is also a {@code record} (Java 17), making them
 * immutable data containers with zero boilerplate.
 *
 * <p><b>Used in:</b> {@link com.example.university.business.UniversityService#describeEnrollment}
 * with a Java 21 pattern-matching switch expression.
 */
public sealed interface EnrollmentStatus
        permits EnrollmentStatus.Active,
                EnrollmentStatus.Graduated,
                EnrollmentStatus.Suspended {

    /**
     * Active: the student is currently attending classes.
     *
     * @param semester  the current semester, e.g. {@code "Fall 2025"}
     */
    record Active(String semester) implements EnrollmentStatus {}

    /**
     * Graduated: the student has successfully completed all requirements.
     *
     * @param graduationYear  the year they graduated, e.g. {@code 2024}
     */
    record Graduated(int graduationYear) implements EnrollmentStatus {}

    /**
     * Suspended: the student's enrollment is temporarily paused.
     *
     * @param reason  a short explanation, e.g. {@code "Unpaid tuition"}
     */
    record Suspended(String reason) implements EnrollmentStatus {}
}
