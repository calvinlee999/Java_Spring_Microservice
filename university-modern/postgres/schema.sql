-- =============================================================================
-- schema.sql — PostgreSQL schema for the University Modern application
--
-- Tables:
--   staff_member        — professors and other university staff
--   department          — academic departments (Humanities, Sciences, etc.)
--   student             — enrolled students
--   course              — courses offered by departments
--   course_prerequisites — which courses must be taken before another course
--   course_student       — which students are enrolled in which courses
-- =============================================================================

-- Staff members: first name + last name stored as separate columns.
-- When Person becomes a Java record (@Embeddable), Hibernate still maps
-- firstName() → first_name and lastName() → last_name here.
-- version: managed by JPA @Version — prevents two users from overwriting each
--          other's changes at the same time (optimistic locking / lost-update protection).
CREATE TABLE staff_member (
  id         SERIAL PRIMARY KEY,
  first_name VARCHAR(100) NOT NULL,
  last_name  VARCHAR(100) NOT NULL,
  version    BIGINT       NOT NULL DEFAULT 0
);

-- Departments: each has a name and a chair (a staff member who runs it).
-- version: JPA optimistic locking — if two requests try to rename the same
--          department simultaneously, only the first one wins; the second gets
--          an OptimisticLockException instead of silently overwriting data.
CREATE TABLE department (
  id       SERIAL PRIMARY KEY,
  name     VARCHAR(100) NOT NULL,
  chair_id BIGINT,
  version  BIGINT       NOT NULL DEFAULT 0
);
ALTER TABLE department ADD FOREIGN KEY (chair_id) REFERENCES staff_member(id);

-- Students: basic info + whether they attend full-time.
-- status stores the enrollment state: ACTIVE, GRADUATED, or SUSPENDED.
-- This column pairs with the EnrollmentStatus sealed interface in service logic.
-- version: JPA optimistic locking — prevents concurrent status updates from stomping each other.
CREATE TABLE student (
  id         SERIAL PRIMARY KEY,
  first_name VARCHAR(100) NOT NULL,
  last_name  VARCHAR(100) NOT NULL,
  age        INTEGER      NOT NULL,
  full_time  BOOLEAN      NOT NULL,
  status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  version    BIGINT       NOT NULL DEFAULT 0
);

-- Courses: offered by a department and taught by a staff member.
-- version: JPA optimistic locking — prevents two professors from editing the
--          same course record simultaneously and losing one of their changes.
CREATE TABLE course (
  id            SERIAL PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  credits       INTEGER      NOT NULL,
  instructor_id BIGINT       NOT NULL,
  department_id BIGINT       NOT NULL,
  version       BIGINT       NOT NULL DEFAULT 0
);
ALTER TABLE course ADD FOREIGN KEY (instructor_id) REFERENCES staff_member(id);
ALTER TABLE course ADD FOREIGN KEY (department_id) REFERENCES department(id);

-- Prerequisites: a course can require other courses to be completed first.
-- Example: English 301 requires English 201 to be taken first.
CREATE TABLE course_prerequisites (
  id_of_course          BIGINT NOT NULL,
  id_prerequisite_course BIGINT NOT NULL,
  PRIMARY KEY (id_of_course, id_prerequisite_course)
);
ALTER TABLE course_prerequisites ADD FOREIGN KEY (id_of_course)          REFERENCES course(id);
ALTER TABLE course_prerequisites ADD FOREIGN KEY (id_prerequisite_course) REFERENCES course(id);

-- Enrollment: maps students to the courses they are taking.
CREATE TABLE course_student (
  student_id BIGINT NOT NULL,
  course_id  BIGINT NOT NULL,
  PRIMARY KEY (student_id, course_id)
);
ALTER TABLE course_student ADD FOREIGN KEY (student_id) REFERENCES student(id);
ALTER TABLE course_student ADD FOREIGN KEY (course_id)  REFERENCES course(id);
