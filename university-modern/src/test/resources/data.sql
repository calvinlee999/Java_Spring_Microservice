-- =============================================================================
-- data.sql — Test data loaded into the H2 in-memory database before each test.
--
-- This file is run automatically because application.properties (test) has:
--   spring.sql.init.mode=always
--   spring.jpa.hibernate.ddl-auto=create-drop
--
-- Hibernate creates the tables first, then Spring runs this file to populate them.
-- After all tests finish, the H2 database is dropped — no cleanup needed!
-- =============================================================================

-- Staff members: professors and chairs at the university.
-- Column names match the @Column annotations on the Person record:
--   first_name -> Person.firstName(), last_name -> Person.lastName()
insert into staff_member (id, first_name, last_name) values
(1,  'Dean',    'Thomas'),
(2,  'Dean',    'Green'),
(3,  'Dean',    'Jones'),
(4,  'Dean',    'Martin'),
(5,  'James',   'Brown'),
(6,  'Judy',    'Miller'),
(7,  'James',   'Davis'),
(8,  'Allison', 'Moore'),
(9,  'Whitney', 'White'),
(10, 'Jack',    'Black'),
(11, 'Queen',   'King');

-- Departments: each chaired by a staff member (chair_id is a foreign key).
insert into department (id, name, chair_id) values
(21, 'Humanities',       3),
(22, 'Natural Sciences', 4),
(23, 'Social Sciences',  3);

-- Courses: each has a name, credit value, instructor, and owning department.
insert into course (id, name, credits, instructor_id, department_id) values
(31, 'English 101',     3, 10, 21),
(32, 'English 201',     3,  5, 21),
(33, 'English 301',     4, 10, 21),
(34, 'Chemistry',       3,  7, 22),
(35, 'Physics',         3,  7, 22),
(36, 'C Programming',   3,  8, 22),
(37, 'Java Programming',4,  8, 22),
(38, 'History 101',     3,  6, 23),
(39, 'Anthropology',    3, 11, 23),
(40, 'Sociology',       3, 11, 23),
(41, 'Psychology',      3, 10, 23),
(42, 'Chemistry Lab',   1,  7, 22),
(43, 'Physics Lab',     1,  7, 22);

-- Prerequisites: some courses require completion of an earlier course.
-- id_of_course = the course that HAS a prerequisite
-- id_prerequisite_course = the course that must be taken FIRST
insert into course_prerequisites (id_of_course, id_prerequisite_course) values
(32, 31),  -- English 201 requires English 101
(33, 32),  -- English 301 requires English 201
(35, 34),  -- Physics requires Chemistry
(39, 38),  -- Anthropology requires History 101
(40, 38),  -- Sociology requires History 101
(41, 38);  -- Psychology requires History 101

-- Students: enrolled at the university.
-- status column stores one of: ACTIVE, GRADUATED, SUSPENDED
-- This pairs with the EnrollmentStatus sealed interface in the service layer.
insert into student (id, first_name, last_name, age, full_time, status) values
(51, 'Alice',   'Walker',  20, true,  'ACTIVE'),
(52, 'Bob',     'Smith',   22, false, 'ACTIVE'),
(53, 'Charlie', 'Brown',   25, true,  'GRADUATED'),
(54, 'Diana',   'Prince',  19, true,  'SUSPENDED'),
(55, 'Eve',     'Johnson', 21, false, 'ACTIVE');
