<?php
include 'config.php';

$method = $_SERVER['REQUEST_METHOD'];
$request = isset($_SERVER['PATH_INFO']) ? explode('/', trim($_SERVER['PATH_INFO'], '/')) : [];
error_log("== appointments_api.php hit ==");
error_log("METHOD: " . $_SERVER['REQUEST_METHOD']);
error_log("PATH_INFO: " . ($_SERVER['PATH_INFO'] ?? 'NULL'));
$raw = file_get_contents('php://input');
error_log("RAW BODY: " . $raw);
switch($method) {
    case 'GET':
        if(isset($request[0]) && is_numeric($request[0])) {
            getAppointment($pdo, $request[0]);
        } else {
            getAllAppointments($pdo);
        }
        break;
    case 'POST':
        createAppointment($pdo);
        break;
    case 'PUT':
        if(isset($request[0]) && is_numeric($request[0])) {
            updateAppointment($pdo, $request[0]);
        }
        break;
    case 'DELETE':
        if(isset($request[0]) && is_numeric($request[0])) {
            deleteAppointment($pdo, $request[0]);
        }
        break;
}
function getAppointment($pdo, $id) {
    $sql = "SELECT a.*, 
                   CONCAT(p.first_name, ' ', p.last_name) as patient_name,
                   CONCAT(d.first_name, ' ', d.last_name) as doctor_name,
                   d.specialization
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON a.doctor_id = d.doctor_id
            WHERE a.appointment_id = ?";
    
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$id]);
    $appointment = $stmt->fetch(PDO::FETCH_ASSOC);
    
    echo json_encode($appointment ? $appointment : ["error" => "Appointment not found"]);
}


function getAllAppointments($pdo) {
    $sql = "SELECT a.*, 
                   CONCAT(p.first_name, ' ', p.last_name) as patient_name,
                   CONCAT(d.first_name, ' ', d.last_name) as doctor_name,
                   d.specialization
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON a.doctor_id = d.doctor_id
            ORDER BY a.appointment_date, a.appointment_time";
    
    $stmt = $pdo->query($sql);
    $appointments = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode($appointments);
}

function deleteAppointment($pdo, $id) {
    $stmt = $pdo->prepare("DELETE FROM appointments WHERE appointment_id = ?");
    
    try {
        $stmt->execute([$id]);
        echo json_encode([
            "success" => true,
            "message" => "Appointment deleted successfully"
        ]);
    } catch(Exception $e) {
        echo json_encode(["error" => $e->getMessage()]);
    }
}

function createAppointment($pdo) {
    // 0. suppress PHP notices/warnings in JSON responses
    ini_set('display_errors', 0);
    error_reporting(E_ALL & ~E_NOTICE & ~E_WARNING);

    // 1. read & decode JSON
    $raw = file_get_contents('php://input');
    $data = json_decode($raw, true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        echo json_encode([
            'success' => false,
            'error'   => 'Invalid JSON: ' . json_last_error_msg(),
            'raw'     => $raw
        ]);
        return;
    }

    // 2. pull out parameters, defaulting to NULL if missing
    $patientId       = $data['patientId']       ?? null;
    $doctorId        = $data['doctorId']        ?? null;
    $appointmentDate = $data['appointmentDate'] ?? null;
    $appointmentTime = $data['appointmentTime'] ?? null;
    $notes           = $data['notes']           ?? null;

    // 3. validate required fields
    if (!$patientId || !$doctorId) {
        echo json_encode([
            'success' => false,
            'message' => 'Missing required patientId or doctorId'
        ]);
        return;
    }

    // 4. prepare & execute INSERT (NULLs will insert as NULL)
    $sql = "
      INSERT INTO appointments
        (patient_id, doctor_id, appointment_date, appointment_time, notes, status)
      VALUES
        (?, ?, ?, ?, ?, 'scheduled')
    ";
    $stmt = $pdo->prepare($sql);

    try {
        $stmt->execute([
            $patientId,
            $doctorId,
            // if null, PDO will bind as SQL NULL
            $appointmentDate,
            $appointmentTime,
            $notes
        ]);

        echo json_encode([
            'success'        => true,
            'appointment_id' => $pdo->lastInsertId(),
            'message'        => 'Appointment created successfully'
        ]);

    } catch (Exception $e) {
        // return the exception message as JSON
        echo json_encode([
            'success' => false,
            'error'   => $e->getMessage()
        ]);
    }
}


function updateAppointment($pdo, $id) {
    // 1. grab and decode
    $raw  = file_get_contents('php://input');
    $data = json_decode($raw, true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        echo json_encode([
          'success' => false,
          'error'   => 'Invalid JSON: ' . json_last_error_msg(),
          'raw'     => $raw
        ]);
        return;
    }

    // 2. pull out your fields (adjust keys to match your client!)
    $patientId       = $data['patientId']        ?? null;
    $doctorId        = $data['doctorId']         ?? null;
    $appointmentDate = $data['appointmentDate']  ?? null;
    $appointmentTime = $data['appointmentTime']  ?? null;
    $status          = $data['status']           ?? null;
    $notes           = $data['notes']            ?? '';

    // 3. validate
    if (!$patientId || !$doctorId || !$appointmentDate || !$appointmentTime) {
        echo json_encode([
          'success' => false,
          'message' => 'Missing one of: patientId, doctorId, appointmentDate, appointmentTime'
        ]);
        return;
    }

    // 4. execute update
    $stmt = $pdo->prepare("
      UPDATE appointments
         SET patient_id      = ?,
             doctor_id       = ?,
             appointment_date= ?,
             appointment_time= ?,
             status          = ?,
             notes           = ?
       WHERE appointment_id = ?
    ");
    try {
        $stmt->execute([
          $patientId,
          $doctorId,
          $appointmentDate,
          $appointmentTime,
          $status,
          $notes,
          $id
        ]);
        echo json_encode([
          'success' => true,
          'message' => 'Appointment updated successfully'
        ]);
    } catch (Exception $e) {
        echo json_encode([
          'success' => false,
          'error'   => $e->getMessage()
        ]);
    }
}


?>
