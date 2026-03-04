package com.example.university.domain;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Staff — a JPA entity representing one university staff member
 * (professor, lecturer, or administrator).
 *
 * <p>The staff member's name is stored via the {@link Person} record.
 * Because {@code Person} is {@code @Embeddable}, Hibernate maps its
 * two columns ({@code first_name}, {@code last_name}) directly into
 * the {@code staff_member} table — no separate join or table needed.
 *
 * <p><b>@Version (Optimistic Locking):</b>
 * Prevents concurrent updates from overwriting each other.
 * Works the same way across all cloud-managed PostgreSQL services
 * (AWS RDS/Aurora, Azure Flexible Server, GCP Cloud SQL/AlloyDB)
 * because it's a standard JPA feature, not a cloud-specific SQL extension.
 *
 * <p>Table: {@code staff_member}
 */
@Entity
@Table(name = "staff_member")
public class Staff {

    /** Auto-incremented primary key — the database assigns this number. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Optimistic locking version counter — auto-managed by JPA.
     * See {@link Course#getVersion()} for a full explanation.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * The staff member's name.
     * Stored as embedded columns in the same {@code staff_member} row.
     * Now backed by a Java 17 {@code record} instead of a plain class.
     */
    @Embedded
    private Person member;

    /**
     * Constructor used by application code to create a new staff member.
     *
     * @param member  a {@link Person} record containing first and last name
     */
    public Staff(Person member) {
        this.member = member;
    }

    /** Required by JPA — Hibernate uses this when loading rows from the DB. */
    protected Staff() {}

    /** @return the {@link Person} record containing this staff member's name */
    public Person  getMember()  { return member; }

    /** @return the auto-assigned database ID */
    public Integer getId()      { return id; }

    /** @return the optimistic locking version (managed by JPA) */
    public Long    getVersion() { return version; }

    @Override
    public String toString() {
        return "Staff{id=" + id + ", member=" + member + '}';
    }

    /** Two Staff objects are equal when they share the same database ID. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((Staff) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
