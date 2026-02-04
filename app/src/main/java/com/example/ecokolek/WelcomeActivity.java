package com.example.ecokolek;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    private MaterialButton btnLogin, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose); // name of your XML

        btnLogin  = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, Login.class)));

        btnSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }
}
