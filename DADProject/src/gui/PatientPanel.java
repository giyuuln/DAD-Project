package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Patient;
import services.RestClient;

import java.awt.event.ActionEvent;
import java.util.*;

@SuppressWarnings("serial")
public class PatientPanel extends JPanel {

    private RestClient restClient;
    private DefaultTableModel tableModel;
    private JTable table;

    public PatientPanel() {
        setLayout(null);
        this.restClient = new RestClient();

        tableModel = new DefaultTableModel(
            new String[]{"ID", "First Name", "Last Name", "Phone", "Email", "Address", "Date of Birth"}, 0
        );
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(10, 29, 800, 300);
        add(scrollPane);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(10, 350, 85, 21);
        btnRefresh.addActionListener(e -> loadPatients());
        add(btnRefresh);

        JButton btnAdd = new JButton("Add");
        btnAdd.setBounds(112, 350, 85, 21);
        btnAdd.addActionListener(e -> new PatientFormWindow(this, null).setVisible(true));
        add(btnAdd);

        JButton btnEdit = new JButton("Edit");
        btnEdit.setBounds(214, 350, 85, 21);
        btnEdit.addActionListener(this::handleEdit);
        add(btnEdit);

        loadPatients();
    }

    private void handleEdit(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient to edit.");
            return;
        }

        Patient selectedPatient = new Patient();
        selectedPatient.setPatientId((Integer) tableModel.getValueAt(selectedRow, 0));
        selectedPatient.setFirstName((String) tableModel.getValueAt(selectedRow, 1));
        selectedPatient.setLastName((String) tableModel.getValueAt(selectedRow, 2));
        selectedPatient.setPhone((String) tableModel.getValueAt(selectedRow, 3));
        selectedPatient.setEmail((String) tableModel.getValueAt(selectedRow, 4));
        selectedPatient.setAddress((String) tableModel.getValueAt(selectedRow, 5));

        try {
            String dobString = (String) tableModel.getValueAt(selectedRow, 6);
            if (dobString != null && !dobString.isEmpty()) {
                Date dob = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dobString);
                selectedPatient.setDateOfBirth(dob);
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // optional: show a warning dialog
        }

        new PatientFormWindow(this, selectedPatient).setVisible(true);
    }


    public void refreshPatientTable() {
        loadPatients();
    }

    private void loadPatients() {
        new SwingWorker<List<Patient>, Void>() {
            @Override
            protected List<Patient> doInBackground() {
                return restClient.getAllPatients();
            }

            @Override
            protected void done() {
                try {
                    List<Patient> list = get();
                    tableModel.setRowCount(0);
                    for (Patient p : list) {
                        tableModel.addRow(new Object[]{
                            p.getPatientId(),
                            p.getFirstName(),
                            p.getLastName(),
                            p.getPhone(),
                            p.getEmail(),
                            p.getAddress(),
                            p.getDateOfBirth() != null
                            ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(p.getDateOfBirth())
                            : ""
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        JFrame testFrame = new JFrame("Test Patient Panel");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setSize(850, 500);
        testFrame.getContentPane().add(new PatientPanel());
        testFrame.setVisible(true);
    }
}
