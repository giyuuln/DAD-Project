package gui;

import javax.swing.*;
import models.Appointment;
import models.Doctor;
import models.Patient;
import services.RestClient;

import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class AppointmentFormWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private final boolean isStaff;
    private AppointmentPanel parentPanel;
    private Appointment existingAppointment;
    private RestClient restClient;

    private JComboBox<ComboItem> patientCombo;
    private JComboBox<ComboItem> doctorCombo;
    private JTextField dateField;
    private JTextField timeField;
    private JTextField notesField;
    private JComboBox<String> statusCombo;
    private JButton saveButton;

    /** Staff-only constructor (create new) */
    public AppointmentFormWindow(AppointmentPanel parent) {
        this(parent, null, true);
    }

    /** Doctor-only constructor (edit existing) */
    public AppointmentFormWindow(AppointmentPanel parent, Appointment existingAppointment) {
        this(parent, existingAppointment, false);
    }

    /**
     * Main constructor
     * @param isStaff true if front-desk staff is creating; false if doctor is editing
     * @wbp.parser.constructor
     */
    public AppointmentFormWindow(AppointmentPanel parent,
                                 Appointment existingAppointment,
                                 boolean isStaff) {
        this.parentPanel        = parent;
        this.existingAppointment = existingAppointment;
        this.isStaff            = isStaff;
        this.restClient         = new RestClient();

        String title;
        if (existingAppointment == null) {
            title = isStaff
                  ? "Staff: Request Appointment": "Staff: Request Appointment"
                  ;
        } else {
            title = isStaff
                  ? "Staff: Edit Appointment"
                  : "Doctor: Approve Appointment";
        }
        setTitle(title);
        setSize(400, 380);
        setLocationRelativeTo(null);
        getContentPane().setLayout(null);

        // Patient
        JLabel patientLabel = new JLabel("Patient:");
        patientLabel.setBounds(30, 20, 100, 25);
        getContentPane().add(patientLabel);

        patientCombo = new JComboBox<>();
        patientCombo.setBounds(150, 20, 200, 25);
        getContentPane().add(patientCombo);

        // Doctor
        JLabel doctorLabel = new JLabel("Doctor:");
        doctorLabel.setBounds(30, 60, 100, 25);
        getContentPane().add(doctorLabel);

        doctorCombo = new JComboBox<>();
        doctorCombo.setBounds(150, 60, 200, 25);
        getContentPane().add(doctorCombo);

        // Date
        JLabel dateLabel = new JLabel("Date (yyyy-MM-dd):");
        dateLabel.setBounds(30, 100, 120, 25);
        getContentPane().add(dateLabel);

        dateField = new JTextField();
        dateField.setBounds(150, 100, 200, 25);
        getContentPane().add(dateField);

        // Time
        JLabel timeLabel = new JLabel("Time (HH:mm):");
        timeLabel.setBounds(30, 140, 120, 25);
        getContentPane().add(timeLabel);

        timeField = new JTextField();
        timeField.setBounds(150, 140, 200, 25);
        getContentPane().add(timeField);

        // Notes
        JLabel notesLabel = new JLabel("Notes:");
        notesLabel.setBounds(30, 180, 120, 25);
        getContentPane().add(notesLabel);

        notesField = new JTextField();
        notesField.setBounds(150, 180, 200, 25);
        getContentPane().add(notesField);

        // Status
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setBounds(30, 220, 120, 25);
        getContentPane().add(statusLabel);

        statusCombo = new JComboBox<>(new String[]{"On Hold", "Approved", "Cancelled"});
        statusCombo.setBounds(150, 220, 200, 25);
        getContentPane().add(statusCombo);

        // Save button
        saveButton = new JButton(existingAppointment == null ? "Request" : "Update");
        saveButton.setBounds(150, 280, 90, 25);
        saveButton.addActionListener(e -> saveAppointment());
        getContentPane().add(saveButton);

        // load patients & doctors
        loadComboData();

        if (existingAppointment != null) {
            // always pre-fill the form from the appointment!
            fillFormWithAppointment(existingAppointment);

            if (isStaff) {
                // staff edit: only patient & doctor should remain editable
                // everything else disabled
            	patientCombo.setEnabled(false);
                dateField .setEnabled(false);
                timeField .setEnabled(false);
                notesField.setEnabled(false);
                statusCombo.setEnabled(false);
            } else {
                // doctor edit: only date/time/notes/status editable
                patientCombo.setEnabled(false);
                doctorCombo .setEnabled(false);
            }
        }
        else if (isStaff) {
            // new-appointment *creation* by staff: 
            // disable everything except patient & doctor
            dateField .setEnabled(false);
            timeField .setEnabled(false);
            notesField.setEnabled(false);
            statusCombo.setEnabled(false);
        }


        setVisible(true);
    }

    private static class ComboItem {
        private String id, label;
        public ComboItem(String id, String label) {
            this.id    = id;
            this.label = label;
        }
        @Override public String toString() { return label; }
        public String getId() { return id; }
    }

    private void loadComboData() {
        patientCombo.removeAllItems();
        doctorCombo .removeAllItems();
        try {
            for (Patient p : restClient.getAllPatients()) {
                patientCombo.addItem(new ComboItem(
                    String.valueOf(p.getPatientId()), p.getFullName()));
            }
            for (Doctor d : restClient.getAllDoctors()) {
                doctorCombo.addItem(new ComboItem(
                    String.valueOf(d.getDoctorId()), d.getFullName()));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Unable to load lists: " + ex.getMessage());
        }
    }

    private void fillFormWithAppointment(Appointment a) {
        // select patient
        String pid = String.valueOf(a.getPatientId());
        for (int i = 0; i < patientCombo.getItemCount(); i++) {
            if (patientCombo.getItemAt(i).getId().equals(pid)) {
                patientCombo.setSelectedIndex(i);
                break;
            }
        }
        // select doctor
        String did = String.valueOf(a.getDoctorId());
        for (int i = 0; i < doctorCombo.getItemCount(); i++) {
            if (doctorCombo.getItemAt(i).getId().equals(did)) {
                doctorCombo.setSelectedIndex(i);
                break;
            }
        }

        // fill date if present, else blank
        if (a.getAppointmentDate() != null) {
            dateField.setText(new SimpleDateFormat("yyyy-MM-dd")
                                  .format(a.getAppointmentDate()));
        } else {
            dateField.setText("");
        }

        // fill time if present, else blank
        if (a.getAppointmentTime() != null) {
            timeField.setText(a.getAppointmentTime());
        } else {
            timeField.setText("");
        }

        // fill notes if present, else blank
        if (a.getNotes() != null) {
            notesField.setText(a.getNotes());
        } else {
            notesField.setText("");
        }

        // status (always non-null in your DB, but guard just in case)
        statusCombo.setSelectedItem(
            a.getStatus() != null ? a.getStatus() : "On Hold"
        );
    }


    private void saveAppointment() {
        try {
            String pid   = ((ComboItem)patientCombo.getSelectedItem()).getId();
            String did   = ((ComboItem)doctorCombo .getSelectedItem()).getId();
            String date  = dateField.getText();
            String time  = timeField.getText();
            String notes = notesField.getText();
            String status= (String)statusCombo.getSelectedItem();

            boolean ok;
            if (existingAppointment == null) {
                // always call createAppointment; pass nulls if staff
                String d = isStaff ? null : date;
                String t = isStaff ? null : time;
                String n = isStaff ? null : notes;
                ok = restClient.createAppointment(pid, did, d, t, n);

                // if staff, ping doctor after create
                if (ok && isStaff) {
                    notifyDoctor(Integer.parseInt(did));
                }
            } else {
                // doctor updating existing appointment
                existingAppointment.setAppointmentDate(
                    java.sql.Date.valueOf(date));
                existingAppointment.setAppointmentTime(time);
                existingAppointment.setNotes(notes);
                existingAppointment.setStatus(status);
                ok = restClient.updateAppointment(existingAppointment);
            }

            if (ok) {
                parentPanel.refreshAppointments();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Save operation failed");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage());
        }
    }

    /** One-shot TCP ping to the doctor's listener. */
    private void notifyDoctor(int doctorId) {
        final int port = 6000 + doctorId;
        new Thread(() -> {
            try (Socket sock = new Socket("localhost", port);
                 PrintWriter out = new PrintWriter(
                     sock.getOutputStream(), true)) {
                out.println("NEW_APPOINTMENT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "NotifyDoctor-" + doctorId).start();
    }
}
