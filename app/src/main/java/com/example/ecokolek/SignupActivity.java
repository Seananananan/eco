package com.example.ecokolek;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    TextInputEditText etFirstName, etLastName, etEmail,
            etBarangay, etCity, etPassword, etConfirmPassword;
    MaterialButton btnSignUp;


    private static final String URL_REGISTER =
            "http://192.168.1.12/android_api/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // your XML above

        etFirstName       = findViewById(R.id.etFirstName);
        etLastName        = findViewById(R.id.etLastName);
        etEmail           = findViewById(R.id.etEmail);
        etBarangay        = findViewById(R.id.etBarangay);
        etCity            = findViewById(R.id.etCity);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp         = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> attemptSignUp());
    }

    private String textOf(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void attemptSignUp() {
        String firstName = textOf(etFirstName);
        String lastName  = textOf(etLastName);
        String email     = textOf(etEmail);
        String barangay  = textOf(etBarangay);
        String city      = textOf(etCity);
        String password  = textOf(etPassword);
        String confirm   = textOf(etConfirmPassword);

        if (firstName.isEmpty() || lastName.isEmpty() ||
                email.isEmpty() || barangay.isEmpty() || city.isEmpty() ||
                password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.",
                    Toast.LENGTH_SHORT).show();
            return;
        }


        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        doRegister(firstName, lastName, email, barangay, city, password, confirm);
    }

    private void doRegister(String firstName, String lastName,
                            String email, String barangay,
                            String city, String password, String confirm) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_REGISTER,
                response -> {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response);
                        boolean success = obj.optBoolean("success", false);
                        String message  = obj.optString("message", "Unknown response");

                        Toast.makeText(SignupActivity.this,
                                message, Toast.LENGTH_LONG).show();

                        if (success) {

                            android.content.Intent i =
                                    new android.content.Intent(SignupActivity.this, Login.class);
                            startActivity(i);
                            finish();
                        }

                    } catch (Exception e) {

                        Toast.makeText(SignupActivity.this,
                                response, Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(SignupActivity.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("first_name", firstName);
                params.put("last_name", lastName);
                params.put("email", email);
                params.put("barangay", barangay);
                params.put("city", city);
                params.put("password", password);
                params.put("confirm_password", confirm);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

}
