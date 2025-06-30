package gui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Appointment;
import services.RestClient;

public class AppointmentPanel extends JPanel {

    private RestClient restClient;
    private JTable table;
    private JButton btnEdit;
    private JButton btnDelete;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;

    // keep the last loaded appointments to reference IDs
    private List<Appointment> appointments;

    public AppointmentPanel() {
        this.restClient = new RestClient();
        initialize();
        loadAppointments();
    }

    @SuppressWarnings("serial")
    private void initialize() {
        setLayout(null);
        scrollPane = new JScrollPane();
        scrollPane.setBounds(32, 31, 550, 200);
        add(scrollPane);

        tableModel = new DefaultTableModel(
                new String[]{"Appointment ID","Patient", "Doctor", "Date", "Time", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        // hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        scrollPane.setViewportView(table);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(32, 250, 90, 25);
        btnRefresh.addActionListener(e -> loadAppointments());
        add(btnRefresh);

        JButton btnAdd = new JButton("Add Appointment");
        btnAdd.setBounds(132, 250, 140, 25);
        btnAdd.addActionListener(e -> new AppointmentFormWindow(this).setVisible(true));
        add(btnAdd);

        btnEdit = new JButton("Edit");
        btnEdit.setBounds(282, 250, 90, 25);
        btnEdit.addActionListener(e -> editSelectedAppointment());
        add(btnEdit);

        btnDelete = new JButton("Delete");
        btnDelete.setBounds(382, 250, 90, 25);
        btnDelete.addActionListener(e -> deleteSelectedAppointment());
        add(btnDelete);
    }

    public void refreshAppointments() {
        loadAppointments();
    }

    private void loadAppointments() {
        new SwingWorker<List<Appointment>, Void>() {
            @Override
            protected List<Appointment> doInBackground() {
                return restClient.getAllAppointments();
            }

            @Override
            protected void done() {
                try {
                    appointments = get();
                    tableModel.setRowCount(0);
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                    for (Appointment a : appointments) {
                        // format date if present, else empty
                        String dateStr = a.getAppointmentDate() != null
                            ? df.format(a.getAppointmentDate())
                            : "";

                        // time could be null too
                        String timeStr = a.getAppointmentTime() != null
                            ? a.getAppointmentTime()
                            : "";

                        tableModel.addRow(new Object[]{
                            a.getAppointmentId(),
                            a.getPatientName(),
                            a.getDoctorName(),
                            dateStr,
                            timeStr,
                            a.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                        AppointmentPanel.this,
                        "Error loading appointments: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }


    private void editSelectedAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // reconstruct Appointment
        Appointment selected = appointments.get(selectedRow);
        new AppointmentFormWindow(this, selected,true).setVisible(true);
    }

    private void deleteSelectedAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this appointment?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Appointment toDelete = appointments.get(selectedRow);
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return restClient.deleteAppointment(toDelete.getAppointmentId());
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(AppointmentPanel.this,
                                    "Appointment deleted successfully!",
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                            loadAppointments();
                        } else {
                            JOptionPane.showMessageDialog(AppointmentPanel.this,
                                    "Failed to delete appointment.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(AppointmentPanel.this,
                                "Error deleting appointment: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    // independent testing
    public static void main(String[] args) {
        JFrame frame = new JFrame("Test Appointment Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 400);
        frame.getContentPane().add(new AppointmentPanel());
        frame.setVisible(true);
    }
}
