package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import models.Doctor;
import services.RestClient;

import java.util.List;

public class DoctorPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private RestClient restClient;
    private JTable doctorTable;
    private DefaultTableModel tableModel;

    public DoctorPanel() {
        this.restClient = new RestClient();
        setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(35, 33, 700, 250);
        add(scrollPane);

        String[] columns = {"ID", "First Name", "Last Name", "Specialization", "Phone", "Email"};
        tableModel = new DefaultTableModel(columns, 0);
        doctorTable = new JTable(tableModel);
        scrollPane.setViewportView(doctorTable);

        // Load data in the background
        loadDoctorData();
    }

    private void loadDoctorData() {
        SwingWorker<List<Doctor>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Doctor> doInBackground() {
                return restClient.getAllDoctors();
            }

            @Override
            protected void done() {
                try {
                    List<Doctor> doctors = get();
                    tableModel.setRowCount(0); // Clear previous rows

                    for (Doctor d : doctors) {
                        tableModel.addRow(new Object[]{
                            d.getDoctorId(),
                            d.getFirstName(),
                            d.getLastName(),
                            d.getSpecialization(),
                            d.getPhone(),
                            d.getEmail()
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DoctorPanel.this,
                            "Failed to load doctor data.\n" + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
