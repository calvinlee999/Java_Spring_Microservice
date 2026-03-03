package com.example.university.dao;

import com.example.university.business.UniversityService;
import com.example.university.domain.*;
import com.example.university.repo.CourseRepo;
import com.example.university.repo.DepartmentRepo;
import com.example.university.repo.StaffRepo;
import com.example.university.repo.StudentRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SimpleDBCrudTest — verifies that the basic create-read-update-delete (CRUD)
 * operations work correctly with the H2 in-memory database.
 *
 * <p><b>How this test works:</b>
 * <ol>
 *   <li>{@code @SpringBootTest} starts the full Spring application context using
 *       the test application.properties (H2 instead of PostgreSQL).</li>
 *   <li>Hibernate creates all tables (DDL = create-drop).</li>
 *   <li>Spring runs data.sql to populate the tables with test data.</li>
 *   <li>Each test method is {@code @Transactional}, meaning all database changes
 *       made during the test are <em>rolled back</em> afterwards — the next
 *       test always starts with the same clean data.</li>
 * </ol>
 */
@SpringBootTest
@Transactional
class SimpleDBCrudTest {

    @Autowired
    private UniversityService universityService;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private StaffRepo staffRepo;

    @Autowired
    private StudentRepo studentRepo;

    // -------------------------------------------------------------------------
    // Staff tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("All 11 staff members are loaded from data.sql")
    void allStaffAreLoaded() {
        List<Staff> staff = universityService.findAllStaff();
        // We inserted 11 staff rows in data.sql
        assertThat(staff).hasSize(11);
    }

    @Test
    @DisplayName("Create a new staff member and find it by ID")
    void createAndFindStaff() {
        // Java 17 Record: Person is now a record — created with a compact constructor call
        Staff newProfessor = universityService.createFaculty("Ada", "Lovelace");

        assertThat(newProfessor.getId()).isNotNull();
        // Person is a record: use .firstName() accessor (not getFirstName())
        assertThat(newProfessor.getMember().firstName()).isEqualTo("Ada");
        assertThat(newProfessor.getMember().lastName()).isEqualTo("Lovelace");
    }

    // -------------------------------------------------------------------------
    // Department tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("3 departments are loaded: Humanities, Natural Sciences, Social Sciences")
    void allDepartmentsAreLoaded() {
        assertThat(universityService.findAllDepartments()).hasSize(3);
    }

    @Test
    @DisplayName("Find department by name using Optional")
    void findDepartmentByName() {
        // findByName returns Optional — we use isPresent() to verify it exists
        Optional<Department> result = departmentRepo.findByName("Humanities");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Humanities");
    }

    @Test
    @DisplayName("Find departments by chair's last name using text-block @Query")
    void findDepartmentsByChairLastName() {
        // Tests the @Query text block in DepartmentRepo.findByChairLastName()
        // Chair with last name 'Jones' chairs 'Humanities' (id=3 is Dean Jones)
        List<Department> departments = departmentRepo.findByChairLastName("Jones");
        assertThat(departments).isNotEmpty();
        assertThat(departments).allMatch(d -> d.getChair().getMember().lastName().equals("Jones"));
    }

    // -------------------------------------------------------------------------
    // Course tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("13 courses are loaded from data.sql")
    void allCoursesAreLoaded() {
        assertThat(universityService.findAllCourses()).hasSize(13);
    }

    @Test
    @DisplayName("Find courses by credit hours using @Query text block")
    void findCoursesByCredits() {
        // CourseRepo.findByCredits() uses a JPQL text block — testing it here
        List<Course> threeCredit = courseRepo.findByCredits(3);
        assertThat(threeCredit).isNotEmpty();
        assertThat(threeCredit).allMatch(c -> c.getCredits() == 3);
    }

    @Test
    @DisplayName("Find courses with more than N credits using @Query text block")
    void findCoursesWithMoreThanCredits() {
        List<Course> heavyCourses = courseRepo.findCoursesWithMoreThan(3);
        // English 301 (4 credits) and Java Programming (4 credits) should appear
        assertThat(heavyCourses).isNotEmpty();
        assertThat(heavyCourses).allMatch(c -> c.getCredits() > 3);
    }

    @Test
    @DisplayName("Create a course with prerequisites")
    void createCourseWithPrerequisites() {
        // Set up prerequisite course
        Course intro = courseRepo.findByName("English 101").orElseThrow();
        Staff professor = staffRepo.findById(5).orElseThrow();
        Department dept = departmentRepo.findByName("Humanities").orElseThrow();

        // Create an advanced course that requires the intro course
        Course advanced = universityService.createCourse(
                "Advanced Writing", 4, professor, dept, intro);

        assertThat(advanced.getId()).isNotNull();
        assertThat(advanced.getPrerequisites()).contains(intro);
    }

    // -------------------------------------------------------------------------
    // Student tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("5 students are loaded from data.sql")
    void allStudentsAreLoaded() {
        assertThat(universityService.findAllStudents()).hasSize(5);
    }

    @Test
    @DisplayName("Create a new student using Person record")
    void createStudent() {
        // Person record: immutable, no setters — pass both name parts to constructor
        Student student = universityService.createStudent("Grace", "Hopper", true, 28);

        assertThat(student.getStudentId()).isNotNull();
        assertThat(student.getAttendee().firstName()).isEqualTo("Grace");
        assertThat(student.getAttendee().lastName()).isEqualTo("Hopper");
        assertThat(student.isFullTime()).isTrue();
    }

    @Test
    @DisplayName("Find students younger than 22 using @Query text block")
    void findYoungerStudents() {
        var youngStudents = studentRepo.findYoungerThan(22);
        assertThat(youngStudents).isNotEmpty();
        assertThat(youngStudents).allMatch(s -> s.getAge() < 22);
    }

    @Test
    @DisplayName("Find students by enrollment status using @Query text block")
    void findStudentsByStatus() {
        var activeStudents = studentRepo.findByStatus("ACTIVE");
        assertThat(activeStudents).isNotEmpty();
        assertThat(activeStudents).allMatch(s -> "ACTIVE".equals(s.getStatus()));
    }
}
