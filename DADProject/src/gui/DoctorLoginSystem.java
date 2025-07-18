package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.json.JSONObject;
import services.RestClient;

public class DoctorLoginSystem {
    private JFrame frame;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private RestClient restClient;
    
    // API endpoint for doctor authentication
    private static final String BASE_URL = "http://localhost/hospital_management/php_backend";
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new DoctorLoginSystem().createAndShowGUI();
        });
    }
    
    public DoctorLoginSystem() {
        this.restClient = new RestClient();
    }
    
    public void createAndShowGUI() {
        frame = new JFrame("Hospital Management - Doctor Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 350);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        
        // Create main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(70, 130, 180), 
                                                   0, getHeight(), new Color(135, 206, 235));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new FlowLayout());
        
        JLabel titleLabel = new JLabel("Doctor Login Portal");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);
        
        // Login form panel
        JPanel loginPanel = new JPanel();
        loginPanel.setOpaque(false);
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Create form fields with styling
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 14));
        emailLabel.setForeground(Color.WHITE);
        
        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 12));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setForeground(Color.WHITE);
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 12));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(34, 139, 34));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton registerButton = new JButton("Register New Doctor");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 12));
        registerButton.setBackground(new Color(70, 130, 180));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add components to login panel
        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);
        
        gbc.gridy = 3;
        loginPanel.add(registerButton, gbc);
        
        gbc.gridy = 4;
        loginPanel.add(statusLabel, gbc);
        
        // Add action listeners
        loginButton.addActionListener(new LoginActionListener());
        registerButton.addActionListener(new RegisterActionListener());
        
        // Add Enter key support
        passwordField.addActionListener(new LoginActionListener());
        
        // Add panels to main frame
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(loginPanel, BorderLayout.CENTER);
        
        frame.add(mainPanel);
        frame.setVisible(true);
        
        // Focus on email field
        emailField.requestFocus();
    }
    
    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (email.isEmpty() || password.isEmpty()) {
                showMessage("Please enter both email and password", Color.RED);
                return;
            }
            
            // Show loading message
            showMessage("Authenticating...", Color.YELLOW);
            
            // Perform authentication in background thread to avoid UI freezing
            SwingWorker<JSONObject, Void> worker = new SwingWorker<JSONObject, Void>() {
                @Override
                protected JSONObject doInBackground() throws Exception {
                    // call our new JSON‐returning method
                    return restClient.authenticateDoctor(email, password);
                }
                @Override
                protected void done() {
                	try {
                        JSONObject resp = get();
                        if (resp != null && resp.optBoolean("success", false)) {
                            int doctorId = resp.getInt("doctor_id");
                            showMessage("Login successful!", Color.GREEN);
                            // give a little pause
                            Timer loginTimer = new Timer(300, ev -> {
                            	        new DoctorRequestWindow(doctorId);
                            	        frame.dispose();
                            	   });
                            	    loginTimer.setRepeats(false);
                            	   loginTimer.start();
                        } else {
                            showMessage("Invalid email or password", Color.RED);
                            passwordField.setText("");
                        }
                    } catch (Exception ex) {
                        showMessage("Login error: " + ex.getMessage(), Color.RED);
                        ex.printStackTrace();
                    }

                }
            };
            worker.execute();
        }
    }
    
    private class RegisterActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new DoctorRegistrationDialog(frame, restClient);
        }
    }
    
 
    
    
    
    private void showMessage(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        
        // Clear message after 5 seconds
        Timer timer = new Timer(5000, e -> statusLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }
    
    @Override
    protected void finalize() throws Throwable {
        if (restClient != null) {
            restClient.close();
        }
        super.finalize();
    }
}
