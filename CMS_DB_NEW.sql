-- ============================================================
--  CMS Database – Full Schema (run in MySQL Workbench)
-- ============================================================
CREATE DATABASE IF NOT EXISTS CMS;
USE CMS;

-- Drop in reverse FK order
DROP TABLE IF EXISTS grade;
DROP TABLE IF EXISTS enrollment;
DROP TABLE IF EXISTS course_offering;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS class_section;
DROP TABLE IF EXISTS instructor;
DROP TABLE IF EXISTS department;
DROP TABLE IF EXISTS users;

-- ── users ────────────────────────────────────────────────────
CREATE TABLE users (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20)  NOT NULL   -- ADMIN | INSTRUCTOR | STUDENT
);

-- ── department ───────────────────────────────────────────────
CREATE TABLE department (
    dept_id   VARCHAR(20)  PRIMARY KEY,
    dept_name VARCHAR(100) NOT NULL
);

-- ── instructor ───────────────────────────────────────────────
CREATE TABLE instructor (
    instructor_id  VARCHAR(20) PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(100),
    phone          VARCHAR(20),
    specialization VARCHAR(100),
    user_id        INT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ── class_section ─────────────────────────────────────────────
CREATE TABLE class_section (
    class_id      VARCHAR(20) PRIMARY KEY,
    year_level    INT         NOT NULL,   -- 1, 2, 3, 4
    section       VARCHAR(10) NOT NULL,   -- A, B, C
    dept_id       VARCHAR(20),
    academic_year VARCHAR(20),            -- 2025/2026
    FOREIGN KEY (dept_id) REFERENCES department(dept_id)
);

-- ── students ─────────────────────────────────────────────────
CREATE TABLE students (
    student_id VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    gender     VARCHAR(10),
    email      VARCHAR(100),
    phone      VARCHAR(20),
    class_id   VARCHAR(20),              -- FK to class_section
    user_id    INT,
    FOREIGN KEY (class_id) REFERENCES class_section(class_id),
    FOREIGN KEY (user_id)  REFERENCES users(id)
);

-- ── course ───────────────────────────────────────────────────
CREATE TABLE course (
    course_id    VARCHAR(20) PRIMARY KEY,
    course_name  VARCHAR(100) NOT NULL,
    credit_hour  INT,
    instructor_id VARCHAR(20),
    FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id)
);

-- ── course_offering ──────────────────────────────────────────
--  Assigns a course to a class for a specific semester.
--  The system then auto-creates enrollment rows for every
--  student in that class.
CREATE TABLE course_offering (
    offering_id   INT AUTO_INCREMENT PRIMARY KEY,
    class_id      VARCHAR(20) NOT NULL,
    course_id     VARCHAR(20) NOT NULL,
    instructor_id VARCHAR(20),
    semester      VARCHAR(30) NOT NULL,
    academic_year VARCHAR(20),
    UNIQUE KEY uq_offering (class_id, course_id, semester, academic_year),
    FOREIGN KEY (class_id)      REFERENCES class_section(class_id),
    FOREIGN KEY (course_id)     REFERENCES course(course_id),
    FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id)
);

-- ── enrollment ───────────────────────────────────────────────
CREATE TABLE enrollment (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id    VARCHAR(20) NOT NULL,
    course_id     VARCHAR(20) NOT NULL,
    semester      VARCHAR(30),
    academic_year VARCHAR(20),
    UNIQUE KEY uq_enroll (student_id, course_id, semester, academic_year),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (course_id)  REFERENCES course(course_id)
);

-- ── grade ────────────────────────────────────────────────────
CREATE TABLE grade (
    grade_id      INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT  NOT NULL UNIQUE,
    grade         VARCHAR(5),
    FOREIGN KEY (enrollment_id) REFERENCES enrollment(enrollment_id)
);

-- ============================================================
--  Sample Data
-- ============================================================
INSERT INTO users VALUES
  (1,'admin',  '1234','ADMIN'),
  (2,'ins001', '1234','INSTRUCTOR'),
  (3,'ins002', '1234','INSTRUCTOR'),
  (4,'stu001', '1234','STUDENT'),
  (5,'stu002', '1234','STUDENT'),
  (6,'stu003', '1234','STUDENT');

INSERT INTO department VALUES
  ('DEPT001','Computer Science'),
  ('DEPT002','Mathematics');

INSERT INTO instructor VALUES
  ('INS001','Dr. Abel','abel@cms.edu','0911111111','Computer Science',2),
  ('INS002','Dr. Hana','hana@cms.edu','0922222222','Mathematics',3);

INSERT INTO class_section VALUES
  ('CLS001',1,'A','DEPT001','2025/2026'),
  ('CLS002',1,'B','DEPT001','2025/2026'),
  ('CLS003',2,'A','DEPT002','2025/2026');

INSERT INTO students VALUES
  ('ST001','Emebet','Female','emebet@cms.edu','0933333333','CLS001',4),
  ('ST002','Meron', 'Female','meron@cms.edu', '0944444444','CLS001',5),
  ('ST003','Dawit', 'Male',  'dawit@cms.edu', '0955555555','CLS002',6);

INSERT INTO course VALUES
  ('CS101','Java Programming',3,'INS001'),
  ('CS102','Database Systems',3,'INS001'),
  ('MATH101','Calculus',4,'INS002');

-- Course offerings for CLS001 Semester 1
INSERT INTO course_offering (class_id,course_id,instructor_id,semester,academic_year) VALUES
  ('CLS001','CS101','INS001','Semester 1','2025/2026'),
  ('CLS001','CS102','INS001','Semester 1','2025/2026');

-- Course offerings for CLS002 Semester 1
INSERT INTO course_offering (class_id,course_id,instructor_id,semester,academic_year) VALUES
  ('CLS002','MATH101','INS002','Semester 1','2025/2026');

-- Auto-enroll: CLS001 students → CS101, CS102
INSERT INTO enrollment (student_id,course_id,semester,academic_year) VALUES
  ('ST001','CS101','Semester 1','2025/2026'),
  ('ST001','CS102','Semester 1','2025/2026'),
  ('ST002','CS101','Semester 1','2025/2026'),
  ('ST002','CS102','Semester 1','2025/2026');

-- Auto-enroll: CLS002 students → MATH101
INSERT INTO enrollment (student_id,course_id,semester,academic_year) VALUES
  ('ST003','MATH101','Semester 1','2025/2026');

-- Sample grades
INSERT INTO grade (enrollment_id,grade) VALUES (1,'A'),(2,'A-'),(3,'B+');
