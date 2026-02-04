<?php
header("Content-Type: application/json; charset=UTF-8");
require "config.php";

$response = ["success" => false, "message" => ""];

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    $response["message"] = "Invalid request method";
    echo json_encode($response);
    exit;
}

$post_id = $_POST["post_id"] ?? "";

if ($post_id === "") {
    $response["message"] = "Missing post_id";
    echo json_encode($response);
    exit;
}

try {
    $stmt = $conn->prepare("DELETE FROM posts WHERE id = :id");
    $stmt->bindParam(":id", $post_id, PDO::PARAM_INT);

    if ($stmt->execute()) {
        if ($stmt->rowCount() > 0) {
            $response["success"] = true;
            $response["message"] = "Post deleted";
        } else {
            $response["message"] = "Post not found";
        }
    } else {
        $response["message"] = "Delete failed";
    }
} catch (PDOException $e) {
    $response["message"] = "Database error";
}

echo json_encode($response);
