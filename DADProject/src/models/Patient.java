package models;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;

public class Patient {
    private int patientId;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String address;
    private Date dateOfBirth;

    public Patient() {}

    public Patient(String firstName, String lastName, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
    }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("patient_id", patientId);
        
            json.put("firstName", firstName);   // ✅ Correct key
            json.put("lastName", lastName);     // ✅ Correct key
            json.put("phone", phone);
            json.put("email", email);
            json.put("address", address);
            if (dateOfBirth != null) {
                String dobStr = new SimpleDateFormat("yyyy-MM-dd").format(dateOfBirth);
                json.put("dateOfBirth", dobStr);
            } else {
                json.put("dateOfBirth", JSONObject.NULL);
            }
            return json;
            

    }

    public static Patient fromJSON(JSONObject json) {
        Patient patient = new Patient();
        patient.setPatientId(json.optInt("patient_id"));
        patient.setFirstName(json.optString("first_name"));
        patient.setLastName(json.optString("last_name"));
        patient.setPhone(json.optString("phone"));
        patient.setEmail(json.optString("email"));
        patient.setAddress(json.optString("address"));

        if (json.has("date_of_birth") && !json.isNull("date_of_birth")) {
            try {
                String dobString = json.getString("date_of_birth");
                Date dob = new SimpleDateFormat("yyyy-MM-dd").parse(dobString);
                patient.setDateOfBirth(dob);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return patient;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getFullName(); // Automatically returns full name
    }
}
