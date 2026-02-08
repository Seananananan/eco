package com.example.ecokolek;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private AlertDialog notificationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupButtonClickListeners();
        updateMonthlyStats(); // Update stats display
    }

    private void setupButtonClickListeners() {
        MaterialButton btnPostWaste = findViewById(R.id.btnPostWaste);
        MaterialButton btnTracking = findViewById(R.id.btnTracking);
        MaterialButton btnSchedulePickup = findViewById(R.id.btnSchedulePickup);
        MaterialButton btnCommunity = findViewById(R.id.btnCommunity);
        MaterialButton btnViewPosts = findViewById(R.id.btnViewPosts);
        MaterialButton btnNotifications = findViewById(R.id.btnNotifications);

        // Post Waste Button
        btnPostWaste.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PostActivity.class);
            startActivity(intent);
        });

        // Schedule Pickup Button
        btnSchedulePickup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
            startActivity(intent);
        });

        // Tracking Button
        btnTracking.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrackingActivity.class);
            startActivity(intent);
        });

        // View Posts Button
        btnViewPosts.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewActivity.class);
            startActivity(intent);
        });

        // Community Button
        btnCommunity.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CommunityLayout.class);
            startActivity(intent);
        });

        // Notifications Button
        btnNotifications.setOnClickListener(v -> showNotifications());
    }

    private void updateMonthlyStats() {
        // Cast to TextView before setText()
        TextView tvPlasticsValue = findViewById(R.id.tvPlasticsValue);
        TextView tvOrganicsValue = findViewById(R.id.tvOrganicsValue);
        TextView tvElectronicsValue = findViewById(R.id.tvElectronicsValue);
        TextView tvOthersValue = findViewById(R.id.tvOthersValue);
        TextView tvTotalLabel = findViewById(R.id.tvTotalLabel);

        tvPlasticsValue.setText("12kg");
        tvOrganicsValue.setText("8kg");
        tvElectronicsValue.setText("4kg");
        tvOthersValue.setText("2kg");

        int totalKg = 12 + 8 + 4 + 2;
        tvTotalLabel.setText("Total " + totalKg + "kg collected");
    }

    private void showNotifications() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔔 Notifications")
                .setMessage("✅ Pickup scheduled for tomorrow 9AM\n💰 50 Eco Points earned!\n👤 Eco Warrior Maria is nearby")
                .setPositiveButton("Mark All Read", (dialog, which) -> {
                    // Hide notification badge
                    View badge = findViewById(R.id.tvNotificationBadge);
                    badge.setVisibility(View.GONE);
                })
                .setNegativeButton("Close", null);

        notificationDialog = builder.create();
        notificationDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (notificationDialog != null && notificationDialog.isShowing()) {
            notificationDialog.dismiss();
        }
        super.onDestroy();
    }
}
