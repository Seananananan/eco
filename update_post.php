<?php
header("Content-Type: application/json; charset=UTF-8");
require "config.php";

$response = ["success" => false, "message" => ""];

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    $response["message"] = "Invalid request method";
    echo json_encode($response);
    exit;
}

$post_id = intval($_POST["post_id"] ?? 0);
$title   = trim($_POST["title"]   ?? "");
$note    = trim($_POST["note"]    ?? "");
$address = trim($_POST["address"] ?? "");

if ($post_id <= 0 || $title === "" || $note === "" || $address === "") {
    $response["message"] = "Missing required fields";
    echo json_encode($response);
    exit;
}

// handle optional new image
$imagePath = null;

if (!empty($_FILES["image"]["name"])) {
    $uploadDir = "uploads/"; // same base used by get_post.php (stored in image_path)

    if (!is_dir($uploadDir)) {
        mkdir($uploadDir, 0777, true);
    }

    $ext = pathinfo($_FILES["image"]["name"], PATHINFO_EXTENSION);
    if ($ext === "") {
        $ext = "jpg";
    }

    $fileName   = "post_" . $post_id . "_" . time() . "." . $ext;
    $targetPath = $uploadDir . $fileName;

    if (move_uploaded_file($_FILES["image"]["tmp_name"], $targetPath)) {
        $imagePath = $targetPath; // e.g. "uploads/post_1_123456789.jpg"
    } else {
        $response["message"] = "Failed to upload image";
        echo json_encode($response);
        exit;
    }
}

try {

    if ($imagePath) {
        $sql = "UPDATE posts
                SET title      = :title,
                    note       = :note,
                    address    = :address,
                    image_path = :image_path
                WHERE id = :id";
    } else {
        $sql = "UPDATE posts
                SET title   = :title,
                    note    = :note,
                    address = :address
                WHERE id = :id";
    }

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(":title",   $title);
    $stmt->bindParam(":note",    $note);
    $stmt->bindParam(":address", $address);
    if ($imagePath) {
        $stmt->bindParam(":image_path", $imagePath);
    }
    $stmt->bindParam(":id",      $post_id, PDO::PARAM_INT);

    if ($stmt->execute()) {
        if ($stmt->rowCount() > 0) {
            $response["success"] = true;
            $response["message"] = "Post updated successfully";
        } else {
            $response["message"] = "No changes or post not found";
        }
    } else {
        $response["message"] = "Failed to update post";
    }
} catch (PDOException $e) {
    $response["message"] = "Database error";
}

echo json_encode($response);
