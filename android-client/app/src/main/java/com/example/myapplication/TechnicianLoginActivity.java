package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class TechnicianLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_login);

        // Hide the default top action bar for a clean look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        TextInputEditText etMobile = findViewById(R.id.etMobile);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        ImageView btnBack = findViewById(R.id.btnBack);

        // Handle Back Button
        btnBack.setOnClickListener(v -> finish());

        // Handle Login Button
        // Handle Login Button (Bypass for UI testing)
        btnContinue.setOnClickListener(v -> {
            Toast.makeText(this, "Bypassing Login for UI Testing", Toast.LENGTH_SHORT).show();

            // Go directly to the Home screen as a Technician
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("USER_ROLE", "TECHNICIAN");
            startActivity(intent);
            finish();
        });
    }
}