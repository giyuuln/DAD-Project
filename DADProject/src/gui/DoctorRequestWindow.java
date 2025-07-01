package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.LineBorder;
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

        JButton approveButton = new JButton("Approve & Schedule");
        approveButton.setBounds(161, 222, 130, 21);
        approveButton.addActionListener(e -> approveAndScheduleSelected());
        getContentPane().add(approveButton);

        JButton rejectButton = new JButton("Reject");
        rejectButton.setBounds(310, 222, 85, 21);
        rejectButton.addActionListener(e -> rejectSelected());
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
     * Approve and schedule the selected appointment
     */
    private void approveAndScheduleSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment request first.");
            return;
        }

        // Get the selected appointment
        Appointment appt = appointments.get(row);

        // Show calendar scheduling dialog
        CalendarSchedulingDialog dialog = new CalendarSchedulingDialog(this, appt);
        dialog.setVisible(true);

        // If user confirmed the scheduling
        if (dialog.isConfirmed()) {
            Date selectedDate = dialog.getSelectedDate();
            String selectedTime = dialog.getSelectedTime();
            String notes = dialog.getNotes();

            // Update the appointment
            appt.setAppointmentDate(new java.sql.Date(selectedDate.getTime()));
            appt.setAppointmentTime(selectedTime);
            appt.setNotes(notes);
            appt.setStatus("scheduled");

            boolean success = restClient.updateAppointment(appt);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Appointment approved and scheduled successfully!");
                loadRequests(); // Refresh the table
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to update appointment.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Reject the selected appointment
     */
    private void rejectSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment request first.");
            return;
        }

        // Confirm rejection
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reject this appointment?",
            "Confirm Rejection",
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            Appointment appt = appointments.get(row);
            
            // Ask for rejection reason
            String reason = JOptionPane.showInputDialog(this,
                "Enter reason for rejection (optional):",
                "Rejected by doctor");
            
            if (reason == null) {
                reason = "Rejected by doctor";
            }
            
            appt.setStatus("cancelled");
            appt.setNotes(reason);
            
            boolean success = restClient.updateAppointment(appt);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Appointment rejected successfully.");
                loadRequests(); // Refresh the table
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to reject appointment.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Load appointment requests for this doctor
     */
    private void loadRequests() {
        tableModel.setRowCount(0);
        this.appointments = restClient.getAllAppointments();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        for (Appointment a : this.appointments) {
            // Only show appointments for this doctor that are in "scheduled" status
            // (pending approval from doctor)
            if (a.getDoctorId() == doctorId) {

                String dateStr  = a.getAppointmentDate() != null
                    ? df.format(a.getAppointmentDate())
                    : "Not set";
                String timeStr  = a.getAppointmentTime() != null
                    ? a.getAppointmentTime()
                    : "Not set";
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

    /**
     * Custom Calendar Scheduling Dialog
     */
    private class CalendarSchedulingDialog extends JDialog {
        private static final long serialVersionUID = 1L;
        
        private boolean confirmed = false;
        private Date selectedDate;
        private String selectedTime;
        private String notes;
        
        private JPanel calendarPanel;
        private JComboBox<String> timeComboBox;
        private JTextArea notesArea;
        private Calendar calendar;
        private JLabel monthYearLabel;
        private JButton[][] dayButtons;
        private JButton selectedDayButton;

        public CalendarSchedulingDialog(JFrame parent, Appointment appointment) {
            super(parent, "Schedule Appointment - " + appointment.getPatientName(), true);
            this.calendar = Calendar.getInstance();
            
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            updateCalendarDisplay();
            
            // Pre-fill with existing appointment data if available
            if (appointment.getAppointmentTime() != null) {
                timeComboBox.setSelectedItem(appointment.getAppointmentTime());
            }
            if (appointment.getNotes() != null) {
                notesArea.setText(appointment.getNotes());
            }
            
            setSize(500, 600);
            setLocationRelativeTo(parent);
        }

        private void initializeComponents() {
            // Calendar navigation
            JButton prevMonthBtn = new JButton("◀");
            JButton nextMonthBtn = new JButton("▶");
            monthYearLabel = new JLabel("", SwingConstants.CENTER);
            monthYearLabel.setFont(new Font("Arial", Font.BOLD, 16));
            
            // Calendar grid
            calendarPanel = new JPanel(new GridLayout(7, 7, 2, 2));
            dayButtons = new JButton[6][7]; // 6 weeks, 7 days
            
            // Days of week header
            String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String day : daysOfWeek) {
                JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
                dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
                dayLabel.setOpaque(true);
                dayLabel.setBackground(Color.LIGHT_GRAY);
                calendarPanel.add(dayLabel);
            }
            
            // Calendar day buttons
            for (int week = 0; week < 6; week++) {
                for (int day = 0; day < 7; day++) {
                    JButton dayBtn = new JButton();
                    dayBtn.setPreferredSize(new Dimension(40, 40));
                    dayBtn.setFocusPainted(false);
                    dayButtons[week][day] = dayBtn;
                    calendarPanel.add(dayBtn);
                    
                    // Add click listener
                    final int w = week, d = day;
                    dayBtn.addActionListener(e -> selectDay(w, d));
                }
            }
            
            // Time selection
            timeComboBox = new JComboBox<>();
            String[] timeSlots = {
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30",
                "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30"
            };
            for (String time : timeSlots) {
                timeComboBox.addItem(time);
            }
            timeComboBox.setSelectedItem("09:00");
            
            // Notes
            notesArea = new JTextArea(3, 30);
            notesArea.setText("Approved and scheduled by doctor");
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            
            // Navigation buttons
            prevMonthBtn.addActionListener(e -> {
                calendar.add(Calendar.MONTH, -1);
                updateCalendarDisplay();
            });
            
            nextMonthBtn.addActionListener(e -> {
                calendar.add(Calendar.MONTH, 1);
                updateCalendarDisplay();
            });
        }

        private void setupLayout() {
            setLayout(new BorderLayout());
            
            // Top panel - Calendar navigation
            JPanel topPanel = new JPanel(new BorderLayout());
            JButton prevMonthBtn = new JButton("◀");
            JButton nextMonthBtn = new JButton("▶");
            
            prevMonthBtn.addActionListener(e -> {
                calendar.add(Calendar.MONTH, -1);
                updateCalendarDisplay();
            });
            
            nextMonthBtn.addActionListener(e -> {
                calendar.add(Calendar.MONTH, 1);
                updateCalendarDisplay();
            });
            
            topPanel.add(prevMonthBtn, BorderLayout.WEST);
            topPanel.add(monthYearLabel, BorderLayout.CENTER);
            topPanel.add(nextMonthBtn, BorderLayout.EAST);
            
            // Center panel - Calendar
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(topPanel, BorderLayout.NORTH);
            centerPanel.add(calendarPanel, BorderLayout.CENTER);
            centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Bottom panel - Time, Notes, and Buttons
            JPanel bottomPanel = new JPanel(new BorderLayout());
            
            // Time selection panel
            JPanel timePanel = new JPanel(new FlowLayout());
            timePanel.add(new JLabel("Time:"));
            timePanel.add(timeComboBox);
            
            // Notes panel
            JPanel notesPanel = new JPanel(new BorderLayout());
            notesPanel.add(new JLabel("Notes:"), BorderLayout.NORTH);
            notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);
            notesPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton confirmBtn = new JButton("Confirm Schedule");
            JButton cancelBtn = new JButton("Cancel");
            
            confirmBtn.addActionListener(e -> confirmScheduling());
            cancelBtn.addActionListener(e -> dispose());
            
            buttonPanel.add(confirmBtn);
            buttonPanel.add(cancelBtn);
            
            bottomPanel.add(timePanel, BorderLayout.NORTH);
            bottomPanel.add(notesPanel, BorderLayout.CENTER);
            bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            add(centerPanel, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
        }

        private void setupEventHandlers() {
            // Event handlers are set up in initializeComponents()
        }

        private void updateCalendarDisplay() {
            // Update month/year label
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy");
            monthYearLabel.setText(monthYearFormat.format(calendar.getTime()));
            
            // Get first day of the month
            Calendar firstDay = (Calendar) calendar.clone();
            firstDay.set(Calendar.DAY_OF_MONTH, 1);
            
            // Get today's date for comparison
            Calendar today = Calendar.getInstance();
            
            // Clear previous selection
            selectedDayButton = null;
            
            // Calculate starting position
            int startDay = firstDay.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday
            int daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH);
            
            // Reset all buttons
            for (int week = 0; week < 6; week++) {
                for (int day = 0; day < 7; day++) {
                    dayButtons[week][day].setText("");
                    dayButtons[week][day].setEnabled(false);
                    dayButtons[week][day].setBackground(null);
                    dayButtons[week][day].setBorder(null);
                }
            }
            
            // Fill in the days
            for (int day = 1; day <= daysInMonth; day++) {
                int week = (startDay + day - 1) / 7;
                int dayOfWeek = (startDay + day - 1) % 7;
                
                if (week < 6) {
                    JButton dayBtn = dayButtons[week][dayOfWeek];
                    dayBtn.setText(String.valueOf(day));
                    
                    // Check if this date is today or in the future
                    Calendar dayDate = (Calendar) firstDay.clone();
                    dayDate.set(Calendar.DAY_OF_MONTH, day);
                    
                    if (dayDate.get(Calendar.YEAR) > today.get(Calendar.YEAR) ||
                        (dayDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                         dayDate.get(Calendar.DAY_OF_YEAR) >= today.get(Calendar.DAY_OF_YEAR))) {
                        dayBtn.setEnabled(true);
                        dayBtn.setBackground(Color.WHITE);
                    } else {
                        dayBtn.setEnabled(false);
                        dayBtn.setBackground(Color.LIGHT_GRAY);
                    }
                    
                    dayBtn.setBorder(new LineBorder(Color.GRAY));
                }
            }
        }

        private void selectDay(int week, int day) {
            JButton clickedBtn = dayButtons[week][day];
            if (!clickedBtn.isEnabled() || clickedBtn.getText().isEmpty()) {
                return;
            }
            
            // Reset previous selection
            if (selectedDayButton != null) {
                selectedDayButton.setBackground(Color.WHITE);
                selectedDayButton.setBorder(new LineBorder(Color.GRAY));
            }
            
            // Highlight selected day
            clickedBtn.setBackground(Color.BLUE);
            clickedBtn.setBorder(new LineBorder(Color.DARK_GRAY, 2));
            selectedDayButton = clickedBtn;
            
            // Calculate selected date
            int selectedDay = Integer.parseInt(clickedBtn.getText());
            Calendar selectedCal = (Calendar) calendar.clone();
            selectedCal.set(Calendar.DAY_OF_MONTH, selectedDay);
            selectedDate = selectedCal.getTime();
        }

        private void confirmScheduling() {
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(this,
                    "Please select a date from the calendar.",
                    "No Date Selected",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            selectedTime = (String) timeComboBox.getSelectedItem();
            notes = notesArea.getText().trim();
            
            if (notes.isEmpty()) {
                notes = "Approved and scheduled by doctor";
            }
            
            confirmed = true;
            dispose();
        }

        // Getters
        public boolean isConfirmed() { return confirmed; }
        public Date getSelectedDate() { return selectedDate; }
        public String getSelectedTime() { return selectedTime; }
        public String getNotes() { return notes; }
    }
}
