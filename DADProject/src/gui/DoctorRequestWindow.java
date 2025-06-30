package gui;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import models.Appointment;
import services.RestClient;

public class DoctorRequestWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private RestClient restClient;
    private int doctorId;
    private DefaultTableModel tableModel;
    private List<Appointment> appointments;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                new DoctorRequestWindow(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public DoctorRequestWindow(int doctorId) {
        this.doctorId   = doctorId;
        this.restClient = new RestClient();

        setTitle("Appointment Requests for Doctor " + doctorId);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        // --- build table ---
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(47, 52, 561, 132);
        getContentPane().add(scrollPane);

        tableModel = new DefaultTableModel(new Object[]{
                "ID", "Patient", "Date", "Time", "Notes", "Status"
        }, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        scrollPane.setViewportView(table);

        // --- buttons ---
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBounds(51, 222, 85, 21);
        refreshButton.addActionListener(e -> loadRequests());
        getContentPane().add(refreshButton);

        JButton approveButton = new JButton("Approve");
        approveButton.setBounds(161, 222, 85, 21);
        approveButton.addActionListener(e ->
            openDoctorFormForSelected("confirmed", "Approved by doctor")
        );
        getContentPane().add(approveButton);

        JButton rejectButton = new JButton("Reject");
        rejectButton.setBounds(271, 222, 85, 21);
        rejectButton.addActionListener(e ->
            openDoctorFormForSelected("cancelled", "Rejected by doctor")
        );
        getContentPane().add(rejectButton);


        // start TCP notification server
        startNotificationServer();

        // initial data load
        loadRequests();

        setSize(660, 320);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Spin up a background thread that listens for "NEW_APPOINTMENT"
     * pings on port (6000 + doctorId) and reloads the table.
     */
    private void startNotificationServer() {
        final int port = 6000 + doctorId;
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                System.out.println("Notification server listening on port " + port);
                while (true) {
                    try (Socket client = server.accept();
                         BufferedReader in = new BufferedReader(
                             new InputStreamReader(client.getInputStream()))
                    ) {
                        String msg = in.readLine();
                        if ("NEW_APPOINTMENT".equals(msg)) {
                            // schedule table reload on the EDT
                            SwingUtilities.invokeLater(this::loadRequests);
                        }
                    } catch (Exception inner) {
                        inner.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Failed to start notification server on port " + port +
                    "\n" + e.getMessage());
            }
        }, "DoctorNotificationServer").start();
    }
    /**
     * Open the AppointmentFormWindow in doctor‐edit mode for the selected row,
     * pre‐setting status to newStatus and notes to defaultNote.
     */
    private void openDoctorFormForSelected(String newStatus, String defaultNote) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment request first.");
            return;
        }
        // pull from your cached list
        Appointment appt = appointments.get(row);
        // set the status/notes defaults
        appt.setStatus(newStatus);
        appt.setNotes(defaultNote);

        // launch the form: (isStaff = false means doctor mode)
        new AppointmentFormWindow(null, appt, false).setVisible(true);
    }


    private void loadRequests() {
        tableModel.setRowCount(0);
        // assign into your field:
        this.appointments = restClient.getAllAppointments();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        for (Appointment a : this.appointments) {
            if (a.getDoctorId() == doctorId &&
                "scheduled".equalsIgnoreCase(a.getStatus())) {

                String dateStr  = a.getAppointmentDate() != null
                    ? df.format(a.getAppointmentDate())
                    : "";
                String timeStr  = a.getAppointmentTime() != null
                    ? a.getAppointmentTime()
                    : "";
                String notesStr = a.getNotes() != null
                    ? a.getNotes()
                    : "";

                tableModel.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getPatientName(),
                    dateStr,
                    timeStr,
                    notesStr,
                    a.getStatus()
                });
            }
        }
    }



    private void updateSelectedStatus(String newStatus, String defaultNote) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment request first.");
            return;
        }

        // 1) Locate the Appointment object
        Appointment appt = appointments.get(row);

        // 2) Ask for a date (pre-fill with existing or today)
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = appt.getAppointmentDate() != null
            ? df.format(appt.getAppointmentDate())
            : df.format(new java.util.Date());
        String dateStr = JOptionPane.showInputDialog(
            this,
            "Enter appointment date (yyyy-MM-dd):",
            currentDate
        );
        if (dateStr == null) return;  // user cancelled

        // 3) Ask for a time (pre-fill with existing or empty)
        String currentTime = appt.getAppointmentTime() != null
            ? appt.getAppointmentTime()
            : "";
        String timeStr = JOptionPane.showInputDialog(
            this,
            "Enter appointment time (HH:mm):",
            currentTime
        );
        if (timeStr == null) return;  // user cancelled

        // 4) Ask for notes (pre-fill with defaultNote or existing)
        String notesStr = JOptionPane.showInputDialog(
            this,
            "Enter doctor’s notes:",
            appt.getNotes() != null ? appt.getNotes() : defaultNote
        );
        if (notesStr == null) return;  // user cancelled

        // 5) Apply all changes to the Appointment object
        try {
            // parse and set the date
            java.util.Date utilDate = df.parse(dateStr);
            appt.setAppointmentDate(new java.sql.Date(utilDate.getTime()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid date format. Use yyyy-MM-dd.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        appt.setAppointmentTime(timeStr);
        appt.setNotes(notesStr);
        appt.setStatus(newStatus);

        // 6) Send the full update to the server
        boolean ok = restClient.updateAppointment(appt);
        if (ok) {
            loadRequests();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to update appointment.");
        }
    }

}
