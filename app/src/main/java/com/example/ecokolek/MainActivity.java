package com.example.ecokolek;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        MaterialButton btnPostWaste = findViewById(R.id.btnPostWaste);
        MaterialButton btnTracking = findViewById(R.id.btnTracking);
        MaterialButton btnGuidelines = findViewById(R.id.btnGuidelines);
        MaterialButton btnSchedulePickup = findViewById(R.id.btnSchedulePickup);
        MaterialButton btnCommunity = findViewById(R.id.btnCommunity);
        MaterialButton btnViewPosts = findViewById(R.id.btnViewPosts);

        btnViewPosts.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ViewActivity.class));
        });

        btnPostWaste.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PostActivity.class);
            startActivity(intent);
        });

        btnSchedulePickup.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
            startActivity(intent);
        });

        btnTracking.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, TrackingActivity.class);
            startActivity(intent);
        });
//
        btnGuidelines.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, GuidelinesLayout.class);
            startActivity(intent);
        });
//
        btnCommunity.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CommunityLayout.class);
            startActivity(intent);
        });
    }
}
