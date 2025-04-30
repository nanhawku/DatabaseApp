import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentDash extends JFrame {
    private int studentId;
    private DefaultListModel<String> classListModel;
    private JList<String> classList;
    private Connection connection;

    public StudentDash(int studentId, Connection connection) {
        this.studentId = studentId;
        this.connection = connection;

        setTitle("Student Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Fetch and display student name
        String studentName = getStudentName(studentId);

        JLabel welcomeLabel = new JLabel("Student Dashboard - " + studentName, JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(welcomeLabel, BorderLayout.NORTH);

        classListModel = new DefaultListModel<>();
        classList = new JList<>(classListModel);
        classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(classList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("View My Classes");
        JButton dropButton = new JButton("Drop Selected Class");
        buttonPanel.add(refreshButton);
        buttonPanel.add(dropButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        refreshButton.addActionListener(e -> loadStudentClasses());

        dropButton.addActionListener(e -> {
            String selected = classList.getSelectedValue();
            if (selected != null && !selected.equals("No classes registered")) {
                int confirm = JOptionPane.showConfirmDialog(this, "Drop class: " + selected + "?",
                        "Confirm Drop", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dropClass(selected);
                    loadStudentClasses();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a class to drop.");
            }
        });

        // Load classes when dashboard opens
        loadStudentClasses();

        setVisible(true);
    } //end StudentDash

    private String getStudentName(int studentId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT CONCAT(fname, ' ', lname) AS name FROM Users WHERE ID = ?");
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error fetching student name: " + e.getMessage());
        }
        return "Student";
    } //end getStudentName

    private void loadStudentClasses() {
        classListModel.clear();
        try {
            // Use the stored procedure
            CallableStatement stmt = connection.prepareCall("{call GetStudentClasses(?)}");
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            boolean hasClasses = false;
            while (rs.next()) {
                classListModel.addElement(rs.getString("class_name"));
                hasClasses = true;
            }

            // Display a message if no classes are found
            if (!hasClasses) {
                classListModel.addElement("No classes registered");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error loading classes: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading classes: " + e.getMessage());
        }
    } //end loadStudentClasses

    private void dropClass(String className) {
        try {
            // Use the stored procedure
            CallableStatement stmt = connection.prepareCall("{call DropStudentClass(?, ?)}");
            stmt.setInt(1, studentId);
            stmt.setString(2, className);
            stmt.execute();

            JOptionPane.showMessageDialog(this, "Successfully dropped: " + className);

            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error dropping class: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error dropping class: " + e.getMessage());
        }
    } //end dropClass
} //end class StudentDash
