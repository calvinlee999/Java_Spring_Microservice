package com.example.university.business;

import com.example.university.domain.*;
import com.example.university.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.SequencedCollection;

/**
 * UniversityService — the main business logic layer of the University app.
 *
 * <p>This class acts as a middleman between the web layer (controllers)
 * and the data layer (repositories).  Controllers ask this service for
 * data; the service uses repos to fetch from the database.
 *
 * <p><b>@Transactional (principal engineer note):</b>
 * Every method that <em>writes</em> data ({@code create*}, {@code deleteAll})
 * is annotated with {@code @Transactional}.  This means:
 * <ul>
 *   <li>All DB operations in the method run inside a single transaction.</li>
 *   <li>If <em>anything</em> fails halfway through, the entire transaction is
 *       rolled back — no partial saves, no corrupted data.</li>
 *   <li>Read-only methods use {@code @Transactional(readOnly=true)}, which
 *       tells the JPA provider (Hibernate) and the connection pool (HikariCP)
 *       to skip dirty-checking and optimise for read throughput.</li>
 * </ul>
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
@Transactional(readOnly = true)   // All methods default to read-only; write methods override below
public class UniversityService {

    /**
     * SLF4J Logger — the standard logging facade used throughout Spring Boot apps.
     * Think of {@code log} as a professional notepad:
     * instead of {@code System.out.println("...")}, we call
     * {@code log.info("...")} so messages include a timestamp, log level,
     * and thread name — essential for debugging in Kubernetes where you
     * tail the pod logs.
     */
    private static final Logger log = LoggerFactory.getLogger(UniversityService.class);

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

    /**
     * Creates and saves a new student.
     * {@code @Transactional} ensures the save and any cascaded operations
     * either ALL succeed or ALL roll back — no half-saved data.
     */
    @Transactional
    public Student createStudent(String firstName, String lastName,
                                 boolean fullTime, int age) {
        log.info("Creating student: {} {}, fullTime={}, age={}", firstName, lastName, fullTime, age);
        return studentRepo.save(
                new Student(new Person(firstName, lastName), fullTime, age));
    }

    /**
     * Creates and saves a new staff member (professor or administrator).
     */
    @Transactional
    public Staff createFaculty(String firstName, String lastName) {
        log.info("Creating faculty: {} {}", firstName, lastName);
        return staffRepo.save(new Staff(new Person(firstName, lastName)));
    }

    /**
     * Creates and saves a new department with the given chair.
     */
    @Transactional
    public Department createDepartment(String name, Staff chair) {
        log.info("Creating department: '{}' chaired by {} {}",
                name, chair.getMember().firstName(), chair.getMember().lastName());
        return departmentRepo.save(new Department(name, chair));
    }

    /**
     * Creates and saves a new course with no prerequisites.
     */
    @Transactional
    public Course createCourse(String name, int credits,
                               Staff professor, Department department) {
        log.info("Creating course: '{}' ({} credits) in dept '{}'",
                name, credits, department.getName());
        return courseRepo.save(new Course(name, credits, professor, department));
    }

    /**
     * Creates a new course and links one or more prerequisite courses to it.
     * {@code @Transactional} guarantees the course AND all prerequisites are
     * saved together or not at all.
     */
    @Transactional
    public Course createCourse(String name, int credits, Staff professor,
                               Department department, Course... prereqs) {
        log.info("Creating course '{}' with {} prerequisite(s)", name, prereqs.length);
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
     * Proper SLF4J logging replaces {@code System.err.println}
     * so the message appears in structured log output (K8s pod logs, CloudWatch, etc.).
     */
    @Transactional
    public void deleteAll() {
        try {
            log.warn("deleteAll() called — removing all student records");
            studentRepo.deleteAll();
        } catch (Exception e) {
            log.error("deleteAll() failed: {}", e.getMessage(), e);
        }
    }
}
