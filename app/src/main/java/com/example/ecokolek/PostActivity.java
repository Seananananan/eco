package com.example.ecokolek;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.exifinterface.media.ExifInterface;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private static final int REQ_PICK_IMAGE = 1001;

    private CardView cardUpload;
    private ImageView imgPreview;
    private LinearLayout layoutUploadInfo;
    private TextView tvUploadTitle, tvUploadSub;
    private byte[] imageBytes = null;

    private TextInputEditText etTitle, etNote, etAddress;
    private MaterialButton btnDraft, btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);


        cardUpload       = findViewById(R.id.cardUpload);
        imgPreview       = findViewById(R.id.imgPreview);
        layoutUploadInfo = findViewById(R.id.layoutUploadInfo);
        tvUploadTitle    = findViewById(R.id.tvUploadTitle);
        tvUploadSub      = findViewById(R.id.tvUploadSub);

        etTitle   = findViewById(R.id.etTitle);
        etNote    = findViewById(R.id.etNote);
        etAddress = findViewById(R.id.etAddress);

        btnDraft  = findViewById(R.id.btnDraft);
        btnSubmit = findViewById(R.id.btnSubmit);

        cardUpload.setOnClickListener(v -> openImagePicker());

        btnDraft.setOnClickListener(v -> saveDraft());
        btnSubmit.setOnClickListener(v -> submitPost());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imgUri = data.getData();
            if (imgUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imgUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (inputStream != null) inputStream.close();

                    InputStream exifStream = getContentResolver().openInputStream(imgUri);
                    ExifInterface exif = new ExifInterface(exifStream);
                    if (exifStream != null) exifStream.close();

                    int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                    );

                    Bitmap rotated = rotateBitmap(bitmap, orientation);

                    imgPreview.setImageBitmap(rotated);
                    imgPreview.setVisibility(View.VISIBLE);
                    tvUploadTitle.setText("Image selected");
                    tvUploadSub.setText("Tap to change image");
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    rotated.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    imageBytes = baos.toByteArray();

                } catch (Exception e) {
                    e.printStackTrace();
                    imgPreview.setImageURI(imgUri);
                    imgPreview.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private Bitmap rotateBitmap(Bitmap source, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return source; // already correct
        }
        return Bitmap.createBitmap(
                source,
                0,
                0,
                source.getWidth(),
                source.getHeight(),
                matrix,
                true
        );
    }

    private void saveDraft() {
        String title   = textOf(etTitle);
        String note    = textOf(etNote);
        String address = textOf(etAddress);

    }

    private static final String URL_SUBMIT_POST =
            "http://192.168.1.21/android_api/submit_post.php";

    private void submitPost() {
        String title   = textOf(etTitle);
        String note    = textOf(etNote);
        String address = textOf(etAddress);

        if (title.isEmpty() || note.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }


        android.content.SharedPreferences prefs =
                getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);
        if (userId == 0) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> params = new java.util.HashMap<>();
        params.put("user_id", String.valueOf(userId));
        params.put("title", title);
        params.put("note", note);
        params.put("address", address);

        Map<String, VolleyMultipartRequest.DataPart> files = null;
        if (imageBytes != null) {
            files = new java.util.HashMap<>();
            files.put("image",
                    new VolleyMultipartRequest.DataPart(
                            "post_image.jpg",
                            imageBytes,
                            "image/jpeg"
                    ));
        }

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                com.android.volley.Request.Method.POST,
                URL_SUBMIT_POST,
                response -> {
                    String resStr = new String(response.data, java.nio.charset.StandardCharsets.UTF_8);
                    android.util.Log.d("POST_RESPONSE", resStr);
                    Toast.makeText(PostActivity.this, "Post submitted successfully", Toast.LENGTH_LONG).show();


                    etTitle.setText("");
                    etNote.setText("");
                    etAddress.setText("");

                    imageBytes = null;
                    imgPreview.setImageDrawable(null);
                    imgPreview.setVisibility(View.GONE);

                    tvUploadTitle.setText("Upload image");
                    tvUploadSub.setText("Tap to add a photo");
                    layoutUploadInfo.setVisibility(View.VISIBLE);
                },
                error -> {
                    String msg;
                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;
                        String body = new String(error.networkResponse.data);
                        msg = "Code " + code + ": " + body;
                    } else if (error.getMessage() != null) {
                        msg = error.getMessage();
                    } else {
                        msg = "Unknown error";
                    }
                    Toast.makeText(PostActivity.this, msg, Toast.LENGTH_LONG).show();
                },
                params,
                files
        );


        com.android.volley.RequestQueue queue =
                com.android.volley.toolbox.Volley.newRequestQueue(this);
        queue.add(request);
    }


    private String textOf(TextInputEditText et) {
        return et == null ? "" : Objects.toString(et.getText(), "").trim();
    }
}
