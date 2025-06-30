package gui;

import javax.swing.*;
import models.Patient;
import services.RestClient;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PatientFormWindow extends JFrame {

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;
    private JTextField dobField;

    private RestClient restClient;
    private PatientPanel parent;
    private Patient patientToEdit;

    public PatientFormWindow(PatientPanel parent, Patient patientToEdit) {
        this.parent = parent;
        this.patientToEdit = patientToEdit;
        this.restClient = new RestClient();

        boolean isEdit = patientToEdit != null;

        setTitle(isEdit ? "Edit Patient" : "Add Patient");
        setSize(385, 370);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(null);

        JLabel lblFirst = new JLabel("First Name:");
        lblFirst.setBounds(10, 30, 80, 20);
        getContentPane().add(lblFirst);

        firstNameField = new JTextField();
        firstNameField.setBounds(180, 30, 160, 20);
        getContentPane().add(firstNameField);

        JLabel lblLast = new JLabel("Last Name:");
        lblLast.setBounds(10, 70, 80, 20);
        getContentPane().add(lblLast);

        lastNameField = new JTextField();
        lastNameField.setBounds(180, 70, 160, 20);
        getContentPane().add(lastNameField);

        JLabel lblPhone = new JLabel("Phone:");
        lblPhone.setBounds(10, 110, 80, 20);
        getContentPane().add(lblPhone);

        phoneField = new JTextField();
        phoneField.setBounds(180, 110, 160, 20);
        getContentPane().add(phoneField);

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setBounds(10, 150, 80, 20);
        getContentPane().add(lblEmail);

        emailField = new JTextField();
        emailField.setBounds(180, 150, 160, 20);
        getContentPane().add(emailField);

        JLabel lblAddress = new JLabel("Address:");
        lblAddress.setBounds(10, 190, 80, 20);
        getContentPane().add(lblAddress);

        addressField = new JTextField();
        addressField.setBounds(180, 191, 160, 20);
        getContentPane().add(addressField);

        JLabel lblDate = new JLabel("Date of Birth (dd-MM-yyyy) :");
        lblDate.setBounds(10, 230, 160, 20);
        getContentPane().add(lblDate);

        dobField = new JTextField();
        dobField.setBounds(180, 231, 160, 20);
        getContentPane().add(dobField);

        JButton btnSave = new JButton(isEdit ? "Update" : "Save");
        btnSave.setBounds(138, 280, 100, 30);
        getContentPane().add(btnSave);

        if (isEdit) {
            firstNameField.setText(patientToEdit.getFirstName());
            lastNameField.setText(patientToEdit.getLastName());
            phoneField.setText(patientToEdit.getPhone());
            emailField.setText(patientToEdit.getEmail());
            addressField.setText(patientToEdit.getAddress());
            if (patientToEdit.getDateOfBirth() != null) {
                dobField.setText(new SimpleDateFormat("yyyy-MM-dd").format(patientToEdit.getDateOfBirth()));
            }
        }

        btnSave.addActionListener(e -> {
            if (isEdit) {
                updatePatient();
            } else {
                createPatient();
            }
        });
    }

    private Date parseDateInput() {
    	try {
    	    String dobText = dobField.getText();  // Assuming dd-MM-yyyy format
    	    if (!dobText.isEmpty()) {
    	        Date dob = new SimpleDateFormat("dd-MM-yyyy").parse(dobText);
    	        return dob;
    	    }
    	} catch (Exception e) {
    	    e.printStackTrace(); // Handle error or show a message
    	}
        return null;
    }

    private void createPatient() {
        Patient p = new Patient();
        p.setFirstName(firstNameField.getText());
        p.setLastName(lastNameField.getText());
        p.setPhone(phoneField.getText());
        p.setEmail(emailField.getText());
        p.setAddress(addressField.getText());
        p.setDateOfBirth(parseDateInput());

        boolean success = restClient.createPatient(p);
        if (success) {
            JOptionPane.showMessageDialog(this, "Patient added successfully.");
            parent.refreshPatientTable();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add patient.");
        }
    }

    private void updatePatient() {
        patientToEdit.setFirstName(firstNameField.getText());
        patientToEdit.setLastName(lastNameField.getText());
        patientToEdit.setPhone(phoneField.getText());
        patientToEdit.setEmail(emailField.getText());
        patientToEdit.setAddress(addressField.getText());
        patientToEdit.setDateOfBirth(parseDateInput());

        boolean success = restClient.updatePatient(patientToEdit);
        if (success) {
            JOptionPane.showMessageDialog(this, "Patient updated successfully.");
            parent.refreshPatientTable();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update patient.");
        }
    }
}
