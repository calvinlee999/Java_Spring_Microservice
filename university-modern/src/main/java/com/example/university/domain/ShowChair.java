package com.example.university.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

/**
 * ShowChair — a Spring Data REST <b>Projection</b>.
 *
 * <p>A projection is like a camera filter — instead of loading the
 * entire {@link Department} object (with all its nested data), this
 * interface tells Spring Data to return only two fields: the department
 * name and the chair's full name.
 *
 * <p>Usage: append {@code ?projection=showChair} to any department URL.
 * <br>Example: {@code GET /departments/21?projection=showChair}
 * <br>Response: {@code { "name": "Humanities", "chairName": "Dean Jones" }}
 *
 * <p>The {@code @Value} SpEL expression reads into the nested object graph:
 * {@code target} is the {@link Department}, {@code .chair} is its {@link Staff},
 * {@code .member} is the {@link Person} record, and {@code .firstName()} / {@code .lastName()}
 * are the record's accessor methods. Spring 6 resolves record accessors in SpEL.
 */
@Projection(name = "showChair", types = {Department.class})
public interface ShowChair {

    /** Returns the department's name (e.g. {@code "Humanities"}). */
    String getName();

    /**
     * Combines the chair's first and last name into one string.
     * SpEL {@code #{...}} reads deeply into the object graph at runtime.
     *
     * @return full name of the department chair, e.g. {@code "Dean Jones"}
     */
    @Value("#{target.chair.member.firstName()} #{target.chair.member.lastName()}")
    String getChairName();
}
