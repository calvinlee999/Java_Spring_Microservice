package com.example.university.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Course — a JPA entity representing one course offered at the university.
 *
 * <p>A course belongs to a {@link Department} and is taught by a
 * {@link Staff} member (the instructor).  It may also have pre-requisite
 * courses that must be completed before enrolling.
 *
 * <p><b>Principal database architect notes:</b>
 * <ul>
 *   <li><b>Optimistic Locking ({@code @Version})</b> — the {@code version}
 *       column prevents the "lost update" problem.  Imagine two admin users
 *       open the same course record:  the second save will fail with an
 *       {@code OptimisticLockException} instead of silently overwriting the
 *       first user's changes.  This is safer than pessimistic locking
 *       (which blocks rows) and works identically across RDS, Flexible Server,
 *       and Cloud SQL because it is pure JPA — no cloud-specific SQL.</li>
 *   <li><b>Bean Validation ({@code @NotBlank}, {@code @Min})</b> — constraints
 *       are enforced at the Java layer BEFORE anything touches the database.
 *       A 400 Bad Request is returned if validation fails, protecting the DB
 *       from invalid data without needing DB-level check constraints.</li>
 * </ul>
 *
 * <p>Table: {@code course}
 */
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Optimistic locking version counter — JPA manages this automatically.
     *
     * <p><b>How it works (8th-grade analogy):</b>
     * Think of {@code version} as a document edition number.
     * When you save v3, JPA checks: "is the row still at v3?"  If yes,
     * it saves and bumps to v4.  If someone else already saved v4, your
     * save fails with an error — you must refresh and try again.
     * This prevents two people from accidentally erasing each other's work.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Name of the course, e.g. {@code "Java Programming"}.
     * Must not be blank — enforced by Bean Validation before the DB is touched.
     */
    @NotBlank(message = "Course name must not be blank")
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Number of credit hours this course is worth.
     * Must be at least 1 — a zero-credit course makes no academic sense.
     */
    @Min(value = 1, message = "A course must be worth at least 1 credit")
    @NotNull(message = "Credit hours are required")
    @Column(name = "credits", nullable = false)
    private Integer credits;

    /**
     * The staff member who teaches this course.
     * Foreign key: {@code course.instructor_id} → {@code staff_member.id}.
     */
    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Staff instructor;

    /**
     * List of courses that must be completed before taking this one.
     * Self-referencing many-to-many relationship via join table
     * {@code course_prerequisites}.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "course_prerequisites",
        joinColumns        = @JoinColumn(name = "id_of_course"),
        inverseJoinColumns = @JoinColumn(name = "id_prerequisite_course")
    )
    private List<Course> prerequisites = new ArrayList<>();

    /**
     * The department that offers this course.
     * Foreign key: {@code course.department_id} → {@code department.id}.
     */
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    /**
     * Creates a new course with the minimum required details.
     *
     * @param name        course title (must not be blank)
     * @param credits     credit-hour value (must be ≥ 1)
     * @param instructor  the teaching staff member
     * @param department  the owning department
     */
    public Course(String name, Integer credits, Staff instructor, Department department) {
        this.name        = name;
        this.credits     = credits;
        this.instructor  = instructor;
        this.department  = department;
    }

    /** Required by JPA. */
    protected Course() {}

    public Integer          getId()            { return id; }
    public Long             getVersion()       { return version; }
    public String           getName()          { return name; }
    public Integer          getCredits()       { return credits; }
    public Staff            getInstructor()    { return instructor; }
    public Department       getDepartment()    { return department; }
    public List<Course>     getPrerequisites() { return prerequisites; }

    /**
     * Adds a prerequisite course and returns {@code this} for fluent chaining.
     * Example: {@code course.addPrerequisite(intro).addPrerequisite(math);}
     *
     * @param prerequisite  the course to add as a prerequisite
     * @return this course (for chaining)
     */
    public Course addPrerequisite(Course prerequisite) {
        prerequisites.add(prerequisite);
        return this;
    }

    @Override
    public String toString() {
        return "Course{id=" + id + ", name='" + name + "', credits=" + credits + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Course) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}

    /** Required by JPA. */
    protected Course() {}

    public Integer          getId()            { return id; }
    public String           getName()          { return name; }

    /**
     * Adds a prerequisite course and returns {@code this} for fluent chaining.
     * Example: {@code course.addPrerequisite(intro).addPrerequisite(math);}
     *
     * @param prerequisite  the course to add as a prerequisite
     * @return this course (for chaining)
     */
    public Course addPrerequisite(Course prerequisite) {
        prerequisites.add(prerequisite);
        return this;
    }

    @Override
    public String toString() {
        return "Course{id=" + id + ", name='" + name + "', credits=" + credits + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Course) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
