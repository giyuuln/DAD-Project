<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE');
header('Access-Control-Allow-Headers: Content-Type');

include 'config.php';

$method = $_SERVER['REQUEST_METHOD'];
$request = isset($_SERVER['PATH_INFO']) ? explode('/', trim($_SERVER['PATH_INFO'], '/')) : [];

switch($method) {
    case 'GET':
        if (isset($request[0]) && is_numeric($request[0])) {
            getDoctor($pdo, $request[0]);
        } else {
            getAllDoctors($pdo);
        }
        break;
    case 'POST':
        createDoctor($pdo);
        break;
    case 'PUT':
        if (isset($request[0]) && is_numeric($request[0])) {
            updateDoctor($pdo, $request[0]);
        }
        break;
    case 'DELETE':
        if (isset($request[0]) && is_numeric($request[0])) {
            deleteDoctor($pdo, $request[0]);
        }
        break;
}

// FUNCTIONS

function getAllDoctors($pdo) {
    $stmt = $pdo->query("SELECT * FROM doctors ORDER BY last_name, first_name");
    $doctors = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode($doctors);
}

function getDoctor($pdo, $id) {
    $stmt = $pdo->prepare("SELECT * FROM doctors WHERE doctor_id = ?");
    $stmt->execute([$id]);
    $doctor = $stmt->fetch(PDO::FETCH_ASSOC);
    echo json_encode($doctor ? $doctor : ["error" => "Doctor not found"]);
}

function createDoctor($pdo) {
    $data = json_decode(file_get_contents("php://input"), true);

    $stmt = $pdo->prepare("INSERT INTO doctors (first_name, last_name, email, phone, specialization) VALUES (?, ?, ?, ?, ?)");

    try {
        $stmt->execute([
            $data['firstName'],
            $data['lastName'],
            $data['email'],
            $data['phone'],
            $data['specialization']
        ]);
        echo json_encode([
            "success" => true,
            "doctor_id" => $pdo->lastInsertId()
        ]);
    } catch (Exception $e) {
        echo json_encode(["error" => $e->getMessage()]);
    }
}

function updateDoctor($pdo, $id) {
    $data = json_decode(file_get_contents("php://input"), true);

    $stmt = $pdo->prepare("UPDATE doctors SET first_name = ?, last_name = ?, email = ?, phone = ?, specialization = ? WHERE doctor_id = ?");

    try {
        $stmt->execute([
            $data['firstName'],
            $data['lastName'],
            $data['email'],
            $data['phone'],
            $data['specialization'],
            $id
        ]);
        echo json_encode(["success" => true]);
    } catch (Exception $e) {
        echo json_encode(["error" => $e->getMessage()]);
    }
}

function deleteDoctor($pdo, $id) {
    $stmt = $pdo->prepare("DELETE FROM doctors WHERE doctor_id = ?");
    try {
        $stmt->execute([$id]);
        echo json_encode(["success" => true]);
    } catch (Exception $e) {
        echo json_encode(["error" => $e->getMessage()]);
    }
}
?>
