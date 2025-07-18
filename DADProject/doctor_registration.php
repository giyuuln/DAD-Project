<?php
// doctor_register.php - Registration endpoint
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

include 'config.php';  
$data = json_decode(file_get_contents('php://input'), true);

// make sure we got everything
$required = ['firstName','lastName','specialization','phone','email','password'];
foreach($required as $f) {
    if (empty($data[$f])) {
        echo json_encode(["error" => "Missing $f"]);
        exit;
    }
}

$firstName      = $data['firstName'];
$lastName       = $data['lastName'];
$specialization = $data['specialization'];
$phone          = $data['phone'];
$email          = $data['email'];
$password       = $data['password'];

// hash the password
$passwordHash = password_hash($password, PASSWORD_DEFAULT);


try {
   
    // Check if email already exists
    $checkEmail = $pdo->prepare("SELECT COUNT(*) FROM doctors WHERE email = ?");
    $checkEmail->execute([$email]);
    if ($checkEmail->fetchColumn() > 0) {
        echo json_encode([
            "success" => false,
            "error" => "Email already exists"
        ]);
        exit();
    }

    // Insert new doctor
   $stmt = $pdo->prepare(
      "INSERT INTO doctors
         (first_name, last_name, specialization, phone, email, password_hash)
       VALUES (?,?,?,?,?,?)"
    );
    $stmt->execute([
      $firstName, $lastName, $specialization,
      $phone,     $email,    $passwordHash
    ]);

    echo json_encode([
        "success" => true,
        "doctor_id" => $pdo->lastInsertId(),
        "message" => "Doctor registered successfully"
    ]);
} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "error" => "Registration error: " . $e->getMessage()
    ]);
}
