package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private String selectedRole = "TECHNICIAN";
    private AppCompatButton btnRoleTech, btnRolePatient;

    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();

        // Session Management: Skip login if the user is already authenticated
        if (mAuth.getCurrentUser() != null) {
            proceedToDashboard();
            return;
        }

        btnRoleTech = findViewById(R.id.btnRoleTech);
        btnRolePatient = findViewById(R.id.btnRolePatient);
        CardView cardMobileLogin = findViewById(R.id.cardMobileLogin);

        updateRoleUI();

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
            btnRoleTech.setBackgroundColor(android.graphics.Color.parseColor("#003893"));
            btnRoleTech.setTextColor(android.graphics.Color.WHITE);
            btnRolePatient.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btnRolePatient.setTextColor(android.graphics.Color.parseColor("#003893"));
        } else {
            btnRolePatient.setBackgroundColor(android.graphics.Color.parseColor("#003893"));
            btnRolePatient.setTextColor(android.graphics.Color.WHITE);
            btnRoleTech.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btnRoleTech.setTextColor(android.graphics.Color.parseColor("#003893"));
        }
    }

    private void handleAuthentication() {
        if ("TECHNICIAN".equals(selectedRole)) {
            showMobileVerificationDialog();
        } else {
            Toast.makeText(this, "Logging in as Patient", Toast.LENGTH_SHORT).show();
            proceedToDashboard();
        }
    }

    private void showMobileVerificationDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText etPhone = new EditText(this);
        etPhone.setHint("e.g. 9876543210");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        new AlertDialog.Builder(this)
                .setTitle("Technician Mobile Auth")
                .setMessage("Enter registered 10-digit phone number:")
                .setView(layout)
                .setPositiveButton("Send OTP", (dialog, which) -> {
                    String phone = etPhone.getText().toString().trim();
                    if (phone.length() != 10) {
                        Toast.makeText(this, "Please enter a valid 10-digit number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendRealSmsOtp("+91" + phone);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendRealSmsOtp(String fullPhoneNumber) {
        Toast.makeText(this, "Requesting network SMS...", Toast.LENGTH_SHORT).show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(fullPhoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(LoginActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                mVerificationId = verificationId;
                                mResendToken = token;
                                showOtpChallengeDialog(fullPhoneNumber);
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void showOtpChallengeDialog(String phone) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText etOtp = new EditText(this);
        etOtp.setHint("Enter 6-digit Network OTP");
        etOtp.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etOtp);

        new AlertDialog.Builder(this)
                .setTitle("Verify Security OTP")
                .setMessage("Code sent to " + phone)
                .setView(layout)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String otp = etOtp.getText().toString().replaceAll("\\s+", "").trim();
                    if (otp.length() == 6) {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                        signInWithPhoneAuthCredential(credential);
                    } else {
                        Toast.makeText(this, "OTP must be exactly 6 digits.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Identity Verified", Toast.LENGTH_SHORT).show();
                        proceedToDashboard();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed. Incorrect OTP.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void proceedToDashboard() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("USER_ROLE", selectedRole);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}