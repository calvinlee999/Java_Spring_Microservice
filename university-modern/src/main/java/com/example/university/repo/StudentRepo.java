package com.example.university.repo;

import com.example.university.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * StudentRepo — Spring Data repository for {@link Student} entities.
 *
 * <p>Extends {@link JpaRepository} for free CRUD operations and exposes
 * them via {@code @RepositoryRestResource} at the path {@code /students}.
 */
@RepositoryRestResource(collectionResourceRel = "students", path = "students")
public interface StudentRepo extends JpaRepository<Student, Integer> {

    /**
     * Finds all students who are currently marked as full-time.
     *
     * <p>Spring Data derives the SQL automatically from the method name:
     * {@code findByFullTime} → {@code WHERE full_time = ?}. No SQL needed!
     *
     * @param fullTime  pass {@code true} for full-time, {@code false} for part-time
     * @return list of matching students
     */
    List<Student> findByFullTime(boolean fullTime);

    /**
     * Finds all students whose age is less than the given value.
     *
     * <p><b>Java 17 Feature: Text Block</b> — the JPQL query is written
     * as a text block (triple-quoted string).  Text blocks let you write
     * multi-line strings without string concatenation or escape characters.
     * They are easier to read, especially for SQL or JSON strings.
     *
     * @param age  the upper age limit (exclusive)
     * @return list of students younger than the given age
     */
    @Query("""
            SELECT s
            FROM   Student s
            WHERE  s.age < :age
            ORDER  BY s.age ASC
            """)
    List<Student> findYoungerThan(int age);

    /**
     * Finds all students who have a specific enrollment status string.
     * The status values are: {@code "ACTIVE"}, {@code "GRADUATED"}, {@code "SUSPENDED"}.
     *
     * <p>Text block used here to keep the JPQL readable across multiple lines.
     *
     * @param status  the status string to search for
     * @return list of students with that status
     */
    @Query("""
            SELECT s
            FROM   Student s
            WHERE  s.status = :status
            """)
    List<Student> findByStatus(String status);
}
