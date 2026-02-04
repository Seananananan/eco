<?php
header("Content-Type: application/json; charset=UTF-8");
require "config.php";

$response = ["success" => false, "message" => ""];

// debug (can remove later)
file_put_contents(__DIR__ . "/debug_post.txt",
    "REQUEST:\n" . print_r($_REQUEST, true) .
    "\nFILES:\n" . print_r($_FILES, true)
);

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    $response["message"] = "Invalid request method";
    echo json_encode($response);
    exit;
}

// use $_REQUEST so it works with multipart/form-data
$user_id = intval($_REQUEST["user_id"] ?? 0);
$title   = trim($_REQUEST["title"]   ?? "");
$note    = trim($_REQUEST["note"]    ?? "");
$address = trim($_REQUEST["address"] ?? "");

if ($user_id <= 0 || $title === "" || $note === "" || $address === "") {
    $response["message"] = "Please fill in all required fields.";
    echo json_encode($response);
    exit;
}

$imagePath = null;

if (isset($_FILES["image"]) && $_FILES["image"]["error"] === UPLOAD_ERR_OK) {
    $uploadDir = __DIR__ . "/uploads/";
    if (!is_dir($uploadDir)) {
        mkdir($uploadDir, 0777, true);
    }

    $ext     = pathinfo($_FILES["image"]["name"], PATHINFO_EXTENSION);
    $ext     = strtolower($ext);
    $allowed = ["jpg","jpeg","png"];

    if (!in_array($ext, $allowed)) {
        $response["message"] = "Invalid image type.";
        echo json_encode($response);
        exit;
    }

    $name     = "post_" . time() . "_" . mt_rand(1000,9999) . "." . $ext;
    $targetAbs = $uploadDir . $name;

    if (!move_uploaded_file($_FILES["image"]["tmp_name"], $targetAbs)) {
        $response["message"] = "Failed to upload image.";
        echo json_encode($response);
        exit;
    }

    // relative path to store in DB
    $imagePath = "uploads/" . $name;
}

try {
    $sql = "INSERT INTO posts (user_id, title, note, address, image_path, status)
            VALUES (:user_id, :title, :note, :address, :image_path, 'pending')";
    $stmt = $conn->prepare($sql);
    $stmt->bindParam(":user_id", $user_id, PDO::PARAM_INT);
    $stmt->bindParam(":title",   $title);
    $stmt->bindParam(":note",    $note);
    $stmt->bindParam(":address", $address);
    $stmt->bindParam(":image_path", $imagePath);

    if ($stmt->execute()) {
        $response["success"] = true;
        $response["message"] = "Post submitted successfully.";
        $response["post_id"] = $conn->lastInsertId();
    } else {
        $response["message"] = "Failed to submit post.";
    }
} catch (PDOException $e) {
    $response["message"] = "Database error.";
}

echo json_encode($response);
