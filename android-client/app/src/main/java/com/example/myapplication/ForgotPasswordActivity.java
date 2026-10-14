package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private LinearLayout layoutEmailStep, layoutOtpStep;
    private TextInputEditText etResetEmail, etOtpCode;
    private Button btnSubmit;
    private TextView tvTitle, tvSubtitle;

    private String generatedOtp = "";
    private boolean isOtpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        layoutEmailStep = findViewById(R.id.layoutEmailStep);
        layoutOtpStep = findViewById(R.id.layoutOtpStep);
        etResetEmail = findViewById(R.id.etResetEmail);
        etOtpCode = findViewById(R.id.etOtpCode);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            if (!isOtpSent) {
                processEmailSubmission();
            } else {
                processOtpVerification();
            }
        });
    }

    private void processEmailSubmission() {
        String email = etResetEmail.getText() != null ? etResetEmail.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Sending Email...");

        // Generate a secure 6-digit OTP
        generatedOtp = String.format("%06d", new Random().nextInt(999999));

        // Execute network request on a background thread
        new Thread(() -> {
            try {
                EmailSenderUtil.sendOtpEmail(email, generatedOtp);

                // Return to Main Thread to update UI
                runOnUiThread(() -> {
                    Toast.makeText(this, "OTP Sent successfully to " + email, Toast.LENGTH_LONG).show();

                    isOtpSent = true;
                    layoutEmailStep.setVisibility(View.GONE);
                    layoutOtpStep.setVisibility(View.VISIBLE);

                    tvTitle.setText("Verify OTP");
                    tvSubtitle.setText("Enter the 6-digit code sent to " + email);
                    btnSubmit.setText("Verify Code");
                    btnSubmit.setEnabled(true);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to send email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubmit.setText("Send OTP");
                    btnSubmit.setEnabled(true);
                });
            }
        }).start();
    }

    private void processOtpVerification() {
        String enteredOtp = etOtpCode.getText() != null ? etOtpCode.getText().toString().trim() : "";

        if (enteredOtp.equals(generatedOtp)) {
            Toast.makeText(this, "Verification Successful! Redirecting...", Toast.LENGTH_LONG).show();

            // Auto Sign-in & Home Dashboard Navigation
            Intent intent = new Intent(ForgotPasswordActivity.this, HomeActivity.class);
            // Clear the backstack so the user cannot navigate back to the OTP screen
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid OTP Code. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}