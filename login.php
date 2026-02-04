<?php
header("Content-Type: application/json");
require "config.php";

if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    $email    = $_POST['email']    ?? '';
    $password = $_POST['password'] ?? '';

    if ($email === '' || $password === '') {
        echo json_encode(["status" => "error", "message" => "Missing parameters"]);
        exit;
    }

    try {
        $sql = "SELECT id, password FROM users WHERE email = :email LIMIT 1";
        $stmt = $conn->prepare($sql);
        $stmt->bindParam(':email', $email);
        $stmt->execute();

        if ($stmt->rowCount() > 0) {
            $user = $stmt->fetch(PDO::FETCH_ASSOC);

           
            if (password_verify($password, $user['password'])) {
                echo json_encode([
                    "status"  => "success",
                    "message" => "Login successful",
                    "user_id" => $user["id"]
                ]);
            } else {
                echo json_encode([
                    "status"  => "error",
                    "message" => "Invalid password"
                ]);
            }

        } else {
            echo json_encode([
                "status"  => "error",
                "message" => "User not found"
            ]);
        }

    } catch (PDOException $e) {
        echo json_encode([
            "status"  => "error",
            "message" => "Login failed"
        ]);
    }
    exit;
}
