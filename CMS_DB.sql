use CMS;
CREATE TABLE  students (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100)
);
CREATE TABLE course (
    coursecode VARCHAR(20) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    instructor VARCHAR(100) NOT NULL
);
CREATE TABLE instructor (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    department VARCHAR(100)
);

SHOW TABLES;
SELECT * FROM student;
SELECT * FROM students;
SELECT * FROM instructor;
DROP TABLE  course;
CREATE TABLE course (
    coursecode VARCHAR(20) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    instructor VARCHAR(100) NOT NULL
);

SELECT * FROM students;
SELECT * FROM course;

DROP table enrollment;


DROP TABLE enrollment;
DROP table users;

CREATE TABLE enrollment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    grade VARCHAR(5),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_code) REFERENCES course(coursecode)
);

SELECT * FROM enrollment ;

SELECT * FROM students;
SELECT * FROM course;
SELECT * FROM enrollment ;
SELECT * FROM instructor ;
SELECT * FROM users ;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(50)
);
INSERT INTO users (username, password, role)
VALUES ('admin', '1234', 'admin');




