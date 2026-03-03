package com.example.university.domain;

import jakarta.persistence.*;
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
 * <p>Table: {@code department}
 */
@Entity
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** Human-readable department name, e.g. {@code "Humanities"}. */
    @Column(name = "name")
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

    public Integer getId()   { return id; }
    public String  getName() { return name; }
    public Staff   getChair(){ return chair; }

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
