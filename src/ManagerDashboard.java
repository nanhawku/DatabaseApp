import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

//First commit comment

public class ManagerDashboard extends JFrame {

    private final String dbUrl, dbUser, dbPassword;

    public ManagerDashboard(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        setTitle("Manager Dashboard");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton viewScheduleBtn = new JButton("View Student Schedule");
        JButton viewRosterBtn = new JButton("View Class Roster");
        JButton addStudentBtn = new JButton("Add Student to Class");
        JButton dropStudentBtn = new JButton("Drop Student from Class");
        JButton newStudentBtn = new JButton("Add New Student");

        viewScheduleBtn.addActionListener(e -> viewStudentSchedule());
        viewRosterBtn.addActionListener(e -> viewClassRoster());
        addStudentBtn.addActionListener(e -> addStudentToClass());
        dropStudentBtn.addActionListener(e -> dropStudentFromClass());
        newStudentBtn.addActionListener(e -> addNewStudent());

        setLayout(new GridLayout(5, 1, 10, 10));
        add(viewScheduleBtn);
        add(viewRosterBtn);
        add(addStudentBtn);
        add(dropStudentBtn);
        add(newStudentBtn);

        setVisible(true);
    }

    private void viewStudentSchedule() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (studentId != null) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                String query = "SELECT class_name FROM schedule WHERE student_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, studentId);
                    ResultSet rs = stmt.executeQuery();
                    StringBuilder sb = new StringBuilder("Schedule:\n");
                    while (rs.next()) {
                        sb.append(rs.getString("class_name")).append("\n");
                    }
                    JOptionPane.showMessageDialog(this, sb.toString());
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        }
    }

    private void viewClassRoster() {
        String className = JOptionPane.showInputDialog(this, "Enter Class Name:");
        if (className != null) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                String query = "SELECT student_id FROM schedule WHERE class_name = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, className);
                    ResultSet rs = stmt.executeQuery();
                    StringBuilder sb = new StringBuilder("Roster:\n");
                    while (rs.next()) {
                        sb.append("Student ID: ").append(rs.getString("student_id")).append("\n");
                    }
                    JOptionPane.showMessageDialog(this, sb.toString());
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        }
    }

    private void addStudentToClass() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        String className = JOptionPane.showInputDialog(this, "Enter Class Name:");
        if (studentId != null && className != null) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                String query = "INSERT INTO schedule (student_id, class_name) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, studentId);
                    stmt.setString(2, className);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Student added to class.");
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        }
    }

    private void dropStudentFromClass() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        String className = JOptionPane.showInputDialog(this, "Enter Class Name:");
        if (studentId != null && className != null) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                String query = "DELETE FROM schedule WHERE student_id = ? AND class_name = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, studentId);
                    stmt.setString(2, className);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Student removed from class.");
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        }
    }

    private void addNewStudent() {
        String studentId = JOptionPane.showInputDialog(this, "Enter New Student ID:");
        String name = JOptionPane.showInputDialog(this, "Enter Student Name:");
        if (studentId != null && name != null) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                String query = "INSERT INTO students (student_id, name) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, studentId);
                    stmt.setString(2, name);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Student added.");
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


