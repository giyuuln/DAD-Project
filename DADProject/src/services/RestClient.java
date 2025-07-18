package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import models.Appointment;
import models.Patient;
import models.Doctor;

public class RestClient {
    private static final String BASE_URL = "http://localhost/hospital_management/php_backend";
    private CloseableHttpClient httpClient;

    public RestClient() {
        this.httpClient =  HttpClients.createDefault();
    }

    private String makeHTTPRequest(String url, String method, String jsonData) {
        try {
            HttpUriRequest request;
            switch (method) {
                case "GET":
                    request = new HttpGet(url);
                    break;
                case "POST":
                    HttpPost post = new HttpPost(url);
                    if (jsonData != null) {
                        StringEntity entity = new StringEntity(jsonData, "UTF-8");
                        post.setEntity(entity);
                    }
                    request = post;
                    break;
                case "PUT":
                    HttpPut put = new HttpPut(url);
                    if (jsonData != null) {
                        StringEntity entity = new StringEntity(jsonData, "UTF-8");
                        put.setEntity(entity);
                    }
                    request = put;
                    break;
                case "DELETE":
                    request = new HttpDelete(url);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown method: " + method);
            }

            // ALWAYS set this header on POST/PUT
            if (method.equals("POST") || method.equals("PUT")) {
                request.setHeader("Content-Type", "application/json; charset=UTF-8");
            }

            System.out.println("➡️  " + method + " " + url);
            if (jsonData != null) {
                System.out.println("   Payload: " + jsonData);
            }

            HttpResponse response = httpClient.execute(request);
            int status = response.getStatusLine().getStatusCode();
            System.out.println("⬅️  HTTP " + status);

            HttpEntity entity = response.getEntity();
            String body = entity != null 
                ? EntityUtils.toString(entity, "UTF-8") 
                : "";

            System.out.println("   Body: " + body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // Patient operations
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String response = makeHTTPRequest(BASE_URL + "/patients_api.php", "GET", null);

        if (response != null) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject patientJson = jsonArray.getJSONObject(i);
                    Patient patient = Patient.fromJSON(patientJson);
                    patients.add(patient);
                  
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return patients;
    }
   
    public List<String> getAllPatientNames() {
        List<String> names = new ArrayList<>();
        for (Patient p : getAllPatients()) {
            if (p.getFirstName() != null && p.getLastName() != null) {
                names.add(p.toString());
                
            }
        }
        
        return names;
    }
    

    public boolean createPatient(Patient patient) {
        try {
            URL url = new URL(BASE_URL + "/patients_api.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("firstName", patient.getFirstName());
            json.put("lastName", patient.getLastName());
            json.put("phone", patient.getPhone());
            json.put("email", patient.getEmail());
            json.put("address", patient.getAddress());
            json.put("dateOfBirth", JSONObject.NULL); // optional, if you're not using it yet

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }


            JSONObject resJson = new JSONObject(response.toString());
            return resJson.optBoolean("success", false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean updatePatient(Patient patient) {
        try {
        	String url = BASE_URL + "/patients_api.php/" + patient.getPatientId();

            JSONObject json = patient.toJSON(); // make sure toJSON() returns a JSONObject
            

            String response = makeHTTPRequest(url, "PUT", json.toString());


            if (response != null) {
                JSONObject resJson = new JSONObject(response);
                return resJson.optBoolean("success", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    // Doctor operations
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String response = makeHTTPRequest(BASE_URL + "/doctors_api.php", "GET", null);

        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject doctorJson = jsonArray.getJSONObject(i);
                Doctor doctor = Doctor.fromJSON(doctorJson);
                doctors.add(doctor);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error parsing doctors: " + e.getMessage());
            e.printStackTrace();
        }

        return doctors;
    }
    
    public List<String> getAllDoctorNames() {
        List<String> names = new ArrayList<>();
        for (Doctor d : getAllDoctors()) {
            if (d.getFirstName() != null && d.getLastName() != null) {
                names.add(d.toString());
            }
        }
        return names;
    }



    // Appointment operations
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String response = makeHTTPRequest(BASE_URL + "/appointments_api.php", "GET", null);

        try {
            JSONArray jsonArray = new JSONArray(response);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject appointmentJson = jsonArray.getJSONObject(i);
                Appointment appointment = Appointment.fromJSON(appointmentJson);
                appointments.add(appointment);
            }

        } catch (Exception e) {
            System.err.println("Error parsing appointments: " + e.getMessage());
            e.printStackTrace();
        }

        return appointments;
    }

    public boolean createAppointment(String patientId, String doctorId, String date, String time, String notes) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("patientId", patientId);
        jsonData.put("doctorId", doctorId);
        jsonData.put("appointmentDate", date);
        jsonData.put("appointmentTime", time);
        jsonData.put("notes", notes);

        String response = makeHTTPRequest(BASE_URL + "/appointments_api.php", "POST", jsonData.toString());
        System.out.println("📡 Server response: " + response);

        if (response != null) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                return jsonResponse.optBoolean("success", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * Fetch a single appointment by its ID.
     */
    public Appointment getAppointment(int appointmentId) throws Exception {
        String url = BASE_URL + "/appointments_api.php/" + appointmentId;
        String raw = makeHTTPRequest(url, "GET", null);
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        // if the API returns an object or an array with one element
        raw = raw.trim();
        JSONObject json;
        if (raw.startsWith("[")) {
            JSONArray arr = new JSONArray(raw);
            if (arr.length() == 0) return null;
            json = arr.getJSONObject(0);
        } else {
            json = new JSONObject(raw);
        }
        return Appointment.fromJSON(json);
    }
    /**
     * Update only the status and notes of an appointment.
     * Internally fetches the full appointment, applies changes, and re-submits the entire object.
     */
    public boolean updateAppointmentStatus(int appointmentId, String status, String notes) {
        try {
            // 1. Fetch existing appointment
            Appointment appt = getAppointment(appointmentId);
            if (appt == null) {
                System.err.println("Appointment not found: " + appointmentId);
                return false;
            }

            // 2. Apply new status and notes
            appt.setStatus(status);
            appt.setNotes(notes);

            // 3. Delegate to the full-payload update
            return updateAppointment(appt);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAppointment(Appointment appt) {
        // 1) Build a full JSON payload
        JSONObject data = new JSONObject();
        data.put("patientId",       appt.getPatientId());
        data.put("doctorId",        appt.getDoctorId());
        data.put("appointmentDate", new SimpleDateFormat("yyyy-MM-dd")
                                     .format(appt.getAppointmentDate()));
        data.put("appointmentTime", appt.getAppointmentTime());
        data.put("status",          appt.getStatus());
        data.put("notes",           appt.getNotes());

        // 2) Send the PUT
        String url = BASE_URL + "/appointments_api.php/" + appt.getAppointmentId();
        System.out.println("➡️ PUT " + url);
        System.out.println("   Payload: " + data);
        String raw = makeHTTPRequest(url, "PUT", data.toString());
        System.out.println("⬅️ Body: " + raw);

        if (raw == null) return false;
        raw = raw.trim();
        try {
            JSONObject resp = new JSONObject(raw);
            boolean ok = resp.optBoolean("success", false);
            if (!ok) {
                System.err.println("❌ update failed → "
                  + resp.optString("message", resp.optString("error", raw)));
            }
            return ok;
        } catch (JSONException ex) {
            System.err.println("⚠️ Bad JSON from updateAppointment: >>>" + raw + "<<<");
            ex.printStackTrace();
            return false;
        }
    }


    public boolean updateAppointmentStatus(Appointment appt) {
        // build a FULL JSON payload
        JSONObject data = new JSONObject();
        data.put("patientId",       appt.getPatientId());
        data.put("doctorId",        appt.getDoctorId());
        data.put("appointmentDate", new SimpleDateFormat("yyyy-MM-dd")
                                    .format(appt.getAppointmentDate()));
        data.put("appointmentTime", appt.getAppointmentTime());
        data.put("status",          appt.getStatus());
        data.put("notes",           appt.getNotes());

        String url = BASE_URL + "/appointments_api.php/" + appt.getAppointmentId();
        System.out.println("▶️ PUT "+url+"\n   Payload: "+data);
        String raw = makeHTTPRequest(url, "PUT", data.toString());
        System.out.println("📡 Raw response: "+raw);

        if (raw == null) return false;
        raw = raw.trim();
        try {
            JSONObject resp = new JSONObject(raw);
            boolean ok = resp.optBoolean("success", false);
            if (!ok) {
                System.err.println("❌ update failed → "+ resp.optString("message", resp.optString("error", raw)));
            }
            return ok;
        } catch (JSONException ex) {
            System.err.println("⚠️ Bad JSON from updateAppointment: >>>"+raw+"<<<");
            ex.printStackTrace();
            return false;
        }
    }



    public boolean deleteAppointment(int appointmentId) {
        String response = makeHTTPRequest(BASE_URL + "/appointments_api.php/" + appointmentId, "DELETE", null);

        if (response != null) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                return jsonResponse.optBoolean("success", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    /**
     * Call your PHP login endpoint.
     */
    public JSONObject  authenticateDoctor(String email, String password) {
    	try {
            JSONObject req = new JSONObject()
                .put("email",    email)
                .put("password", password);
            String raw = makeHTTPRequest(
                BASE_URL + "/doctor_auth.php",
                "POST",
                req.toString()
            );
            return raw != null
                 ? new JSONObject(raw)
                 : null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Call your PHP registration endpoint.
     */
    public boolean registerDoctor(String firstName,
                                  String lastName,
                                  String email,
                                  String phone,
                                  String specialization,
                                  String password)
    {
    	try {
            JSONObject req = new JSONObject()
                .put("firstName",      firstName)
                .put("lastName",       lastName)
                .put("email",          email)
                .put("phone",          phone)
                .put("specialization", specialization)
                .put("password",       password);
            String raw = makeHTTPRequest(
                BASE_URL + "/doctor_registration.php",
                "POST",
                req.toString()
            );
            JSONObject resp = new JSONObject(raw);
            return resp.optBoolean("success", false);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void close() {
    	try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper setter for appointment date if needed externally
    public void setAppointmentDate(Appointment appointment, String dateString) {
        try {
            java.util.Date utilDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dateString);
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            appointment.setAppointmentDate(sqlDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public boolean createAppointmentBasic(String pid, String did) {
		// TODO Auto-generated method stub
		return false;
	}
}
