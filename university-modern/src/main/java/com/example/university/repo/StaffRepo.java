package com.example.university.repo;

import com.example.university.domain.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

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
 *   <li>{@code GET  /staff}     → list all staff members</li>
 *   <li>{@code GET  /staff/{id}} → get one staff member</li>
 *   <li>{@code POST /staff}     → create a new staff member</li>
 *   <li>{@code DELETE /staff/{id}} → remove a staff member</li>
 * </ul>
 */
@RepositoryRestResource(collectionResourceRel = "staff", path = "staff")
public interface StaffRepo extends JpaRepository<Staff, Integer> {
    // Spring Data generates all CRUD methods automatically.
    // No need to write any SQL or method bodies here!
}
