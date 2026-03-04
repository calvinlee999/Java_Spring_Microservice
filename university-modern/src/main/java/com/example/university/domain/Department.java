package com.example.university.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Department — a JPA entity representing one academic department
 * (e.g. Humanities, Natural Sciences, Social Sciences).
 *
 * <p>Each department has a {@link Staff} member who acts as its chair
 * (the professor who leads the department).
 *
 * <p>The {@link com.example.university.domain.ShowChair} Spring Data projection
 * can be used to load only the name and chair name without fetching
 * the full Staff object — handy for lightweight list endpoints.
 *
 * <p><b>@Version (Optimistic Locking):</b>
 * If two admin users try to rename a department at the same time, JPA's
 * version counter guarantees one of them gets an error instead of silently
 * losing their work.  No cloud-specific SQL needed — it works on RDS,
 * Flexible Server, and Cloud SQL identically.
 *
 * <p>Table: {@code department}
 */
@Entity
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Optimistic locking version counter — auto-managed by JPA.
     * See {@link Course#getVersion()} for a full explanation.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Human-readable department name, e.g. {@code "Humanities"}.
     * Must not be blank — a nameless department would be confusing!
     */
    @NotBlank(message = "Department name must not be blank")
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The staff member who chairs (leads) this department.
     * Mapped via a foreign key column {@code chair_id} pointing to {@code staff_member.id}.
     */
    @ManyToOne
    @JoinColumn(name = "chair_id")
    private Staff chair;

    public Department(String name, Staff chair) {
        this.name  = name;
        this.chair = chair;
    }

    /** Required by JPA. */
    protected Department() {}

    public Integer getId()      { return id; }
    public Long    getVersion() { return version; }
    public String  getName()    { return name; }
    public Staff   getChair()   { return chair; }

    public void setName(String name)   { this.name = name; }
    public void setChair(Staff chair)  { this.chair = chair; }

    @Override
    public String toString() {
        return "Department{id=" + id + ", name='" + name + "', chair=" + chair + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((Department) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
