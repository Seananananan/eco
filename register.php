<?php
header("Content-Type: application/json; charset=UTF-8");
require "config.php";

$response = ["success" => false, "message" => ""];

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    $response["message"] = "Invalid request method";
    echo json_encode($response);
    exit;
}

$first_name = trim($_POST["first_name"] ?? "");
$last_name  = trim($_POST["last_name"] ?? "");
$email      = trim($_POST["email"] ?? "");
$barangay   = trim($_POST["barangay"] ?? "");   
$city       = trim($_POST["city"] ?? "");
$password   = $_POST["password"] ?? "";
$confirmPwd = $_POST["confirm_password"] ?? "";

if ($first_name === "" || $last_name === "" ||
    $email === "" || $password === "" || $confirmPwd === "") {
    $response["message"] = "Please fill in all required fields.";
    echo json_encode($response);
    exit;
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    $response["message"] = "Invalid email format.";
    echo json_encode($response);
    exit;
}

if ($password !== $confirmPwd) {
    $response["message"] = "Passwords do not match.";
    echo json_encode($response);
    exit;
}

try {
    $checkSql = "SELECT id FROM users WHERE email = :email LIMIT 1";
    $check = $conn->prepare($checkSql);
    $check->bindParam(":email", $email);
    $check->execute();

    if ($check->rowCount() > 0) {
        $response["message"] = "Email already exists.";
        echo json_encode($response);
        exit;
    }

    $hash = password_hash($password, PASSWORD_DEFAULT);

 
    $sql = "INSERT INTO users (fname, lname, email, baranggay, city, password)
            VALUES (:fname, :lname, :email, :baranggay, :city, :password)";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(":fname", $first_name);
    $stmt->bindParam(":lname", $last_name);
    $stmt->bindParam(":email", $email);
    $stmt->bindParam(":baranggay", $barangay);  
    $stmt->bindParam(":city", $city);
    $stmt->bindParam(":password", $hash);

    if ($stmt->execute()) {
        $response["success"] = true;
        $response["message"] = "Account created successfully.";
        $response["user_id"] = $conn->lastInsertId();
    } else {
        $response["message"] = "Failed to create account.";
    }

} catch (PDOException $e) {
    $response["message"] = "Database error.";
}

echo json_encode($response);
