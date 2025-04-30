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


/*
- All student rosters can be views
- No inline SQL in the application, must be a stored procedure and pass in arguments
- All of the SQL exists in the database, and not the application
*/
