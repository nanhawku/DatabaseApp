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

        JLabel welcomeLabel = new JLabel("Student Dashboard", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(welcomeLabel, BorderLayout.NORTH);

        classListModel = new DefaultListModel<>();
        classList = new JList<>(classListModel);
        JScrollPane scrollPane = new JScrollPane(classList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("View Classes");
        JButton dropButton = new JButton("Drop Selected Class");
        buttonPanel.add(refreshButton);
        buttonPanel.add(dropButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        refreshButton.addActionListener(e -> loadStudentClasses());

        dropButton.addActionListener(e -> {
            String selected = classList.getSelectedValue();
            if (selected != null) {
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

        loadStudentClasses();

        setVisible(true);
    }

    private void loadStudentClasses() {
        classListModel.clear();
        try {
            CallableStatement stmt = connection.prepareCall("{call GetStudentClasses(?)}");
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                classListModel.addElement(rs.getString("class_name"));
            }
            //if no classes found
            if(classListModel.isEmpty()) {
                classListModel.addElement("No Classes Found");
            } //end if

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading classes: " + e.getMessage());
        }
    } //end loadStudentClasses

    private void dropClass(String className) {
        try {
            CallableStatement stmt = connection.prepareCall("{call DropStudentClass(?, ?)}");
            stmt.setInt(1, studentId);
            stmt.setString(2, className);
            stmt.execute();

            JOptionPane.showMessageDialog(this, "Dropped Class: " + className);

            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error dropping class: " + e.getMessage());
        }
    }
}
