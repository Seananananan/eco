package com.example.ecokolek;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ViewActivity extends AppCompatActivity {

    private static final String URL_GET_POSTS =
            "http://192.168.1.21/android_api/get_post.php";
    private static final String URL_DELETE_POST =
            "http://192.168.1.21/android_api/delete_post.php";
    private static final String URL_UPDATE_POST =
            "http://192.168.1.21/android_api/update_post.php";

    private static final int REQUEST_IMAGE_EDIT = 2001;

    private ImageView currentFullImageView = null;

    private LinearLayout containerPosts;

    private ImageView currentEditImageView = null;
    private byte[] editImageBytes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        containerPosts = findViewById(R.id.containerPosts);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);
        if (userId == 0) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPosts(userId);
    }

    private void loadPosts(int userId) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_GET_POSTS,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("success")) {
                            Toast.makeText(this,
                                    obj.optString("message", "Failed to load posts"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray arr = obj.getJSONArray("posts");
                        containerPosts.removeAllViews();
                        LayoutInflater inflater = LayoutInflater.from(this);

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject p = arr.getJSONObject(i);

                            int postId      = p.getInt("id");
                            String title    = p.getString("title");
                            String note     = p.getString("note");
                            String address  = p.getString("address");
                            String imageUrl = p.optString("image_url", null);
                            String created  = p.optString("created_at", "");
                            String status   = p.optString("status", "");

                            View card = inflater.inflate(R.layout.activity_details, containerPosts, false);

                            ImageView imgThumb      = card.findViewById(R.id.imgThumb);
                            TextView tvPostTitle    = card.findViewById(R.id.tvPostTitle);
                            TextView tvPostAddress  = card.findViewById(R.id.tvPostAddress);
                            TextView tvPostMeta     = card.findViewById(R.id.tvPostMeta);
                            MaterialButton btnView  = card.findViewById(R.id.btnViewDetails);

                            tvPostTitle.setText(title);
                            tvPostAddress.setText(address);
                            tvPostMeta.setText(created + " · " + status);

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                loadImageFromUrl(imageUrl, imgThumb);
                            }

                            String finalImageUrl = imageUrl;
                            btnView.setOnClickListener(v ->
                                    showPostDialog(postId, title, note, address, finalImageUrl, created, status)
                            );

                            containerPosts.addView(card);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error loading posts", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void loadImageFromUrl(String url, ImageView imageView) {
        new Thread(() -> {
            try {
                java.net.URL u = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                conn.setDoInput(true);
                conn.connect();
                java.io.InputStream is = conn.getInputStream();
                final Bitmap bmp =
                        BitmapFactory.decodeStream(is);
                is.close();

                runOnUiThread(() -> imageView.setImageBitmap(bmp));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showPostDialog(int postId,
                                String title,
                                String note,
                                String address,
                                String imageUrl,
                                String created,
                                String status) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_post_details);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        dialog.setCanceledOnTouchOutside(true);

        ImageView imgFull       = dialog.findViewById(R.id.imgFull);
        TextView tvTitle        = dialog.findViewById(R.id.tvTitleFull);
        TextView tvMeta         = dialog.findViewById(R.id.tvMetaFull);
        TextView tvAddress      = dialog.findViewById(R.id.tvAddressFull);
        TextView tvNote         = dialog.findViewById(R.id.tvNoteFull);
        MaterialButton btnEdit  = dialog.findViewById(R.id.btnEdit);
        MaterialButton btnDelete= dialog.findViewById(R.id.btnDelete);

        tvTitle.setText(title);
        tvMeta.setText(created + " · " + status);
        tvAddress.setText(address);
        tvNote.setText(note);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadImageFromUrl(imageUrl, imgFull);
        }


        btnDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Delete report")
                    .setMessage("This action cannot be undone.\nDo you want to delete this report?")
                    .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                    .setPositiveButton("Delete", (dialogInterface, which) -> {
                        deletePost(postId, dialog);
                    });

            AlertDialog alert = builder.create();
            alert.show();

            alert.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(android.graphics.Color.parseColor("#D32F2F"));
            alert.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(android.graphics.Color.parseColor("#87B86A"));
        });

        btnEdit.setOnClickListener(v ->
                showEditDialog(postId, title, note, address, imageUrl,
                         tvTitle, tvAddress, tvNote, imgFull)
        );


        dialog.show();
    }

    private void showEditDialog(int postId,
                                String title,
                                String note,
                                String address,
                                String imageUrl,
                                TextView tvTitleFull,
                                TextView tvAddressFull,
                                TextView tvNoteFull,
                                ImageView imgFull) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_editpost);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        dialog.setCanceledOnTouchOutside(true);

        ImageView imgPreviewEdit = dialog.findViewById(R.id.imgPreviewEdit);
        TextView btnCloseEdit    = dialog.findViewById(R.id.btnCloseEdit);
        TextInputEditText etTitleEdit   = dialog.findViewById(R.id.etTitleEdit);
        TextInputEditText etNoteEdit    = dialog.findViewById(R.id.etNoteEdit);
        TextInputEditText etAddressEdit = dialog.findViewById(R.id.etAddressEdit);
        MaterialButton btnCancelEdit    = dialog.findViewById(R.id.btnCancelEdit);
        MaterialButton btnApplyEdit     = dialog.findViewById(R.id.btnApplyEdit);

        etTitleEdit.setText(title);
        etNoteEdit.setText(note);
        etAddressEdit.setText(address);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadImageFromUrl(imageUrl, imgPreviewEdit);
        }


        currentEditImageView = imgPreviewEdit;
        currentFullImageView = imgFull;
        editImageBytes = null;

        imgPreviewEdit.setOnClickListener(v -> pickImageForEdit());

        btnCloseEdit.setOnClickListener(v -> dialog.dismiss());
        btnCancelEdit.setOnClickListener(v -> dialog.dismiss());

        btnApplyEdit.setOnClickListener(v -> {
            String newTitle   = etTitleEdit.getText() != null ? etTitleEdit.getText().toString().trim() : "";
            String newNote    = etNoteEdit.getText() != null ? etNoteEdit.getText().toString().trim() : "";
            String newAddress = etAddressEdit.getText() != null ? etAddressEdit.getText().toString().trim() : "";

            if (newTitle.isEmpty() || newNote.isEmpty() || newAddress.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePost(postId, newTitle, newNote, newAddress, dialog,
                    tvTitleFull, tvAddressFull, tvNoteFull);
        });

        dialog.show();
    }

    private void pickImageForEdit() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_EDIT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                if (is != null) is.close();

                if (currentEditImageView != null) {
                    currentEditImageView.setImageBitmap(bmp);
                }
                if (currentFullImageView != null) {      // <--- add this block
                    currentFullImageView.setImageBitmap(bmp);
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                editImageBytes = baos.toByteArray();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}

    private void updatePost(int postId,
                            String title,
                            String note,
                            String address,
                            Dialog editDialog,
                            TextView tvTitleFull,
                            TextView tvAddressFull,
                            TextView tvNoteFull) {

        Toast.makeText(this,
                (editImageBytes == null ? "No new image" : "Sending new image"),
                Toast.LENGTH_SHORT).show();

        Map<String, String> params = new HashMap<>();
        params.put("post_id", String.valueOf(postId));
        params.put("title", title);
        params.put("note", note);
        params.put("address", address);

        Map<String, VolleyMultipartRequest.DataPart> byteData = new HashMap<>();
        if (editImageBytes != null) {
            String fileName = "post_" + postId + "_" + System.currentTimeMillis() + ".jpg";
            byteData.put("image",
                    new VolleyMultipartRequest.DataPart(fileName, editImageBytes, "image/jpeg"));
        }

        VolleyMultipartRequest req = new VolleyMultipartRequest(
                Request.Method.POST,
                URL_UPDATE_POST,
                response -> {
                    try {
                        String respStr = new String(response.data);
                        JSONObject obj = new JSONObject(respStr);
                        boolean success = obj.optBoolean("success", false);
                        String message  = obj.optString("message", "Update failed");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            // update the open details dialog UI
                            tvTitleFull.setText(title);
                            tvAddressFull.setText(address);
                            tvNoteFull.setText(note);

                            editDialog.dismiss();

                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            int userId = prefs.getInt("user_id", 0);
                            if (userId != 0) {
                                loadPosts(userId);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Update parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Update error", Toast.LENGTH_SHORT).show(),
                params,
                byteData
        );

        Volley.newRequestQueue(this).add(req);
    }


    private void deletePost(int postId, Dialog dialog) {
        StringRequest req = new StringRequest(
                Request.Method.POST,
                URL_DELETE_POST,
                resp -> {
                    try {
                        JSONObject obj = new JSONObject(resp);
                        boolean success = obj.optBoolean("success", false);
                        String message  = obj.optString("message", "Delete failed");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            dialog.dismiss();
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            int userId = prefs.getInt("user_id", 0);
                            if (userId != 0) {
                                loadPosts(userId);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Delete parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Delete error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("post_id", String.valueOf(postId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }
}
