package com.example.university.domain;

import jakarta.persistence.*;
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
 * <p>Table: {@code course}
 */
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** Name of the course, e.g. {@code "Java Programming"}. */
    @Column(name = "name")
    private String name;

    /** Number of credit hours this course is worth. */
    @Column(name = "credits")
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
     * @param name        course title
     * @param credits     credit-hour value
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
