package com.example.university.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Student — a JPA entity representing one student enrolled at the university.
 *
 * <p>The student's name is stored via the {@link Person} record ({@code @Embeddable}).
 * <br>The {@code status} column stores the enrollment state as a plain string
 * ({@code "ACTIVE"}, {@code "GRADUATED"}, or {@code "SUSPENDED"}).  In the service
 * layer this string is converted to an {@link com.example.university.domain.EnrollmentStatus}
 * sealed interface instance and processed with a Java 21 pattern-matching switch.
 *
 * <p><b>@Version (Optimistic Locking):</b>
 * Prevents two transactions from updating the same student record at the same time
 * and losing one of the changes.  Especially important for enrollment status updates
 * in a cloud environment where multiple pods may handle requests concurrently.
 *
 * <p>Table: {@code student}
 */
@Entity
@Table(name = "student")
public class Student {

    /** Auto-incremented database ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer studentId;

    /**
     * Optimistic locking version counter — auto-managed by JPA.
     * See {@link Course#getVersion()} for a full explanation.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * The student's name — embedded from the Java 17 {@link Person} record.
     * Maps to columns {@code first_name} and {@code last_name} in the {@code student} table.
     */
    @Embedded
    private Person attendee;

    /** {@code true} = full-time student, {@code false} = part-time. */
    @Column(name = "full_time")
    private boolean fullTime;

    /**
     * Student's age in years.  Must be ≥ 0 — a negative age is not valid.
     */
    @Min(value = 0, message = "Age must be 0 or greater")
    @NotNull(message = "Age is required")
    @Column(name = "age", nullable = false)
    private Integer age;

    /**
     * Enrollment state: one of {@code ACTIVE}, {@code GRADUATED}, {@code SUSPENDED}.
     * Stored as a VARCHAR so it is human-readable in the database.
     * Converted to {@link EnrollmentStatus} in the service layer for pattern matching.
     */
    @Column(name = "status")
    private String status = "ACTIVE";

    /**
     * The courses this student is currently enrolled in.
     * Uses EAGER loading so courses are always fetched with the student.
     * CascadeType.ALL means if a student is deleted, their enrollment records go too.
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
        name = "course_student",
        joinColumns         = {@JoinColumn(name = "student_id")},
        inverseJoinColumns  = {@JoinColumn(name = "course_id")}
    )
    private List<Course> courses = new ArrayList<>();

    /**
     * Creates a new student with the given personal details.
     *
     * @param attendee  a {@link Person} record (first + last name)
     * @param fullTime  whether the student attends full-time
     * @param age       the student's age (must be ≥ 0)
     */
    public Student(Person attendee, boolean fullTime, Integer age) {
        this.attendee = attendee;
        this.fullTime = fullTime;
        this.age = age;
    }

    /** Required by JPA. */
    protected Student() {}

    public Integer getStudentId()    { return studentId; }
    public Long    getVersion()      { return version; }
    public Person  getAttendee()     { return attendee; }
    public boolean isFullTime()      { return fullTime; }
    public Integer getAge()          { return age; }
    public String  getStatus()       { return status; }
    public List<Course> getCourses() { return courses; }

    public void setAge(Integer age)      { this.age = age; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Student{id=" + studentId + ", " + attendee
                + ", fullTime=" + fullTime + ", age=" + age
                + ", status='" + status + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return studentId.equals(((Student) o).studentId);
    }

    @Override
    public int hashCode() { return Objects.hash(studentId); }
}
