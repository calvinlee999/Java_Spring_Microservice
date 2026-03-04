package com.example.university.repo;

import com.example.university.domain.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

/**
 * StaffRepo — Spring Data repository for {@link Staff} entities.
 *
 * <p>By extending {@link JpaRepository} we get 20+ database methods for free:
 * {@code save()}, {@code findAll()}, {@code findById()}, {@code deleteById()}, etc.
 * No implementation code is needed — Spring Data generates it at startup.
 *
 * <p>{@code @RepositoryRestResource} tells Spring Data REST to expose this
 * repository as a REST API automatically.
 * <ul>
 *   <li>{@code GET  /staff}        → list all staff members</li>
 *   <li>{@code GET  /staff/{id}}   → get one staff member</li>
 *   <li>{@code POST /staff}        → create a new staff member</li>
 *   <li>{@code DELETE /staff/{id}} → remove a staff member</li>
 * </ul>
 */
@RepositoryRestResource(collectionResourceRel = "staff", path = "staff")
public interface StaffRepo extends JpaRepository<Staff, Integer> {

    /**
     * Finds a staff member whose last name matches exactly.
     * Spring Data navigates the embedded {@link com.example.university.domain.Person}
     * record: {@code member.lastName = :lastName}.
     *
     * <p>This is a <b>Java 17 Text Block</b> demo — notice the triple quotes.
     * Without text blocks the JPQL would be a cramped single-line string;
     * the text block makes it read like real SQL.
     *
     * @param lastName  the exact last name to search (case-sensitive)
     * @return Optional containing the staff member, or empty if not found
     */
    @Query("""
            SELECT s
            FROM   Staff s
            WHERE  s.member.lastName = :lastName
            ORDER  BY s.member.firstName ASC
            """)
    List<Staff> findByMemberLastName(String lastName);

    /**
     * Finds a single staff member by their first and last name.
     * Useful for looking up a specific person without knowing their ID.
     *
     * @param firstName  first name
     * @param lastName   last name
     * @return Optional containing the matching staff member, or empty
     */
    @Query("""
            SELECT s
            FROM   Staff s
            WHERE  s.member.firstName = :firstName
              AND  s.member.lastName  = :lastName
            """)
    Optional<Staff> findByMemberFirstNameAndLastName(String firstName, String lastName);
}
