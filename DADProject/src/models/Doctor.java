package models;

import org.json.JSONObject;

public class Doctor {
    private int doctorId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String phone;
    private String email;

    // Constructors
    public Doctor() {}

    // Getters and Setters
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Return JSON representation
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("doctor_id", doctorId);
        json.put("first_name", firstName);
        json.put("last_name", lastName);
        json.put("specialization", specialization);
        json.put("phone", phone);
        json.put("email", email);
        return json;
    }

    // Convenience method for displaying full name
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public static Doctor fromJSON(JSONObject json) {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(json.optInt("doctor_id"));
        doctor.setFirstName(json.optString("first_name"));
        doctor.setLastName(json.optString("last_name"));
        doctor.setSpecialization(json.optString("specialization"));
        doctor.setPhone(json.optString("phone"));
        doctor.setEmail(json.optString("email"));
        return doctor;
    }

    // For combo boxes or debug printing
    @Override
    public String toString() {
        return getFullName(); // Or use this: "Dr. " + getFullName() + " (" + specialization + ")";
    }
}
