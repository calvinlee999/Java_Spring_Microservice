package com.example.university.repo;

import com.example.university.domain.Course;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

/**
 * CourseRepo — Spring Data repository for {@link Course} entities.
 *
 * <p>This repo has three superpowers:
 * <ol>
 *   <li>{@link JpaRepository} — free CRUD + paging + sorting</li>
 *   <li>{@link JpaSpecificationExecutor} — dynamic queries via the Specification pattern
 *       (used in {@link com.example.university.business.DynamicQueryService})</li>
 *   <li>{@code @RepositoryRestResource} — auto-REST at {@code /courses}</li>
 * </ol>
 *
 * <p><b>Java 17 Feature: Text Blocks</b>
 * All custom {@code @Query} methods use triple-quoted ({@code """..."""}) text blocks
 * for multi-line JPQL.  Compare this to the old way:
 * <pre>{@code
 * @Query("SELECT c FROM Course c WHERE c.name = :name")
 * }</pre>
 * vs the modern readable approach:
 * <pre>{@code
 * @Query("""
 *         SELECT c
 *         FROM   Course c
 *         WHERE  c.name = :name
 *         """)
 * }</pre>
 */
@RepositoryRestResource(collectionResourceRel = "courses", path = "courses")
public interface CourseRepo
        extends JpaRepository<Course, Integer>,
                JpaSpecificationExecutor<Course> {

    /**
     * Finds a course by its exact name.
     * Returns an {@link Optional} so callers handle "course not found" cleanly.
     *
     * @param name  exact course title
     * @return Optional containing the course, or empty
     */
    Optional<Course> findByName(String name);

    /**
     * Finds all courses that list a specific course as a prerequisite.
     * E.g. find all courses that require "English 101".
     *
     * @param prerequisite  the course to check
     * @return list of courses that have it as a prerequisite
     */
    List<Course> findByPrerequisites(Course prerequisite);

    /**
     * Finds all courses with a specific credit-hour value.
     *
     * <p><b>Text Block demo —</b> same query, totally readable.
     *
     * @param credits  the credit-hour count to filter by
     * @return matching courses
     */
    @Query("""
            SELECT c
            FROM   Course c
            WHERE  c.credits = :credits
            ORDER  BY c.name ASC
            """)
    List<Course> findByCredits(int credits);

    /**
     * Finds all courses whose department is chaired by a person with the given last name.
     * Spring Data navigates: {@code department → chair → member → lastName}.
     *
     * @param chair  the chair's last name
     * @return matching courses
     */
    @Query("""
            SELECT c
            FROM   Course c
            WHERE  c.department.chair.member.lastName = :chair
            """)
    List<Course> findByDepartmentChairMemberLastName(String chair);

    /**
     * Returns every course that offers more credits than the given threshold.
     * Useful for finding "heavy" courses when advising students.
     *
     * @param credits  the lower bound (exclusive)
     * @return courses with more credits than the threshold
     */
    @Query("""
            SELECT c
            FROM   Course c
            WHERE  c.credits > :credits
            ORDER  BY c.credits DESC, c.name ASC
            """)
    List<Course> findCoursesWithMoreThan(int credits);
}
