<?php
header("Content-Type: application/json; charset=UTF-8");
require "config.php";

$response = ["success" => false, "message" => ""];

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    $response["message"] = "Invalid request method";
    echo json_encode($response);
    exit;
}

$user_id = intval($_POST["user_id"] ?? 0);
if ($user_id <= 0) {
    $response["message"] = "Missing or invalid user_id";
    echo json_encode($response);
    exit;
}

try {
    $stmt = $conn->prepare("
        SELECT id, title, note, address, image_path, created_at, status
        FROM posts
        WHERE user_id = :user_id
        ORDER BY id DESC
    ");
    $stmt->bindParam(":user_id", $user_id, PDO::PARAM_INT);
    $stmt->execute();

    $rows = [];
    $baseUrl = "http://192.168.1.21/android_api/"; // adjust if needed

    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $imagePath = $row["image_path"] ?? null;
        if ($imagePath) {
            $row["image_url"] = $baseUrl . $imagePath; // full URL
        } else {
            $row["image_url"] = null;
        }
        $rows[] = $row;
    }

    if (count($rows) === 0) {
        $response["success"] = true;
        $response["message"] = "No posts found";
        $response["posts"]   = [];
    } else {
        $response["success"] = true;
        $response["message"] = "Posts loaded";
        $response["posts"]   = $rows;
    }

} catch (PDOException $e) {
    $response["message"] = "Database error";
}

echo json_encode($response);
