package services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import models.Appointment;

public class ReminderThread extends Thread{
	 private RestClient restClient;
	    private boolean running = true;
	    
	    public ReminderThread() {
	        this.restClient = new RestClient();
	    }
	    
	    @Override
	    public void run() {
	        while(running) {
	            try {
	                checkUpcomingAppointments();
	                Thread.sleep(300000); // Check every 5 minutes
	            } catch(InterruptedException e) {
	                break;
	            }
	        }
	    }
	    
	    private void checkUpcomingAppointments() {
	        List<Appointment> appointments = restClient.getAllAppointments();
	        Date now = new Date();
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(now);
	        cal.add(Calendar.HOUR, 1); // Check appointments in next hour
	        Date nextHour = cal.getTime();
	        
	        for(Appointment appointment : appointments) {
	            if(appointment.getAppointmentDate() != null && 
	               appointment.getStatus().equals("scheduled")) {
	                
	                Date appointmentDateTime = appointment.getAppointmentDate();
	                
	                if(appointmentDateTime.after(now) && appointmentDateTime.before(nextHour)) {
	                    // Send UDP notification
	                    String message = "Upcoming appointment: " + 
	                                   appointment.getPatientName() + 
	                                   " with " + appointment.getDoctorName() + 
	                                   " at " + appointment.getAppointmentTime();
	                    
	                    
	                    // Also show desktop notification
	                    JOptionPane.showMessageDialog(null, message, "Appointment Reminder", JOptionPane.INFORMATION_MESSAGE);
	                }
	            }
	        }
	    }
	    
	    public void stopReminder() {
	        running = false;
	        this.interrupt();
	    }
}
