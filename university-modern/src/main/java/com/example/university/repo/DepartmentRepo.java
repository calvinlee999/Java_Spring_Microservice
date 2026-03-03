package com.example.university.repo;

import com.example.university.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

/**
 * DepartmentRepo — Spring Data repository for {@link Department} entities.
 *
 * <p>Exposed via Spring Data REST at path {@code /departments}.
 * Append {@code ?projection=showChair} to any single department URL to see
 * only the name and chair name (see {@link com.example.university.domain.ShowChair}).
 */
@RepositoryRestResource(collectionResourceRel = "departments", path = "departments")
public interface DepartmentRepo extends JpaRepository<Department, Integer> {

    /**
     * Finds a department by its exact name.
     * Spring Data turns this method name into: {@code WHERE name = ?1}
     *
     * @param name  the department name to look up
     * @return an Optional containing the department, or empty if not found
     */
    Optional<Department> findByName(String name);

    /**
     * Finds all departments whose chair (leader) has the given last name.
     *
     * <p>Spring Data navigates the object graph automatically:
     * {@code ByChairMemberLastName} → {@code WHERE chair.member.last_name = ?1}.
     * Since {@code Person} is now a record, Hibernate maps {@code lastName()}
     * to the {@code last_name} column correctly.
     *
     * <p><b>Java 17 Text Block</b> used for the JPQL to show how multi-line
     * queries are written cleanly without concatenation.
     *
     * @param lastName  the chair's last name
     * @return matching departments
     */
    @Query("""
            SELECT d
            FROM   Department d
            WHERE  d.chair.member.lastName = :lastName
            """)
    List<Department> findByChairLastName(String lastName);
}
