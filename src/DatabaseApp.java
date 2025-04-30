import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DatabaseApp extends JFrame implements ActionListener {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private int loginAttempts = 0;
    private final int MAX_ATTEMPTS = 3;

    // Use AggieAdmin credentials as defined in the SQL file
    private final String DB_URL = "jdbc:mysql://localhost:3306/ncat";
    private final String DB_USER = "AggieAdmin";
    private final String DB_PASSWORD = "AggiePride1";

    public DatabaseApp() {
        setTitle("NCAT Database Login");
        setSize(300, 170);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Login");

        loginButton.addActionListener(this);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);

        add(panel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        String role = authenticateUser(username, password);
        if (role != null) {
            JOptionPane.showMessageDialog(this, "Login successful as " + role + "!");
            try {
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                if (role.equalsIgnoreCase("manager")) {
                    new ManagerDashboard(DB_URL, DB_USER, DB_PASSWORD);
                } else if (role.equalsIgnoreCase("student")) {
                    // Retrieve the student ID from the database
                    String query = "SELECT ID FROM Users WHERE username = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        int studentId = rs.getInt("ID");
                        new StudentDash(studentId, conn);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Database connection error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            dispose(); // close login window
        } else {
            loginAttempts++;
            if (loginAttempts >= MAX_ATTEMPTS) {
                JOptionPane.showMessageDialog(this,
                        "Too many failed attempts. Application will now exit.",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid credentials! Attempts remaining: " + (MAX_ATTEMPTS - loginAttempts),
                        "Login Failed", JOptionPane.WARNING_MESSAGE);
                usernameField.setText("");
                passwordField.setText("");
            }
        }
    }

    private String authenticateUser(String username, String password) {
        String role = null;

        try {
            // Load JDBC driver
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this,
                        "MySQL JDBC Driver not found! Please install MySQL Connector/J.",
                        "Driver Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            // Test connection
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection successful!");

            // Try the authentication
            String call = "{CALL authenticate_user(?, ?, ?)}";
            try (CallableStatement stmt = conn.prepareCall(call)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.registerOutParameter(3, Types.VARCHAR);

                stmt.execute();
                role = stmt.getString(3);
                System.out.println("Authentication result: " + (role != null ? "Success as " + role : "Failed"));
            }

            conn.close();
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database connection error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        return role;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DatabaseApp::new);
    }
}
