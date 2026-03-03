package com.example.university.business;

import com.example.university.domain.*;
import com.example.university.repo.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.SequencedCollection;

/**
 * UniversityService — the main business logic layer of the University app.
 *
 * <p>This class acts as a middleman between the web layer (controllers)
 * and the data layer (repositories).  Controllers ask this service for
 * data; the service uses repos to fetch from the database.
 *
 * <p><b>New Java features demonstrated in this class:</b>
 * <ul>
 *   <li><b>Java 21 — SequencedCollection:</b> {@link java.util.SequencedCollection} is a new
 *       interface added to all {@code List}, {@code Deque}, and {@code LinkedHashSet} types.
 *       It adds {@code getFirst()}, {@code getLast()}, and {@code reversed()} methods.</li>
 *   <li><b>Java 21 — Pattern Matching Switch (Sealed + Records):</b>
 *       The {@link #describeEnrollment} method uses a {@code switch} expression that
 *       deconstructs record patterns from the sealed {@link EnrollmentStatus} interface.
 *       The compiler guarantees all cases are covered — no default branch needed.</li>
 *   <li><b>Java 17 — Pattern Matching for instanceof:</b>
 *       {@link #toEnrollmentStatus} demonstrates the concise instanceof-pattern cast.</li>
 * </ul>
 */
@Service
public class UniversityService {

    private final DepartmentRepo departmentRepo;
    private final StaffRepo      staffRepo;
    private final StudentRepo    studentRepo;
    private final CourseRepo     courseRepo;

    public UniversityService(CourseRepo courseRepo,
                              DepartmentRepo departmentRepo,
                              StaffRepo staffRepo,
                              StudentRepo studentRepo) {
        this.courseRepo     = courseRepo;
        this.departmentRepo = departmentRepo;
        this.staffRepo      = staffRepo;
        this.studentRepo    = studentRepo;
    }

    // -------------------------------------------------------------------------
    // Create methods — save new entities to the database
    // -------------------------------------------------------------------------

    /** Creates and saves a new student. The {@link Person} record holds the name. */
    public Student createStudent(String firstName, String lastName,
                                 boolean fullTime, int age) {
        return studentRepo.save(
                new Student(new Person(firstName, lastName), fullTime, age));
    }

    /** Creates and saves a new staff member (professor or administrator). */
    public Staff createFaculty(String firstName, String lastName) {
        return staffRepo.save(new Staff(new Person(firstName, lastName)));
    }

    /** Creates and saves a new department with the given chair. */
    public Department createDepartment(String name, Staff chair) {
        return departmentRepo.save(new Department(name, chair));
    }

    /** Creates and saves a new course with no prerequisites. */
    public Course createCourse(String name, int credits,
                               Staff professor, Department department) {
        return courseRepo.save(new Course(name, credits, professor, department));
    }

    /** Creates a new course and links prerequisite courses to it. */
    public Course createCourse(String name, int credits, Staff professor,
                               Department department, Course... prereqs) {
        Course c = new Course(name, credits, professor, department);
        for (Course p : prereqs) {
            c.addPrerequisite(p);
        }
        return courseRepo.save(c);
    }

    // -------------------------------------------------------------------------
    // Read methods — fetch data from the database
    // -------------------------------------------------------------------------

    public List<Course>     findAllCourses()     { return courseRepo.findAll(); }
    public List<Staff>      findAllStaff()       { return staffRepo.findAll(); }
    public List<Department> findAllDepartments() { return departmentRepo.findAll(); }
    public List<Student>    findAllStudents()    { return studentRepo.findAll(); }

    // -------------------------------------------------------------------------
    // Java 21 — SequencedCollection API
    // -------------------------------------------------------------------------

    /**
     * Returns all courses in a reversed order (highest ID first).
     *
     * <p><b>Java 21 Feature: SequencedCollection</b>
     * {@code List} now implements {@link SequencedCollection}, which adds:
     * <ul>
     *   <li>{@code list.getFirst()} — first element (replaces {@code list.get(0)})</li>
     *   <li>{@code list.getLast()}  — last element (replaces {@code list.get(list.size()-1)})</li>
     *   <li>{@code list.reversed()} — an unmodifiable reversed view of the list</li>
     * </ul>
     * The old way: {@code Collections.reverse(list)} mutates the list in place.
     * The new way: {@code list.reversed()} gives you a reversed view without mutation.
     *
     * @return a SequencedCollection of all courses in reversed (last-to-first) order
     */
    public SequencedCollection<Course> findCoursesReversed() {
        List<Course> all = courseRepo.findAll();
        // Java 21 — reversed() returns a non-destructive reversed view of the list.
        return all.reversed();
    }

    /**
     * Returns the first course alphabetically by name.
     *
     * <p><b>Java 21 Feature: SequencedCollection.getFirst()</b>
     * Cleaner and more expressive than {@code courses.get(0)}.
     *
     * @return the alphabetically first course
     */
    public Course findFirstCourse() {
        List<Course> sorted = courseRepo.findAll(Sort.by("name"));
        // Java 21 — getFirst() is self-documenting unlike get(0)
        return sorted.getFirst();
    }

    /**
     * Returns the last course alphabetically by name.
     *
     * <p><b>Java 21 Feature: SequencedCollection.getLast()</b>
     *
     * @return the alphabetically last course
     */
    public Course findLastCourse() {
        List<Course> sorted = courseRepo.findAll(Sort.by("name"));
        // Java 21 — getLast() instead of courses.get(courses.size() - 1)
        return sorted.getLast();
    }

    // -------------------------------------------------------------------------
    // Java 21 — Sealed Interface + Record Pattern Matching Switch
    // -------------------------------------------------------------------------

    /**
     * Produces a human-readable summary of a student's enrollment status.
     *
     * <p><b>Java 21 Feature: Pattern Matching for Switch + Record Deconstruction</b>
     * The {@code switch} expression matches against each sealed type AND
     * simultaneously destructures the record component into a local variable.
     *
     * <p>Example:
     * <pre>{@code
     *   case EnrollmentStatus.Active(var sem)
     * }</pre>
     * This means: "if {@code status} is an {@code Active} record, bind its
     * {@code semester} component to the variable {@code sem}".
     *
     * <p>Because {@code EnrollmentStatus} is sealed, the compiler knows there
     * are exactly three cases.  No {@code default} needed — it's exhaustive.
     *
     * @param status  a sealed {@link EnrollmentStatus} instance
     * @return a friendly description string
     */
    public String describeEnrollment(EnrollmentStatus status) {
        // Java 21: pattern-matching switch over a sealed interface
        // Each case deconstructs the record component automatically.
        return switch (status) {
            case EnrollmentStatus.Active    s -> "Currently enrolled in semester: " + s.semester();
            case EnrollmentStatus.Graduated s -> "Graduated in " + s.graduationYear();
            case EnrollmentStatus.Suspended s -> "Suspended — reason: " + s.reason();
        };
    }

    /**
     * Converts a raw status string from the database into a typed
     * {@link EnrollmentStatus} sealed interface instance.
     *
     * <p><b>Java 17 Feature: Pattern Matching for instanceof</b>
     * The guard condition {@code case "ACTIVE" -> ...} in the switch
     * is a Java 14+ switch expression (also demonstrated here).
     *
     * @param student  the student whose status string should be converted
     * @param detail   extra context: semester name, graduation year, or reason
     * @return the matching {@link EnrollmentStatus} implementation
     */
    public EnrollmentStatus toEnrollmentStatus(Student student, String detail) {
        String rawStatus = student.getStatus();

        // Java 17 Pattern Matching for instanceof:
        // Old: if (rawStatus instanceof String) { String s = (String) rawStatus; ... }
        // New: the variable 's' is automatically bound and available in the block.
        if (rawStatus instanceof String s) {
            return switch (s.toUpperCase()) {
                case "ACTIVE"    -> new EnrollmentStatus.Active(detail);
                case "GRADUATED" -> new EnrollmentStatus.Graduated(Integer.parseInt(detail));
                case "SUSPENDED" -> new EnrollmentStatus.Suspended(detail);
                default          -> throw new IllegalArgumentException(
                        "Unknown enrollment status: '" + s + "'");
            };
        }
        throw new IllegalStateException("Student status is null for student: " + student);
    }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    /**
     * Deletes all student records (used in tests to reset state).
     * Wrapped in try/catch because cascade constraints can sometimes cause issues.
     */
    public void deleteAll() {
        try {
            studentRepo.deleteAll();
        } catch (Exception e) {
            System.err.println("Could not delete all students: " + e.getMessage());
        }
    }
}
