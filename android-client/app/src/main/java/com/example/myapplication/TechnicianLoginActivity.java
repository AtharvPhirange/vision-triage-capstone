package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TechnicianLoginActivity extends AppCompatActivity {

    private TextInputLayout layoutMobile;
    private TextInputLayout layoutPassword;
    private TextInputEditText etMobile;
    private TextInputEditText etPassword;
    private ImageView btnBack;
    private MaterialButton btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_login);

        // Hide the default top action bar for a clean look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI Views
        layoutMobile = findViewById(R.id.layoutMobile);
        layoutPassword = findViewById(R.id.layoutPassword);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        btnBack = findViewById(R.id.btnBack);
        btnContinue = findViewById(R.id.btnContinue);

        // Handle Back Button Click
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Handle Continue / Login Button Click
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> validateAndProceed());
        }
    }

    private void validateAndProceed() {
        // Reset previous errors
        if (layoutMobile != null) {
            layoutMobile.setError(null);
        }
        if (layoutPassword != null) {
            layoutPassword.setError(null);
        }

        String mobileText = (etMobile != null && etMobile.getText() != null)
                ? etMobile.getText().toString().trim()
                : "";
        String passwordText = (etPassword != null && etPassword.getText() != null)
                ? etPassword.getText().toString().trim()
                : "";

        boolean isValid = true;

        if (mobileText.isEmpty()) {
            if (layoutMobile != null) {
                layoutMobile.setError("Mobile number is required");
            }
            isValid = false;
        } else if (mobileText.length() != 10) {
            if (layoutMobile != null) {
                layoutMobile.setError("Enter a valid 10-digit mobile number");
            }
            isValid = false;
        }

        if (passwordText.isEmpty()) {
            if (layoutPassword != null) {
                layoutPassword.setError("Technician PIN is required");
            }
            isValid = false;
        }

        if (isValid) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("USER_ROLE", "TECHNICIAN");
            startActivity(intent);
            finish();
        }
    }
}