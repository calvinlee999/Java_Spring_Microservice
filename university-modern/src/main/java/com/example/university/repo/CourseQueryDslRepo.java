package com.example.university.repo;

import com.example.university.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * CourseQueryDslRepo — a Spring Data repository that supports QueryDSL predicates.
 *
 * <p>By adding {@link QuerydslPredicateExecutor}, this repo gains the ability
 * to accept type-safe {@code Predicate} objects built with the auto-generated
 * {@code QCourse} class.
 *
 * <p>How QueryDSL works (step by step):
 * <ol>
 *   <li>At compile time, the QueryDSL APT processor reads {@code @Entity} classes
 *       and generates a {@code QCourse} class with a field for every attribute.</li>
 *   <li>In service code, you build a predicate like:
 *       {@code QCourse.course.credits.eq(3).and(QCourse.course.department.eq(dept))}</li>
 *   <li>Pass that predicate to {@code findAll(predicate)} and Spring Data
 *       translates it to the correct SQL WHERE clause.</li>
 * </ol>
 *
 * <p>No SQL strings, no typos, full IDE auto-complete — that's the power of QueryDSL.
 *
 * @see com.example.university.business.DynamicQueryService
 */
public interface CourseQueryDslRepo
        extends JpaRepository<Course, Integer>,
                QuerydslPredicateExecutor<Course> {
    // QuerydslPredicateExecutor adds findAll(Predicate), findOne(Predicate),
    // count(Predicate), and exists(Predicate) — all for free!
}
