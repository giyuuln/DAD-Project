<?php

include 'config.php';
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE');
header('Access-Control-Allow-Headers: Content-Type');


$method = $_SERVER['REQUEST_METHOD'];
$request = isset($_SERVER['PATH_INFO']) ? explode('/', trim($_SERVER['PATH_INFO'], '/')) : [];

switch($method) {
    case 'GET':
        if(isset($request[0]) && is_numeric($request[0])) {
            getPatient($pdo, $request[0]);
        } else {
            getAllPatients($pdo);
        }
        break;
    case 'POST':
        createPatient($pdo);
        break;
    case 'PUT':
        if(isset($request[0]) && is_numeric($request[0])) {
            updatePatient($pdo, $request[0]);
        }
        break;
    case 'DELETE':
        if(isset($request[0]) && is_numeric($request[0])) {
            deletePatient($pdo, $request[0]);
        }
        break;
}

function getAllPatients($pdo) {
    $stmt = $pdo->query("SELECT * FROM patients ORDER BY last_name, first_name");
    $patients = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode($patients);
}

function getPatient($pdo, $id) {
    $stmt = $pdo->prepare("SELECT * FROM patients WHERE patient_id = ?");
    $stmt->execute([$id]);
    $patient = $stmt->fetch(PDO::FETCH_ASSOC);
    echo json_encode($patient ? $patient : ["error" => "Patient not found"]);
}

function createPatient($pdo) {
    $data = json_decode(file_get_contents("php://input"), true);

    if (!isset($data['firstName'], $data['lastName'])) {
        echo json_encode(["error" => "Missing required patient data"]);
        return;
    }

    $stmt = $pdo->prepare("INSERT INTO patients (first_name, last_name, phone, email, address, date_of_birth) VALUES (?, ?, ?, ?, ?, ?)");

    try {
        $stmt->execute([
            $data['firstName'],
            $data['lastName'],
            $data['phone'] ?? '',
            $data['email'] ?? '',
            $data['address'] ?? '',
            $data['dateOfBirth'] ?? null
        ]);

        echo json_encode([
            "success" => true,
            "patient_id" => $pdo->lastInsertId(),
            "message" => "Patient created successfully"
        ]);
    } catch (Exception $e) {
        echo json_encode(["error" => $e->getMessage()]);
    }
}


function updatePatient($pdo, $id) {
    $data = json_decode(file_get_contents("php://input"), true);

    if (!isset($data['firstName'], $data['lastName'])) {
        echo json_encode(["error" => "Missing required fields"]);
        return;
    }

    $stmt = $pdo->prepare("UPDATE patients 
        SET first_name = ?, last_name = ?, phone = ?, email = ?, address = ?, date_of_birth = ? 
        WHERE patient_id = ?");

    try {
        $stmt->execute([
            $data['firstName'],
            $data['lastName'],
            $data['phone'],
            $data['email'],
            $data['address'],
            $data['dateOfBirth'] ?? null,  // 👈 include this line
            $id
        ]);

        echo json_encode([
            "success" => true,
            "message" => "Patient updated successfully"
        ]);
    } catch(Exception $e) {
        echo json_encode(["error" => $e->getMessage()]);
    }
}



function deletePatient($pdo, $id) {
    $stmt = $pdo->prepare("DELETE FROM patients WHERE patient_id = ?");
    
    try {
        $stmt->execute([$id]);
        echo json_encode([
            "success" => true,
            "message" => "Patient deleted successfully"
        ]);
    } catch(Exception $e) {
        echo json_encode(["error" => $e->getMessage()]);
    }
}
?>
