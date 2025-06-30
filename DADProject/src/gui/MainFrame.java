package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import services.ReminderThread;

public class MainFrame {

    private JFrame frmHospital;
    private ReminderThread reminderThread;

    private PatientPanel patientPanel;
    private AppointmentPanel appointmentPanel;
    private DoctorPanel doctorPanel;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MainFrame window = new MainFrame();
                window.frmHospital.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public MainFrame() {
        initialize();
        initServices();
        attachWindowListener();
    }

    private void initialize() {
        frmHospital = new JFrame("Hospital Appointment System");
        frmHospital.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmHospital.setLayout(new BorderLayout());

        // Create and add the tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        patientPanel = new PatientPanel();
        appointmentPanel = new AppointmentPanel();
        doctorPanel = new DoctorPanel();

        tabbedPane.addTab("Patients", patientPanel);
        tabbedPane.addTab("Appointments", appointmentPanel);
        tabbedPane.addTab("Doctors", doctorPanel);

        frmHospital.add(tabbedPane, BorderLayout.CENTER);

        // ✅ Set larger size (e.g., Full HD)
        frmHospital.setSize(1280, 720); // Width x Height

        // ✅ Allow user to resize and center the window
        frmHospital.setResizable(true);
        frmHospital.setLocationRelativeTo(null); 
    }


    private void initServices() {
        reminderThread = new ReminderThread();
        reminderThread.start();

    }

    private void attachWindowListener() {
        frmHospital.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (reminderThread != null) reminderThread.stopReminder();
            }
        });
    }
}
