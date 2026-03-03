package com.example.university.dao;

import com.example.university.business.CourseFilter;
import com.example.university.business.DynamicQueryService;
import com.example.university.business.UniversityService;
import com.example.university.domain.*;
import com.example.university.repo.CourseRepo;
import com.example.university.repo.DepartmentRepo;
import com.example.university.repo.StaffRepo;
import com.example.university.repo.StudentRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.SequencedCollection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ModernFeaturesTest — verifies every modern Java 17/21 and Spring Boot 3.2+
 * feature used in the university-modern application.
 *
 * <p>Tests are grouped with {@code @Nested} classes so it's easy to see
 * which feature is being tested in each section:
 * <ul>
 *   <li>{@link RecordTests}              — Java 17 Records (Person)</li>
 *   <li>{@link SealedInterfaceTests}     — Java 17 Sealed Interface (EnrollmentStatus)</li>
 *   <li>{@link PatternMatchingTests}     — Java 17 Pattern Matching for instanceof</li>
 *   <li>{@link SequencedCollectionTests} — Java 21 SequencedCollection API</li>
 *   <li>{@link SealedSwitchTests}        — Java 21 Pattern Matching Switch + Records</li>
 * </ul>
 */
@SpringBootTest
@Transactional
class ModernFeaturesTest {

    @Autowired
    private UniversityService    universityService;
    @Autowired
    private DynamicQueryService  dynamicQueryService;
    @Autowired
    private CourseRepo           courseRepo;
    @Autowired
    private DepartmentRepo       departmentRepo;
    @Autowired
    private StaffRepo            staffRepo;
    @Autowired
    private StudentRepo          studentRepo;

    // =========================================================================
    // Java 17 — Records
    // =========================================================================

    @Nested
    @DisplayName("Java 17 — Record: Person")
    class RecordTests {

        @Test
        @DisplayName("Person record accessors use firstName() not getFirstName()")
        void recordAccessorsWork() {
            // Java 17 Record: the accessor method matches the component name exactly
            Person p = new Person("Marie", "Curie");
            assertThat(p.firstName()).isEqualTo("Marie");  // record accessor
            assertThat(p.lastName()).isEqualTo("Curie");   // record accessor
        }

        @Test
        @DisplayName("Person record auto-generates equals() based on values")
        void recordEqualsIsValueBased() {
            // Records auto-generate equals() — two records with same data are equal.
            // Old plain class: you'd need to write equals() yourself.
            Person p1 = new Person("Alan", "Turing");
            Person p2 = new Person("Alan", "Turing");
            assertThat(p1).isEqualTo(p2);        // same data → equal
            assertThat(p1).isNotSameAs(p2);      // but different objects in memory
        }

        @Test
        @DisplayName("Person record auto-generates toString()")
        void recordToStringWork() {
            Person p = new Person("Linus", "Torvalds");
            String text = p.toString();
            // Records produce: Person[firstName=Linus, lastName=Torvalds]
            assertThat(text).contains("Linus").contains("Torvalds");
        }

        @Test
        @DisplayName("Staff with Person record can be saved and retrieved")
        void personRecordPersistsCorrectly() {
            Staff prof = universityService.createFaculty("Rosalind", "Franklin");
            Staff loaded = staffRepo.findById(prof.getId()).orElseThrow();
            // Verify the record's column mappings (@Column on record components) work
            assertThat(loaded.getMember().firstName()).isEqualTo("Rosalind");
            assertThat(loaded.getMember().lastName()).isEqualTo("Franklin");
        }
    }

    // =========================================================================
    // Java 17 — Sealed Interface
    // =========================================================================

    @Nested
    @DisplayName("Java 17 — Sealed Interface: EnrollmentStatus")
    class SealedInterfaceTests {

        @Test
        @DisplayName("Active is a permitted implementation of EnrollmentStatus")
        void activeIsValid() {
            EnrollmentStatus status = new EnrollmentStatus.Active("Spring 2025");
            assertThat(status).isInstanceOf(EnrollmentStatus.Active.class);
            // Downcast to record type and access the component
            assertThat(((EnrollmentStatus.Active) status).semester()).isEqualTo("Spring 2025");
        }

        @Test
        @DisplayName("Graduated is a permitted implementation of EnrollmentStatus")
        void graduatedIsValid() {
            EnrollmentStatus status = new EnrollmentStatus.Graduated(2024);
            assertThat(((EnrollmentStatus.Graduated) status).graduationYear()).isEqualTo(2024);
        }

        @Test
        @DisplayName("Suspended is a permitted implementation of EnrollmentStatus")
        void suspendedIsValid() {
            EnrollmentStatus status = new EnrollmentStatus.Suspended("Unpaid tuition");
            assertThat(((EnrollmentStatus.Suspended) status).reason()).isEqualTo("Unpaid tuition");
        }

        @Test
        @DisplayName("toEnrollmentStatus converts 'ACTIVE' string to Active record")
        void convertActiveStatusFromStudent() {
            // Get Alice (status = 'ACTIVE') from data.sql
            var alice = studentRepo.findByStatus("ACTIVE").getFirst();
            EnrollmentStatus es = universityService.toEnrollmentStatus(alice, "Fall 2025");
            assertThat(es).isInstanceOf(EnrollmentStatus.Active.class);
        }

        @Test
        @DisplayName("toEnrollmentStatus converts 'SUSPENDED' string to Suspended record")
        void convertSuspendedStatusFromStudent() {
            var diana = studentRepo.findByStatus("SUSPENDED").getFirst();
            EnrollmentStatus es = universityService.toEnrollmentStatus(diana, "Late assignment");
            assertThat(es).isInstanceOf(EnrollmentStatus.Suspended.class);
            assertThat(((EnrollmentStatus.Suspended) es).reason()).isEqualTo("Late assignment");
        }
    }

    // =========================================================================
    // Java 17 — Pattern Matching for instanceof
    // =========================================================================

    @Nested
    @DisplayName("Java 17 — Pattern Matching for instanceof")
    class PatternMatchingTests {

        @Test
        @DisplayName("describeFilter returns filter description for a CourseFilter")
        void describeFilterWithCourseFilter() {
            Department dept = departmentRepo.findByName("Humanities").orElseThrow();
            CourseFilter filter = CourseFilter.filterBy().department(dept).credits(3);

            // This method uses: if (filterObject instanceof CourseFilter cf) { ... }
            String description = dynamicQueryService.describeFilter(filter);

            assertThat(description).contains("Humanities");
            assertThat(description).contains("credits=3");
        }

        @Test
        @DisplayName("describeFilter falls back gracefully for non-CourseFilter objects")
        void describeFilterWithUnknownObject() {
            // Pass a random object — pattern matching instanceof should handle it safely
            String description = dynamicQueryService.describeFilter("not a filter");
            assertThat(description).contains("Unknown filter type");
        }
    }

    // =========================================================================
    // Java 21 — SequencedCollection
    // =========================================================================

    @Nested
    @DisplayName("Java 21 — SequencedCollection API")
    class SequencedCollectionTests {

        @Test
        @DisplayName("findCoursesReversed() returns a non-null reversed view")
        void reversedCoursesNotNull() {
            SequencedCollection<Course> reversed = universityService.findCoursesReversed();
            // Java 21: List.reversed() returns a non-null, non-empty reversed view
            assertThat(reversed).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("findFirstCourse() returns the alphabetically first course")
        void firstCourseIsAlphabeticallyFirst() {
            Course first = universityService.findFirstCourse();
            // 'Anthropology' comes first alphabetically in our data
            assertThat(first).isNotNull();
            // The first course should come before all others alphabetically
            List<Course> allSorted = courseRepo.findAll(
                    org.springframework.data.domain.Sort.by("name"));
            assertThat(first.getName()).isEqualTo(allSorted.getFirst().getName());
        }

        @Test
        @DisplayName("findLastCourse() returns the alphabetically last course")
        void lastCourseIsAlphabeticallyLast() {
            Course last = universityService.findLastCourse();
            List<Course> allSorted = courseRepo.findAll(
                    org.springframework.data.domain.Sort.by("name"));
            // Java 21 getLast() should match the last element of the sorted list
            assertThat(last.getName()).isEqualTo(allSorted.getLast().getName());
        }

        @Test
        @DisplayName("reversed() does not mutate the original list")
        void reversedDoesNotMutateOriginal() {
            List<Course> original = courseRepo.findAll();
            int originalSize = original.size();

            // Java 21: reversed() returns a VIEW, not a copy — original is unchanged
            SequencedCollection<Course> reversed = original.reversed();

            assertThat(original).hasSize(originalSize);  // original size unchanged
            assertThat(reversed).hasSize(originalSize);  // same elements, reversed order
        }
    }

    // =========================================================================
    // Java 21 — Pattern Matching Switch with Sealed Interface Records
    // =========================================================================

    @Nested
    @DisplayName("Java 21 — Pattern Matching Switch on Sealed Interfaces")
    class SealedSwitchTests {

        @Test
        @DisplayName("describeEnrollment returns correct text for Active")
        void describeActive() {
            EnrollmentStatus active = new EnrollmentStatus.Active("Fall 2025");
            // Java 21 pattern-matching switch with record deconstruction runs inside describeEnrollment()
            String result = universityService.describeEnrollment(active);
            assertThat(result).contains("Fall 2025");
        }

        @Test
        @DisplayName("describeEnrollment returns correct text for Graduated")
        void describeGraduated() {
            EnrollmentStatus graduated = new EnrollmentStatus.Graduated(2023);
            String result = universityService.describeEnrollment(graduated);
            assertThat(result).contains("2023");
        }

        @Test
        @DisplayName("describeEnrollment returns correct text for Suspended")
        void describeSuspended() {
            EnrollmentStatus suspended = new EnrollmentStatus.Suspended("Academic probation");
            String result = universityService.describeEnrollment(suspended);
            assertThat(result).contains("Academic probation");
        }

        @Test
        @DisplayName("All three sealed types produce distinct, non-empty descriptions")
        void allSealedTypesProduceDescriptions() {
            String activeDesc    = universityService.describeEnrollment(new EnrollmentStatus.Active("Spring 2025"));
            String graduatedDesc = universityService.describeEnrollment(new EnrollmentStatus.Graduated(2024));
            String suspendedDesc = universityService.describeEnrollment(new EnrollmentStatus.Suspended("Reason"));

            // All three should produce non-empty, distinct messages
            assertThat(activeDesc).isNotBlank();
            assertThat(graduatedDesc).isNotBlank();
            assertThat(suspendedDesc).isNotBlank();
            assertThat(activeDesc).isNotEqualTo(graduatedDesc);
            assertThat(graduatedDesc).isNotEqualTo(suspendedDesc);
        }
    }
}
