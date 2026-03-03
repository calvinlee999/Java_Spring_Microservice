package com.example.university.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Person — a simple value object holding a first and last name.
 *
 * <p><b>Java 17 Feature: Record</b>
 * <br>Before Java 16, you needed ~50 lines to write a value class:
 * private fields, a constructor, getters, equals(), hashCode(), toString().
 * A {@code record} does all of that in ONE line automatically.
 *
 * <p>The compiler auto-generates:
 * <ul>
 *   <li>{@code firstName()} and {@code lastName()} accessor methods</li>
 *   <li>{@code equals()} / {@code hashCode()} based on component values</li>
 *   <li>{@code toString()} like {@code Person[firstName=Jane, lastName=Doe]}</li>
 * </ul>
 *
 * <p><b>JPA / Hibernate 6 note:</b>
 * {@code @Embeddable} means this record is not a table itself — its two
 * columns ({@code first_name}, {@code last_name}) live inside the
 * owning entity's table (e.g. {@code staff_member}).
 * Hibernate 6 (used by Spring Boot 3.x) fully supports records as
 * embeddable types; no no-arg constructor is needed.
 *
 * @param firstName  the person's first name, stored in column {@code first_name}
 * @param lastName   the person's last name,  stored in column {@code last_name}
 */
@Embeddable
public record Person(
        @Column(name = "first_name") String firstName,
        @Column(name = "last_name")  String lastName
) {
    // No boilerplate needed — the record does it all!
    // Access names with: person.firstName()  and  person.lastName()
}
