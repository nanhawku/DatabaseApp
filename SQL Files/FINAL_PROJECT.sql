/*
Nandi Hawkins and Ryan Bolt
COMP 267
April 2025
Project Description:
This is a SQL file that will create a database called 'ncat'. The DB contains 
a user table for program authentication and a roles table for DB authorization. If
a user identifies as a student, they will also have a major, and can view/drop
their classes. If a user identifies as a manager, they will be able to control 
class rosters and the list of students.
*/

drop database ncat;
create database ncat;
USE ncat;
DROP TABLE IF EXISTS Roles;
CREATE TABLE `ncat`.`Roles` (
  `id` VARCHAR(10) NOT NULL,
  `role` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`));

-- Insert the required roles
INSERT INTO Roles (id, role) VALUES ('mgr', 'Manager');
INSERT INTO Roles (id, role) VALUES ('stu', 'Student');

CREATE TABLE Users(
	ID INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    userpassword VARCHAR (255) NOT NULL,
    roleID VARCHAR(50),
    fname VARCHAR(100),
    lname VARCHAR(100),
    majorId INT
);

CREATE TABLE major(
id INT PRIMARY KEY,
major VARCHAR(45)
);

CREATE TABLE rosterclass(
rosterid INT,
userid INT
);

CREATE TABLE roster(
rosterid INT,
class VARCHAR(45),
code VARCHAR (45)
);

-- Adding Foreign Keys --
ALTER TABLE `ncat`.`Users` 
ADD INDEX `FK_Users_Roles_idx` (`roleID` ASC) VISIBLE;
;
ALTER TABLE `ncat`.`Users` 
ADD CONSTRAINT `FK_Users_Roles`
  FOREIGN KEY (`roleID`)
  REFERENCES `ncat`.`Roles` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `ncat`.`Users` 
ADD INDEX `FK_Users_Major_idx` (`majorId` ASC) VISIBLE;
;
ALTER TABLE `ncat`.`Users` 
ADD CONSTRAINT `FK_Users_Major`
  FOREIGN KEY (`majorId`)
  REFERENCES `ncat`.`major` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Making a composite Key in rosterclass --
ALTER TABLE `ncat`.`rosterclass` 
CHANGE COLUMN `rosterid` `rosterid` INT NOT NULL ,
CHANGE COLUMN `userid` `userid` INT NOT NULL ,
ADD PRIMARY KEY (`rosterid`, `userid`);
;

-- Allow users to log in

DROP USER IF EXISTS 'AggieAdmin'@'localhost';
DROP USER IF EXISTS 'AggieStudent'@'localhost';
CREATE USER 'AggieAdmin'@'localhost' IDENTIFIED BY 'AggiePride1';
CREATE USER 'AggieStudent'@'localhost' IDENTIFIED BY 'AggiePride2';
grant all privileges on ncat.* to 'AggieAdmin'@'localhost' with grant option;
grant all privileges on ncat.* to 'AggieStudent'@'localhost' with grant option;
flush privileges;


-- Stored Procedures

-- Procedure for authenticating users
DELIMITER //
CREATE PROCEDURE authenticate_user(
    IN p_username VARCHAR(100),
    IN p_password VARCHAR(255),
    OUT p_role VARCHAR(45)
)
BEGIN
    DECLARE v_roleID VARCHAR(10);

    -- Find the user
    SELECT roleID INTO v_roleID
    FROM Users
    WHERE username = p_username AND userpassword = p_password;

    -- Get the role name
    IF v_roleID IS NOT NULL THEN
        SELECT role INTO p_role
        FROM Roles
        WHERE id = v_roleID;
    ELSE
        SET p_role = NULL;
    END IF;
END //
DELIMITER ;

-- Procedure for getting student classes
DELIMITER //
CREATE PROCEDURE GetStudentClasses(
    IN p_studentId INT
)
BEGIN
    SELECT r.class as class_name
    FROM roster r
             INNER JOIN rosterclass rc ON r.rosterid = rc.rosterid
    WHERE rc.userid = p_studentId;
END //
DELIMITER ;

-- Procedure for dropping a student class
DELIMITER //
CREATE PROCEDURE DropStudentClass(
    IN p_studentId INT,
    IN p_className VARCHAR(45)
)
BEGIN
    DELETE rc FROM rosterclass rc
                       INNER JOIN roster r ON rc.rosterid = r.rosterid
    WHERE rc.userid = p_studentId AND r.class = p_className;
END //
DELIMITER ;

-- Procedure for viewing a student's schedule
DELIMITER //
CREATE PROCEDURE ViewStudentSchedule(
    IN p_studentId VARCHAR(45)
)
BEGIN
    SELECT r.class as class_name
    FROM roster r
             INNER JOIN rosterclass rc ON r.rosterid = rc.rosterid
    WHERE rc.userid = p_studentId;
END //
DELIMITER ;

-- Procedure for viewing a class roster
DELIMITER //
CREATE PROCEDURE ViewClassRoster(
    IN p_className VARCHAR(45)
)
BEGIN
    SELECT rc.userid as student_id, CONCAT(u.fname, ' ', u.lname) as student_name
    FROM rosterclass rc
             INNER JOIN roster r ON rc.rosterid = r.rosterid
             INNER JOIN Users u ON rc.userid = u.ID
    WHERE r.class = p_className;
END //
DELIMITER ;

-- Procedure for adding a student to a class
DELIMITER //
CREATE PROCEDURE AddStudentToClass(
    IN p_studentId VARCHAR(45),
    IN p_className VARCHAR(45)
)
BEGIN
    DECLARE v_rosterid INT;

    -- Get the roster ID for the class
    SELECT rosterid INTO v_rosterid
    FROM roster
    WHERE class = p_className;

    -- If class exists, add the student
    IF v_rosterid IS NOT NULL THEN
        INSERT INTO rosterclass (rosterid, userid)
        VALUES (v_rosterid, p_studentId);
    END IF;
END //
DELIMITER ;

-- Procedure for adding a new student
DELIMITER //
CREATE PROCEDURE AddNewStudent(
    IN p_studentId VARCHAR(45),
    IN p_firstName VARCHAR(100),
    IN p_lastName VARCHAR(100),
    IN p_username VARCHAR(100),
    IN p_password VARCHAR(255),
    IN p_majorId INT
)
BEGIN
    INSERT INTO Users (ID, username, userpassword, roleID, fname, lname, majorId)
    VALUES (p_studentId, p_username, p_password, 'stu', p_firstName, p_lastName, p_majorId);
END //
DELIMITER ;

-- Insert some sample data
INSERT INTO major (id, major) VALUES (1, 'Computer Science');
INSERT INTO major (id, major) VALUES (2, 'Computer Engineering');
INSERT INTO major (id, major) VALUES (3, 'Business Analytics');

-- Insert sample users
INSERT INTO Users (ID, username, userpassword, roleID, fname, lname, majorId)
VALUES
    (1, 'admin', 'password', 'mgr', 'Admin', 'User', NULL),
    (2, 'student1', 'password', 'stu', 'John', 'Doe', 1),
    (3, 'student2', 'password', 'stu', 'Jane', 'Smith', 2);

-- Insert sample classes
INSERT INTO roster (rosterid, class, code)
VALUES
    (1, 'Program Design', 'COMP 167'),
    (2, 'Data Structures', 'COMP 280'),
    (3, 'Database Design', 'COMP 267');

-- Register students for classes
INSERT INTO rosterclass (rosterid, userid)
VALUES
    (1, 2),  -- John Doe in CS101
    (2, 2),  -- John Doe in CS201
    (3, 3);  -- Jane Smith in CS301



/*
- All student rosters can be views
- No inline SQL in the application, must be a stored procedure and pass in arguments
- All of the SQL exists in the database, and not the application
*/
