
<?php
// doctor_auth.php - Authentication endpoint
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

include 'config.php';


$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['email']) || empty($data['password'])) {
    echo json_encode(["error" => "Missing email or password"]);
    exit;
}

$email    = $data['email'];
$password = $data['password'];

try {
    $stmt = $pdo->prepare("
      SELECT doctor_id, first_name, last_name, email, password_hash
        FROM doctors
       WHERE email = ?
    ");
    $stmt->execute([$email]);
    $doc = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($doc) {
        // Check if password_hash is set
        if (password_verify($password, $doc['password_hash'])) {
            echo json_encode([
                "success"   => true,
                "doctor_id" => $doc['doctor_id'],
                "first_name" => $doc['first_name'],
                "last_name"  => $doc['last_name'],
            ]);
        } else {
            // Invalid password
            echo json_encode([
                "success" => false,
                "error" => "Invalid credentials"
            ]);
        }
    } else {
        // Doctor not found
        echo json_encode([
            "success" => false,
            "error" => "Invalid credentials"
        ]);
    }
} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "error" => "Authentication error: " . $e->getMessage()
    ]);
}
?>
