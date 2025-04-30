import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManagerDashboard extends JFrame {

    private final String dbUrl, dbUser, dbPassword;

    //Constructor
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
    } //end constructor

    private void viewStudentSchedule() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (studentId != null && !studentId.trim().isEmpty()) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                // Use the stored procedure
                String call = "{CALL ViewStudentSchedule(?)}";
                try (CallableStatement stmt = conn.prepareCall(call)) {
                    stmt.setString(1, studentId);
                    ResultSet rs = stmt.executeQuery();

                    StringBuilder sb = new StringBuilder("Schedule for Student ID " + studentId + ":\n");
                    boolean hasClasses = false;

                    while (rs.next()) {
                        sb.append(rs.getString("class_name")).append("\n");
                        hasClasses = true;
                    }

                    if (!hasClasses) {
                        sb.append("No classes found for this student.");
                    }

                    JOptionPane.showMessageDialog(this, sb.toString());
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        }//end viewStudentSchedule
    }

    private void viewClassRoster() {
        String className = JOptionPane.showInputDialog(this, "Enter Class Name:");
        if (className != null && !className.trim().isEmpty()) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                // Use the stored procedure
                String call = "{CALL ViewClassRoster(?)}";
                try (CallableStatement stmt = conn.prepareCall(call)) {
                    stmt.setString(1, className);
                    ResultSet rs = stmt.executeQuery();

                    StringBuilder sb = new StringBuilder("Roster for " + className + ":\n");
                    boolean hasStudents = false;

                    while (rs.next()) {
                        sb.append("Student ID: ").append(rs.getString("student_id"));

                        // If student_name column exists
                        try {
                            String name = rs.getString("student_name");
                            if (name != null && !name.trim().isEmpty()) {
                                sb.append(" - ").append(name);
                            }
                        } catch (SQLException e) {
                            // Column doesn't exist, just continue
                        }

                        sb.append("\n");
                        hasStudents = true;
                    }

                    if (!hasStudents) {
                        sb.append("No students enrolled in this class.");
                    }

                    JOptionPane.showMessageDialog(this, sb.toString());
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        } //end if
    } //end viewClassRoster

    private void addStudentToClass() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        String className = JOptionPane.showInputDialog(this, "Enter Class Name:");

        if (studentId != null && !studentId.trim().isEmpty() &&
                className != null && !className.trim().isEmpty()) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                // Use the stored procedure
                String call = "{CALL AddStudentToClass(?, ?)}";
                try (CallableStatement stmt = conn.prepareCall(call)) {
                    stmt.setString(1, studentId);
                    stmt.setString(2, className);
                    stmt.execute();
                    JOptionPane.showMessageDialog(this, "Student added to class successfully.");
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        }//end if
    }//end addStudentToClass

    private void dropStudentFromClass() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        String className = JOptionPane.showInputDialog(this, "Enter Class Name:");

        if (studentId != null && !studentId.trim().isEmpty() &&
                className != null && !className.trim().isEmpty()) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                // Use a modified version of our drop student class procedure
                String call = "{CALL DropStudentClass(?, ?)}";
                try (CallableStatement stmt = conn.prepareCall(call)) {
                    stmt.setInt(1, Integer.parseInt(studentId));
                    stmt.setString(2, className);
                    stmt.execute();
                    JOptionPane.showMessageDialog(this, "Student removed from class successfully.");
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        }//end if
    }//end dropStudentFromClass

    private void addNewStudent() {
        // Create a form for adding a new student
        JTextField idField = new JTextField(10);
        JTextField firstNameField = new JTextField(15);
        JTextField lastNameField = new JTextField(15);
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JComboBox<String> majorBox = new JComboBox<>();

        // Load majors from database
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "SELECT id, major FROM major";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    majorBox.addItem(rs.getInt("id") + " - " + rs.getString("major"));
                }
            }
        } catch (SQLException ex) {
            showError(ex);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.add(new JLabel("Student ID:"));
        panel.add(idField);
        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Major:"));
        panel.add(majorBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String studentId = idField.getText();
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // Extract major ID from selection
            String selectedMajor = (String) majorBox.getSelectedItem();
            int majorId = Integer.parseInt(selectedMajor.split(" - ")[0]);

            if (studentId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() ||
                    username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!");
                return;
            }

            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                // Use the stored procedure
                String call = "{CALL AddNewStudent(?, ?, ?, ?, ?, ?)}";
                try (CallableStatement stmt = conn.prepareCall(call)) {
                    stmt.setString(1, studentId);
                    stmt.setString(2, firstName);
                    stmt.setString(3, lastName);
                    stmt.setString(4, username);
                    stmt.setString(5, password);
                    stmt.setInt(6, majorId);
                    stmt.execute();
                    JOptionPane.showMessageDialog(this, "Student added successfully.");
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        }
    } //end addNewStudent

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } //end showError
}//end ManagerDashboard