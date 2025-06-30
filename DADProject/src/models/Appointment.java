package models;

import java.util.Date;
import org.json.JSONObject;

public class Appointment {
    private int appointmentId;
    private int patientId;
    private int doctorId;
    private Date appointmentDate;
    private String appointmentTime;
    private String status;
    private String notes;
    private String patientName;
    private String doctorName;

    public Appointment() {}

    public int getAppointmentId() {
        return appointmentId;
    }
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }
    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }
    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }
    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPatientName() {
        return patientName;
    }
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    // Serialize to JSON with snake_case keys
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("appointment_id", appointmentId);
        json.put("patient_id", patientId);
        json.put("doctor_id", doctorId);
        json.put("appointment_date", appointmentDate != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(appointmentDate) : JSONObject.NULL);
        json.put("appointment_time", appointmentTime);
        json.put("status", status);
        json.put("notes", notes);
        return json;
    }

    // Deserialize from JSON with snake_case keys
    public static Appointment fromJSON(JSONObject json) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(json.optInt("appointment_id"));
        appointment.setPatientId(json.optInt("patient_id"));
        appointment.setDoctorId(json.optInt("doctor_id"));
        String dateStr = json.optString("appointment_date", "");
        if (!dateStr.isEmpty() && !dateStr.equals("null")) {
            try {
                appointment.setAppointmentDate(java.sql.Date.valueOf(dateStr));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        appointment.setAppointmentTime(json.optString("appointment_time"));
        appointment.setStatus(json.optString("status"));
        appointment.setNotes(json.optString("notes"));
        appointment.setPatientName(json.optString("patient_name"));
        appointment.setDoctorName(json.optString("doctor_name"));
        return appointment;
    }
}
