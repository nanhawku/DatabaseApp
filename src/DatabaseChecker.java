import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This utility class helps test database operations for the NCAT application
 */
public class DatabaseChecker extends JFrame {

    private final String DB_URL = "jdbc:mysql://localhost:3306/ncat";
    private final String DB_USER = "AggieAdmin";
    private final String DB_PASSWORD = "AggiePride1";

    private JTextArea outputArea;
    private JButton testConnectionBtn;
    private JButton viewUsersBtn;
    private JButton viewClassesBtn;
    private JButton viewRosterBtn;
    private JButton addTestDataBtn;

    public DatabaseChecker() {
        setTitle("NCAT Database Tester");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        testConnectionBtn = new JButton("Test Connection");
        viewUsersBtn = new JButton("View Users");
        viewClassesBtn = new JButton("View Classes");
        viewRosterBtn = new JButton("View Class Roster");
        addTestDataBtn = new JButton("Add Test Data");

        // Add action listeners
        testConnectionBtn.addActionListener(e -> testConnection());
        viewUsersBtn.addActionListener(e -> viewUsers());
        viewClassesBtn.addActionListener(e -> viewClasses());
        viewRosterBtn.addActionListener(e -> viewRoster());
        addTestDataBtn.addActionListener(e -> addTestData());

        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.add(testConnectionBtn);
        buttonPanel.add(viewUsersBtn);
        buttonPanel.add(viewClassesBtn);
        buttonPanel.add(viewRosterBtn);
        buttonPanel.add(addTestDataBtn);

        // Add components to frame
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);

        // Automatically test connection on startup
        testConnection();
    } //end DatabaseChecker

    private void testConnection() {
        outputArea.setText("");
        appendOutput("Testing database connection...\n");

        try {
            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            appendOutput("MySQL JDBC Driver loaded successfully\n");

            // Test connection
            appendOutput("Attempting to connect to: " + DB_URL + "\n");
            appendOutput("Using credentials: " + DB_USER + "\n");

            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            appendOutput("Connected to database successfully!\n");

            // Get database metadata
            DatabaseMetaData metaData = conn.getMetaData();
            appendOutput("\nDatabase Information:\n");
            appendOutput("Database Product Name: " + metaData.getDatabaseProductName() + "\n");
            appendOutput("Database Product Version: " + metaData.getDatabaseProductVersion() + "\n");
            appendOutput("JDBC Driver Name: " + metaData.getDriverName() + "\n");
            appendOutput("JDBC Driver Version: " + metaData.getDriverVersion() + "\n");

            conn.close();
        } catch (ClassNotFoundException e) {
            appendOutput("Error: MySQL JDBC Driver not found!\n");
            appendOutput(e.getMessage() + "\n");
        } catch (SQLException e) {
            appendOutput("Error connecting to database: " + e.getMessage() + "\n");
        }
    } //end testConnection

    private void viewUsers() {
        outputArea.setText("");
        appendOutput("Retrieving users from database...\n\n");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT u.ID, u.username, u.userpassword, r.role, u.fname, u.lname, m.major " +
                    "FROM Users u " +
                    "JOIN Roles r ON u.roleID = r.id " +
                    "LEFT JOIN major m ON u.majorId = m.id " +
                    "ORDER BY u.ID";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                appendOutput(String.format("%-5s %-15s %-15s %-10s %-15s %-15s %-20s\n",
                        "ID", "Username", "Password", "Role", "First Name", "Last Name", "Major"));
                appendOutput("-".repeat(100) + "\n");

                while (rs.next()) {
                    appendOutput(String.format("%-5s %-15s %-15s %-10s %-15s %-15s %-20s\n",
                            rs.getString("ID"),
                            rs.getString("username"),
                            rs.getString("userpassword"),
                            rs.getString("role"),
                            rs.getString("fname"),
                            rs.getString("lname"),
                            rs.getString("major") != null ? rs.getString("major") : "N/A"));
                }
            }
        } catch (SQLException e) {
            appendOutput("Error retrieving users: " + e.getMessage() + "\n");
        }
    } //end viewUsers

    private void viewClasses() {
        outputArea.setText("");
        appendOutput("Retrieving classes from database...\n\n");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT rosterid, class, code FROM roster ORDER BY rosterid";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                appendOutput(String.format("%-10s %-30s %-15s\n",
                        "Roster ID", "Class Name", "Code"));
                appendOutput("-".repeat(60) + "\n");

                while (rs.next()) {
                    appendOutput(String.format("%-10s %-30s %-15s\n",
                            rs.getString("rosterid"),
                            rs.getString("class"),
                            rs.getString("code")));
                }
            }
        } catch (SQLException e) {
            appendOutput("Error retrieving classes: " + e.getMessage() + "\n");
        }
    } //end viewClasses

    private void viewRoster() {
        outputArea.setText("");

        // Ask the user which class to view
        String className = JOptionPane.showInputDialog(this, "Enter Class Name:");
        if (className == null || className.trim().isEmpty()) {
            appendOutput("No class name entered.\n");
            return;
        }

        appendOutput("Retrieving roster for class: " + className + "\n\n");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Use the ViewClassRoster stored procedure
            String call = "{CALL ViewClassRoster(?)}";
            try (CallableStatement stmt = conn.prepareCall(call)) {
                stmt.setString(1, className);
                ResultSet rs = stmt.executeQuery();

                appendOutput(String.format("%-10s %-30s\n",
                        "Student ID", "Student Name"));
                appendOutput("-".repeat(45) + "\n");

                boolean hasStudents = false;

                while (rs.next()) {
                    appendOutput(String.format("%-10s %-30s\n",
                            rs.getString("student_id"),
                            rs.getString("student_name")));
                    hasStudents = true;
                }

                if (!hasStudents) {
                    appendOutput("No students enrolled in this class.\n");
                }
            }
        } catch (SQLException e) {
            appendOutput("Error retrieving roster: " + e.getMessage() + "\n");
        }
    } //end viewRoster

    private void addTestData() {
        outputArea.setText("");
        appendOutput("Adding test data to database...\n\n");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Start transaction
            conn.setAutoCommit(false);

            try {
                // Add a test student
                int studentId = 9999;
                String username = "teststudent";
                String password = "test123";
                String firstName = "Test";
                String lastName = "Student";
                int majorId = 1; // Computer Science

                // Check if student exists
                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT ID FROM Users WHERE ID = ? OR username = ?");
                checkStmt.setInt(1, studentId);
                checkStmt.setString(2, username);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    appendOutput("Test student already exists with ID: " + rs.getInt("ID") + "\n");
                } else {
                    // Insert new student
                    PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO Users (ID, username, userpassword, roleID, fname, lname, majorId) " +
                                    "VALUES (?, ?, ?, 'stu', ?, ?, ?)");
                    insertStmt.setInt(1, studentId);
                    insertStmt.setString(2, username);
                    insertStmt.setString(3, password);
                    insertStmt.setString(4, firstName);
                    insertStmt.setString(5, lastName);
                    insertStmt.setInt(6, majorId);
                    insertStmt.executeUpdate();

                    appendOutput("Added test student with ID: " + studentId + "\n");
                }

                // Add test class if it doesn't exist
                int classId = 9999;
                String className = "Test Class";
                String classCode = "TEST101";

                PreparedStatement checkClassStmt = conn.prepareStatement(
                        "SELECT rosterid FROM roster WHERE rosterid = ? OR class = ?");
                checkClassStmt.setInt(1, classId);
                checkClassStmt.setString(2, className);
                rs = checkClassStmt.executeQuery();

                if (rs.next()) {
                    appendOutput("Test class already exists with ID: " + rs.getInt("rosterid") + "\n");
                } else {
                    // Insert new class
                    PreparedStatement insertClassStmt = conn.prepareStatement(
                            "INSERT INTO roster (rosterid, class, code) VALUES (?, ?, ?)");
                    insertClassStmt.setInt(1, classId);
                    insertClassStmt.setString(2, className);
                    insertClassStmt.setString(3, classCode);
                    insertClassStmt.executeUpdate();

                    appendOutput("Added test class with ID: " + classId + "\n");
                }

                // Register student for class if not already registered
                PreparedStatement checkRegStmt = conn.prepareStatement(
                        "SELECT * FROM rosterclass WHERE rosterid = ? AND userid = ?");
                checkRegStmt.setInt(1, classId);
                checkRegStmt.setInt(2, studentId);
                rs = checkRegStmt.executeQuery();

                if (rs.next()) {
                    appendOutput("Student " + studentId + " is already registered for class " + classId + "\n");
                } else {
                    // Register student for class
                    PreparedStatement registerStmt = conn.prepareStatement(
                            "INSERT INTO rosterclass (rosterid, userid) VALUES (?, ?)");
                    registerStmt.setInt(1, classId);
                    registerStmt.setInt(2, studentId);
                    registerStmt.executeUpdate();

                    appendOutput("Registered student " + studentId + " for class " + classId + "\n");
                }

                // Commit transaction
                conn.commit();
                appendOutput("\nAll test data has been added successfully!\n");

            } catch (SQLException e) {
                // Rollback transaction on error
                conn.rollback();
                throw e;
            } finally {
                // Restore auto-commit
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            appendOutput("Error adding test data: " + e.getMessage() + "\n");
        }
    } // end addTestData

    private void appendOutput(String text) {
        outputArea.append(text);
        // Scroll to the bottom
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    } //end appendOutput

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DatabaseChecker::new);
    } //end main
} //end DatabaseChecker
