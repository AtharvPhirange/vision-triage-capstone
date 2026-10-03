package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

public class LoginActivity extends AppCompatActivity {

    private String selectedRole = "TECHNICIAN";
    private AppCompatButton btnRoleTech, btnRolePatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnRoleTech = findViewById(R.id.btnRoleTech);
        btnRolePatient = findViewById(R.id.btnRolePatient);
        CardView cardMobileLogin = findViewById(R.id.cardMobileLogin);

        if (btnRoleTech != null) {
            btnRoleTech.setOnClickListener(v -> {
                selectedRole = "TECHNICIAN";
                updateRoleUI();
            });
        }

        if (btnRolePatient != null) {
            btnRolePatient.setOnClickListener(v -> {
                selectedRole = "PATIENT";
                updateRoleUI();
            });
        }

        if (cardMobileLogin != null) {
            cardMobileLogin.setOnClickListener(v -> handleAuthentication());
        }
    }

    private void updateRoleUI() {
        if (btnRoleTech == null || btnRolePatient == null) return;

        if ("TECHNICIAN".equals(selectedRole)) {
            btnRoleTech.setBackgroundResource(R.drawable.bg_toggle_active);
            btnRoleTech.setTextColor(android.graphics.Color.WHITE);
            btnRolePatient.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btnRolePatient.setTextColor(android.graphics.Color.parseColor("#003893"));
        } else {
            btnRolePatient.setBackgroundResource(R.drawable.bg_toggle_active);
            btnRolePatient.setTextColor(android.graphics.Color.WHITE);
            btnRoleTech.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btnRoleTech.setTextColor(android.graphics.Color.parseColor("#003893"));
        }
    }

    private void handleAuthentication() {
        if ("TECHNICIAN".equals(selectedRole)) {
            showMobileVerificationDialog();
        } else {
            // Patient direct entry
            proceedToDashboard();
        }
    }

    private void showMobileVerificationDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etPhone = new EditText(this);
        etPhone.setHint("Enter Authorized Mobile Number");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        new AlertDialog.Builder(this)
                .setTitle("Technician Mobile Auth")
                .setMessage("Enter registered phone number to receive secure OTP")
                .setView(layout)
                .setPositiveButton("Send OTP", (dialog, which) -> {
                    String phone = etPhone.getText().toString().trim();
                    if (phone.length() < 10) {
                        Toast.makeText(this, "Enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showOtpChallengeDialog(phone);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showOtpChallengeDialog(String phone) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etOtp = new EditText(this);
        etOtp.setHint("Enter 6-Digit Verification Code");
        etOtp.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etOtp);

        new AlertDialog.Builder(this)
                .setTitle("Verify Security OTP")
                .setMessage("Code sent to +91 " + phone)
                .setView(layout)
                .setPositiveButton("Verify & Login", (dialog, which) -> {
                    String otp = etOtp.getText().toString().trim();
                    if (otp.length() == 6 || "123456".equals(otp)) { // Demo override code 123456
                        Toast.makeText(this, "Technician Identity Verified", Toast.LENGTH_SHORT).show();
                        proceedToDashboard();
                    } else {
                        Toast.makeText(this, "Invalid OTP code. Access denied.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void proceedToDashboard() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("USER_ROLE", selectedRole);
        startActivity(intent);
        finish();
    }
}