package com.example.ecokolek;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Login extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;

    private static final String URL_LOGIN =
            "http://192.168.1.21/android_api/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Please enter email and password",
                        Toast.LENGTH_SHORT).show();
            } else {
                doLogin(email, password);
            }
        });
    }

    private void doLogin(String email, String password) {
        new Thread(() -> {
            try {
                URL url = new URL(URL_LOGIN);
                HttpURLConnection conn =
                        (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                );

                String postData = "email=" + URLEncoder.encode(email, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                String resp = response.toString();

                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(resp);
                        String status  = obj.getString("status");
                        String message = obj.getString("message");

                        Toast.makeText(Login.this,
                                message, Toast.LENGTH_LONG).show();

                        if ("success".equals(status)) {
                            int userId       = obj.optInt("user_id", 0);
                            String barangay  = obj.optString("baranggay", "");
                            String city      = obj.optString("city", "");

                            android.content.SharedPreferences prefs =
                                    getSharedPreferences("user_prefs", MODE_PRIVATE);
                            android.content.SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("user_id", userId);
                            editor.putString("user_barangay", barangay);
                            editor.putString("user_city", city);
                            editor.apply();

                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    } catch (Exception e) {
                        Toast.makeText(Login.this,
                                "Response error", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(Login.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
