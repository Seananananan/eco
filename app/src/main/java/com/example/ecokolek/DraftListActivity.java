package com.example.ecokolek;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DraftListActivity extends AppCompatActivity {

    private static final String URL_GET_DRAFTS =
            "http://192.168.1.21/android_api/get_drafts.php";

    private LinearLayout containerDrafts;
    private static final int REQUEST_IMAGE_EDIT_DRAFT = 3001;

    private ImageView currentDraftEditImageView = null;
    private byte[] draftEditImageBytes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_drafts); // layout similar to activity_view.xml

        containerDrafts = findViewById(R.id.containerDrafts);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);
        if (userId == 0) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDrafts(userId);
    }

    private void loadDrafts(int userId) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_GET_DRAFTS,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("success")) {
                            Toast.makeText(this,
                                    obj.optString("message", "Failed to load drafts"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray arr = obj.getJSONArray("drafts");
                        containerDrafts.removeAllViews();
                        LayoutInflater inflater = LayoutInflater.from(this);

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject d = arr.getJSONObject(i);

                            int draftId      = d.getInt("id");
                            String title     = d.getString("title");
                            String note      = d.getString("note");
                            String weight    = d.optString("weight_kg", "");
                            String wasteType = d.optString("waste_type", "");
                            String imageUrl  = d.optString("image_url", null);
                            String updated   = d.optString("updated_at", "");

                            View card = inflater.inflate(R.layout.item_draft_post, containerDrafts, false);

                            ImageView imgThumb       = card.findViewById(R.id.imgThumbDraft);
                            TextView tvDraftTitle    = card.findViewById(R.id.tvDraftTitle);
                            TextView tvDraftMeta     = card.findViewById(R.id.tvDraftMeta);
                            TextView tvDraftUpdated  = card.findViewById(R.id.tvDraftUpdated);
                            MaterialButton btnView = card.findViewById(R.id.btnViewDraftDetails);

                            tvDraftTitle.setText(title);
                            tvDraftMeta.setText(weight + " kg · " + wasteType);
                            tvDraftUpdated.setText("Last edited: " + updated);

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                loadImageFromUrl(imageUrl, imgThumb);
                            }

                            String finalImageUrl = imageUrl;
                            btnView.setOnClickListener(v ->
                                    showDraftDialog(draftId, title, note, weight, wasteType, finalImageUrl, updated)
                            );
                            card.setOnClickListener(v ->
                                    showDraftDialog(draftId, title, note, weight, wasteType, finalImageUrl, updated)
                            );

                            containerDrafts.addView(card);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error loading drafts", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void loadImageFromUrl(String url, ImageView imageView) {
        new Thread(() -> {
            try {
                java.net.URL u = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                conn.setDoInput(true);
                conn.connect();
                java.io.InputStream is = conn.getInputStream();
                final Bitmap bmp = BitmapFactory.decodeStream(is);
                is.close();

                runOnUiThread(() -> imageView.setImageBitmap(bmp));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void showDraftDialog(int draftId,
                                 String title,
                                 String note,
                                 String weight,
                                 String wasteType,
                                 String imageUrl,
                                 String updated) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_draft_details);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        dialog.setCanceledOnTouchOutside(true);

        ImageView imgFull        = dialog.findViewById(R.id.imgDraftFull);
        TextView tvTitle         = dialog.findViewById(R.id.tvDraftTitleFull);
        TextView tvMeta          = dialog.findViewById(R.id.tvDraftMetaFull);
        TextView tvNote          = dialog.findViewById(R.id.tvDraftNoteFull);
        TextView tvWeight        = dialog.findViewById(R.id.tvDraftWeightFull);
        TextView tvWasteType     = dialog.findViewById(R.id.tvDraftWasteTypeFull);
        MaterialButton btnEdit   = dialog.findViewById(R.id.btnEditDraftDialog);
        MaterialButton btnDelete = dialog.findViewById(R.id.btnDeleteDraftDialog);
        MaterialButton btnPublish= dialog.findViewById(R.id.btnPublishDraftDialog);


        tvTitle.setText(title);
        tvMeta.setText("Last edited: " + updated);
        tvWeight.setText("Weight: " + weight + " kg");
        tvWasteType.setText("Type: " + wasteType);
        tvNote.setText(note);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadImageFromUrl(imageUrl, imgFull);
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadImageFromUrl(imageUrl, imgFull);
        }

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            showEditDraftDialog(draftId, title, note, weight, wasteType, imageUrl);
        });



        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete draft")
                    .setMessage("This action cannot be undone.\nDo you want to delete this draft?")
                    .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                    .setPositiveButton("Delete", (d, which) -> deleteDraft(draftId, dialog))
                    .show();
        });

        btnPublish.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Publish draft")
                    .setMessage("Are you sure you want to publish this draft?")
                    .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                    .setPositiveButton("Publish", (d, which) -> {
                        d.dismiss();
                        publishDraftFromList(draftId, dialog);
                    })
                    .show();
        });


        dialog.show();
    }
    private static final String URL_PUBLISH_DRAFT =
            "http://192.168.1.21/android_api/publish_draft.php";
    private void publishDraftFromList(int draftId, Dialog dialog) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);
        if (userId == 0) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest req = new StringRequest(
                Request.Method.POST,
                URL_PUBLISH_DRAFT,
                resp -> {
                    try {
                        JSONObject obj = new JSONObject(resp);
                        boolean success = obj.optBoolean("success", false);
                        String message  = obj.optString("message", "Publish failed");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            dialog.dismiss();
                            loadDrafts(userId); // refresh list, draft disappears
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Publish parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Publish error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("draft_id", String.valueOf(draftId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private static final String URL_DELETE_DRAFT =
            "http://192.168.1.21/android_api/delete_draft.php";
    private void deleteDraft(int draftId, Dialog dialog) {
        StringRequest req = new StringRequest(
                Request.Method.POST,
                URL_DELETE_DRAFT,
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
                                loadDrafts(userId); // refresh list
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
                params.put("draft_id", String.valueOf(draftId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }
    private void showEditDraftDialog(int draftId,
                                     String title,
                                     String note,
                                     String weight,
                                     String wasteType,
                                     String imageUrl) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_edit_draft);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        dialog.setCanceledOnTouchOutside(true);

        ImageView imgPreview   = dialog.findViewById(R.id.imgPreview);
        TextInputEditText etTitle  = dialog.findViewById(R.id.etTitle);
        TextInputEditText etNote   = dialog.findViewById(R.id.etNote);
        TextInputEditText etWeight = dialog.findViewById(R.id.etWeight);
        AutoCompleteTextView actWasteType = dialog.findViewById(R.id.actWasteType);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnDraft);
        MaterialButton btnApply  = dialog.findViewById(R.id.btnSubmit);
        TextView btnClose        = dialog.findViewById(R.id.btnCloseEdit);

        String[] wasteTypes = getResources().getStringArray(R.array.waste_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_waste_type,
                wasteTypes
        );
        actWasteType.setAdapter(adapter);

        etTitle.setText(title);
        etNote.setText(note);
        etWeight.setText(weight);
        actWasteType.setText(wasteType, false);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadImageFromUrl(imageUrl, imgPreview);
        }

        // prepare for image editing
        currentDraftEditImageView = imgPreview;
        draftEditImageBytes = null;

        imgPreview.setOnClickListener(v -> pickImageForDraftEdit());

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
            String newTitle  = Objects.toString(etTitle.getText(), "").trim();
            String newNote   = Objects.toString(etNote.getText(), "").trim();
            String newWeight = Objects.toString(etWeight.getText(), "").trim();
            String newType   = Objects.toString(actWasteType.getText(), "").trim();

            if (newTitle.isEmpty() || newNote.isEmpty()
                    || newWeight.isEmpty() || newType.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            updateDraftOnServer(draftId, newTitle, newNote, newWeight, newType);
            dialog.dismiss();
        });

        dialog.show();
    }
    private void pickImageForDraftEdit() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                REQUEST_IMAGE_EDIT_DRAFT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_EDIT_DRAFT
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            android.net.Uri uri = data.getData();
            try {
                java.io.InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                if (is != null) is.close();

                if (currentDraftEditImageView != null) {
                    currentDraftEditImageView.setImageBitmap(bmp);
                }

                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                draftEditImageBytes = baos.toByteArray();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static final String URL_UPDATE_DRAFT =
            "http://192.168.1.21/android_api/update_draft_post.php";

    private void updateDraftOnServer(int draftId,
                                     String title,
                                     String note,
                                     String weight,
                                     String wasteType) {

        Map<String, String> params = new HashMap<>();
        params.put("draft_id", String.valueOf(draftId));
        params.put("title", title);
        params.put("note", note);
        params.put("weight_kg", weight);
        params.put("waste_type", wasteType);

        Map<String, VolleyMultipartRequest.DataPart> byteData = new HashMap<>();
        if (draftEditImageBytes != null) {
            String fileName = "draft_" + draftId + "_" + System.currentTimeMillis() + ".jpg";
            byteData.put("image",
                    new VolleyMultipartRequest.DataPart(fileName, draftEditImageBytes, "image/jpeg"));
        }

        VolleyMultipartRequest req = new VolleyMultipartRequest(
                Request.Method.POST,
                URL_UPDATE_DRAFT,
                response -> {
                    try {
                        String respStr = new String(response.data);
                        JSONObject obj = new JSONObject(respStr);
                        boolean success = obj.optBoolean("success", false);
                        String message  = obj.optString("message", "Update failed");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            int userId = prefs.getInt("user_id", 0);
                            if (userId != 0) {
                                loadDrafts(userId); // refresh list
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


}
