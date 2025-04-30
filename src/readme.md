# NCAT Database Management System

## COMP 267 Final Project
**By: Nandi Hawkins and Ryan Bolt**

This project implements a database management system for North Carolina A&T State University (NCAT) that allows for user authentication and role-based access. The system has two main roles:

1. **Student Role**: Students can view their class schedule and drop classes.
2. **Manager Role**: Managers can view student schedules, view class rosters, add students to classes, drop students from classes, and add new students to the system.

## Project Structure

The project consists of the following components:

### Database
- MySQL database with tables for users, roles, majors, classes, and student registrations
- Complete stored procedures to handle all database operations
- Authorization system that distinguishes between student and manager roles

### Java Application
- GUI interface built with Java Swing
- Login system with authentication
- Role-based dashboards
- Operations for both student and manager roles

## Setup Instructions

1. **Set up the database:**
   - Run the `FINAL_PROJECT.sql` script to create the database, tables, and stored procedures
   - This will also create test users and sample data

2. **Configure the database connection:**
   - The application is set to connect to MySQL at `localhost:3306`
   - Username: `AggieAdmin`
   - Password: `AggiePride1`
   - If your MySQL setup is different, modify the connection strings in the Java files

3. **Compile and run the application:**
   - Compile all Java files in the project
   - Run the `DatabaseApp` class to start the application

## Sample Login Credentials

### Manager
- Username: `admin`
- Password: `password`

### Students
- Username: `student1`
- Password: `password`
- Username: `student2` 
- Password: `password`

## Features

### Login System
- Users can authenticate with username and password
- Failed login attempts are tracked, and the application terminates after 3 failures

### Student Dashboard
- View enrolled classes
- Drop classes

### Manager Dashboard
- View a student's class schedule
- View class rosters
- Add students to classes
- Drop students from classes
- Add new students to the system
