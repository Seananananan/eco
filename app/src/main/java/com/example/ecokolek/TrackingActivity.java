package com.example.ecokolek;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class TrackingActivity extends AppCompatActivity {

    private TextView tvSelectedPickup;
    private View rowSelectPickup;
    private ArrayList<String> postLabels = new ArrayList<>();
    private ArrayList<Integer> postIds = new ArrayList<>();

    private ImageView iconRequested, iconAssigned, iconOnTheWay, iconCollected;


    private TextView tvPlasticsKg, tvElectronicsKg, tvOrganicsKg, tvOthersKg, tvTotalKg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tracking);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.trackinglayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvPlasticsKg    = findViewById(R.id.tvPlasticsKg);
        tvElectronicsKg = findViewById(R.id.tvElectronicsKg);
        tvOrganicsKg    = findViewById(R.id.tvOrganicsKg);
        tvOthersKg      = findViewById(R.id.tvOthersKg);
        tvTotalKg       = findViewById(R.id.tvTotalKg);

        loadDashboardTotals();

        tvSelectedPickup = findViewById(R.id.tvSelectedPickup);
        rowSelectPickup = findViewById(R.id.rowSelectPickup);
        rowSelectPickup.setOnClickListener(v -> showPostsDialog());

        iconRequested = findViewById(R.id.iconRequested);
        iconAssigned = findViewById(R.id.iconAssigned);
        iconOnTheWay = findViewById(R.id.iconOnTheWay);
        iconCollected = findViewById(R.id.iconCollected);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        if (userId == 0) {
            tvSelectedPickup.setText("Please log in again");
            clearSteps();
            return;
        }

        loadUserPosts(userId);

    }

    private void loadUserPosts(int userId) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.168.1.21/android_api/get_user_post.php?user_id=" + userId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();

                String response = sb.toString();
                Log.d("TRACKING", "get_user_post response = " + response);

                JSONObject json = new JSONObject(response);
                boolean success = json.optBoolean("success", false);
                Log.d("TRACKING", "success = " + success);

                if (success) {
                    JSONArray postsArr = json.getJSONArray("posts");
                    postLabels.clear();
                    postIds.clear();

                    for (int i = 0; i < postsArr.length(); i++) {
                        JSONObject obj = postsArr.getJSONObject(i);
                        int id = obj.getInt("id");
                        String title = obj.getString("title");
                        String address = obj.getString("address");

                        String label = title + " • " + address;
                        postLabels.add(label);
                        postIds.add(id);
                    }

                    runOnUiThread(() -> {
                        if (!postLabels.isEmpty()) {
                            tvSelectedPickup.setText("Choose report");
                            clearSteps(); // nothing selected yet
                        } else {
                            tvSelectedPickup.setText("No posts yet");
                            clearSteps();
                        }
                    });

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }


    private void showPostsDialog() {
        if (postLabels.isEmpty()) return;

        String[] items = postLabels.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Select pickup")
                .setItems(items, (dialog, which) -> {
                    String selectedLabel = postLabels.get(which);
                    int selectedPostId = postIds.get(which);

                    tvSelectedPickup.setText(selectedLabel);
                    fetchPickupStatus(selectedPostId);
                })
                .show();
    }

    private void fetchPickupStatus(int postId) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.168.1.21/android_api/get_pickup_status.php?post_id=" + postId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                boolean success = json.optBoolean("success", false);

                if (success) {
                    String status = json.optString("status", "requested");
                    runOnUiThread(() -> updateSteps(status));
                } else {
                    runOnUiThread(this::clearSteps);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void clearSteps() {
        iconRequested.setBackgroundResource(R.drawable.bg_status_circle_idle);
        iconAssigned.setBackgroundResource(R.drawable.bg_status_circle_idle);
        iconOnTheWay.setBackgroundResource(R.drawable.bg_status_circle_idle);
        iconCollected.setBackgroundResource(R.drawable.bg_status_circle_idle);
    }

    private void updateSteps(String status) {
        clearSteps();

        switch (status) {
            case "requested":
                iconRequested.setBackgroundResource(R.drawable.bg_status_circle_active);
                break;

            case "assigned":
                iconRequested.setBackgroundResource(R.drawable.bg_status_circle_done);
                iconAssigned.setBackgroundResource(R.drawable.bg_status_circle_active);
                break;

            case "on_the_way":
                iconRequested.setBackgroundResource(R.drawable.bg_status_circle_done);
                iconAssigned.setBackgroundResource(R.drawable.bg_status_circle_done);
                iconOnTheWay.setBackgroundResource(R.drawable.bg_status_circle_active);
                break;

            case "collected":
                iconRequested.setBackgroundResource(R.drawable.bg_status_circle_done);
                iconAssigned.setBackgroundResource(R.drawable.bg_status_circle_done);
                iconOnTheWay.setBackgroundResource(R.drawable.bg_status_circle_done);
                iconCollected.setBackgroundResource(R.drawable.bg_status_circle_active);
                break;
        }
    }
    private static final String URL_DASHBOARD =
            "http://192.168.1.21/android_api/get_total.php";

    private void loadDashboardTotals() {
        StringRequest req = new StringRequest(
                Request.Method.GET,
                URL_DASHBOARD,
                response -> {android.util.Log.d("GET_TOTAL_JSON", response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.optBoolean("success", false)) {
                            return;
                        }
                        JSONObject totals = obj.getJSONObject("totals");

                        double plastics    = totals.optDouble("Plastics", 0);
                        double electronics = totals.optDouble("Electronics", 0);
                        double organics    = totals.optDouble("Organics", 0);
                        double others      = totals.optDouble("Others", 0);

                        tvPlasticsKg.setText(formatKg(plastics));
                        tvElectronicsKg.setText(formatKg(electronics));
                        tvOrganicsKg.setText(formatKg(organics));
                        tvOthersKg.setText(formatKg(others));

                        double total = plastics + electronics + organics + others;
                        tvTotalKg.setText("Total: " + formatKgNumber(total) + " collected");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {

                }

        );
        req.setShouldCache(false);

        com.android.volley.RequestQueue q =
                com.android.volley.toolbox.Volley.newRequestQueue(this);
        q.getCache().clear();
        q.add(req);
    }

    private String formatKg(double value) {
        return formatKgNumber(value) + " kg";
    }

    private String formatKgNumber(double value) {
        if (Math.abs(value - Math.round(value)) < 0.01) {
            return String.valueOf((int) Math.round(value));
        } else {
            return String.format(Locale.getDefault(), "%.1f", value);
        }
    }


}
