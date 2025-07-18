package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import services.RestClient;

@SuppressWarnings("serial")
public class DoctorRegistrationDialog extends JDialog {
    private JTextField firstNameField, lastNameField, emailField, phoneField, specializationField;
    private JPasswordField passwordField, confirmPasswordField;
    private JLabel statusLabel;
    private RestClient restClient;

    public DoctorRegistrationDialog(JFrame parent, RestClient restClient) {
        super(parent, "Register New Doctor", true);
        this.restClient = restClient;
        initializeComponents();
        setupLayout();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void initializeComponents() {
        setSize(400, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        specializationField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void setupLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        JLabel titleLabel = new JLabel("Doctor Registration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(70, 130, 180));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // Form fields
        String[] labels = {"First Name:", "Last Name:", "Email:", "Phone:", "Specialization:", "Password:", "Confirm Password:"};
        JComponent[] fields = {firstNameField, lastNameField, emailField, phoneField, specializationField, passwordField, confirmPasswordField};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1;
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            panel.add(fields[i], gbc);
        }

        // Buttons
        JButton registerButton = new JButton("Register");
        registerButton.setBackground(new Color(34, 139, 34));
        registerButton.setForeground(Color.WHITE);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = labels.length + 1;
        panel.add(registerButton, gbc);
        gbc.gridx = 1;
        panel.add(cancelButton, gbc);

        // Status label
        gbc.gridx = 0; gbc.gridy = labels.length + 2; gbc.gridwidth = 2;
        panel.add(statusLabel, gbc);

        // Action listeners
        registerButton.addActionListener(e -> registerDoctor());
        cancelButton.addActionListener(e -> dispose());

        add(panel);
    }

    private void registerDoctor() {
        // Validate inputs
        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()
            || emailField.getText().trim().isEmpty() || passwordField.getPassword().length == 0) {
            showMessage("Please fill in all required fields", Color.RED);
            return;
        }

        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        if (!password.equals(confirm)) {
            showMessage("Passwords do not match", Color.RED);
            return;
        }
        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters", Color.RED);
            return;
        }

        showMessage("Registering doctor...", Color.BLUE);

        // Delegate to RestClient in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return restClient.registerDoctor(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    specializationField.getText().trim(),
                    password
                );
            }

            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        showMessage("Doctor registered successfully!", Color.GREEN);
                        new Timer(2000, e -> dispose()).start();
                    } else {
                        showMessage("Registration failed. Email may already exist.", Color.RED);
                    }
                } catch (Exception ex) {
                    showMessage("Registration error: " + ex.getMessage(), Color.RED);
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void showMessage(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
}
