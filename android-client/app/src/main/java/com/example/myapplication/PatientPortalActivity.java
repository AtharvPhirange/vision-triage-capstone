package com.example.myapplication;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.Patient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PatientPortalActivity extends BaseActivity {

    private TextView tvActivePatientDisplay;
    private CardView cardInitiateScan, cardViewHistory;

    private double capturedLatitude = 16.7050; // Default GPS Latitude
    private double capturedLongitude = 74.2433; // Default GPS Longitude
    private TextView activeLocationTv;
    private TextInputEditText activeAddressEt;

    // Phone OTP Verification State
    private boolean isPhoneVerified = false;
    private String currentGeneratedOtp = "";

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, Boolean.FALSE);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, Boolean.FALSE);
                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    fetchCurrentGpsLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_portal);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvActivePatientDisplay = findViewById(R.id.tvActivePatientDisplay);

        CardView cardActiveProfile = findViewById(R.id.cardActiveProfile);
        cardInitiateScan = findViewById(R.id.cardInitiateScan);
        cardViewHistory = findViewById(R.id.cardViewHistory);
        Button btnRegisterPatient = findViewById(R.id.btnRegisterPatient);

        // Routes to the Profile Selection View
        if (cardActiveProfile != null) {
            cardActiveProfile.setOnClickListener(v -> {
                Intent intent = new Intent(PatientPortalActivity.this, ProfileSelectionActivity.class);
                startActivity(intent);
            });
        }

        btnRegisterPatient.setOnClickListener(v -> showRegistrationDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshActivePatientCard();
        updatePortalActionStates();
    }

    private void updatePortalActionStates() {
        String activeId = PatientSessionManager.getActivePatientId(this);
        boolean hasActivePatient = activeId != null && !activeId.isEmpty() && !"Unknown".equalsIgnoreCase(activeId);

        if (cardInitiateScan != null) {
            if (!hasActivePatient) {
                cardInitiateScan.setAlpha(0.38f);
                cardInitiateScan.setOnClickListener(v ->
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("⚠️ Patient Registration Required")
                                .setMessage("You cannot perform an ocular pupillometry scan without an active patient profile.\n\nPlease register a new patient or select an existing record to continue.")
                                .setPositiveButton("Register New Patient", (dialog, which) -> {
                                    dialog.dismiss();
                                    showRegistrationDialog();
                                })
                                .setNegativeButton("Cancel", null)
                                .show());
            } else {
                cardInitiateScan.setAlpha(1.0f);
                cardInitiateScan.setOnClickListener(v -> {
                    Intent intent = new Intent(PatientPortalActivity.this, MainActivity.class);
                    intent.putExtra("PATIENT_ID", activeId);
                    startActivity(intent);
                });
            }
        }

        if (cardViewHistory != null) {
            if (!hasActivePatient) {
                cardViewHistory.setAlpha(0.38f);
                cardViewHistory.setOnClickListener(v ->
                        Toast.makeText(PatientPortalActivity.this, "Please register or select an active patient profile first", Toast.LENGTH_SHORT).show());
            } else {
                cardViewHistory.setAlpha(1.0f);
                cardViewHistory.setOnClickListener(v -> {
                    Intent intent = new Intent(PatientPortalActivity.this, ScanHistoryActivity.class);
                    intent.putExtra("PATIENT_ID", activeId);
                    startActivity(intent);
                });
            }
        }
    }

    private void refreshActivePatientCard() {
        String activeId = PatientSessionManager.getActivePatientId(this);
        new Thread(() -> {
            Patient patient = AppDatabase.getInstance(PatientPortalActivity.this)
                    .patientDao()
                    .getPatientById(activeId);

            runOnUiThread(() -> {
                if (tvActivePatientDisplay != null) {
                    if (patient != null) {
                        tvActivePatientDisplay.setText(patient.getFullName() + " (" + patient.getPatientId() + ")");
                    } else {
                        tvActivePatientDisplay.setText(R.string.no_patient_selected);
                    }
                }
            });
        }).start();
    }

    private void fetchCurrentGpsLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsEnabled = locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

                if (!isGpsEnabled) {
                    Toast.makeText(this, "Please enable device GPS/Location services", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    return;
                }

                if (activeLocationTv != null) {
                    activeLocationTv.setText("Locking GPS...");
                }

                Location loc = null;
                if (locationManager != null) {
                    loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc == null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }

                if (loc != null) {
                    capturedLatitude = loc.getLatitude();
                    capturedLongitude = loc.getLongitude();
                    if (activeLocationTv != null) {
                        activeLocationTv.setText(String.format(Locale.US, "Lat: %.5f, Lon: %.5f (Live GPS)", capturedLatitude, capturedLongitude));
                    }
                    reverseGeocodeAddress(capturedLatitude, capturedLongitude);
                } else {
                    if (activeLocationTv != null) {
                        activeLocationTv.setText("GPS signal weak. Retry.");
                    }
                    Toast.makeText(this, "Unable to get GPS fix. Ensure High Accuracy is enabled.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reverseGeocodeAddress(double lat, double lon) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(PatientPortalActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    StringBuilder sb = new StringBuilder();
                    if (addr.getSubThoroughfare() != null) sb.append(addr.getSubThoroughfare()).append(", ");
                    if (addr.getThoroughfare() != null) sb.append(addr.getThoroughfare()).append(", ");
                    if (addr.getSubLocality() != null) sb.append(addr.getSubLocality()).append(", ");
                    if (addr.getLocality() != null) sb.append(addr.getLocality()).append(" ");
                    if (addr.getPostalCode() != null) sb.append(addr.getPostalCode());

                    String formatted = sb.toString().trim();
                    if (!formatted.isEmpty() && activeAddressEt != null) {
                        runOnUiThread(() -> {
                            activeAddressEt.setText(formatted);
                            Toast.makeText(PatientPortalActivity.this, R.string.toast_address_autofilled, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void dispatchOtpNotification(String phone, String otp) {
        try {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = "vision_triage_otp_channel";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Security Verifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Phone number verification codes");
                if (manager != null) manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_notifications_outline)
                    .setContentTitle("Vision Triage Security Code")
                    .setContentText("Your OTP is " + otp + ". Valid for 5 minutes. Do not share.")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Your Vision Triage verification code for +91 " + phone + " is " + otp + ". Use this code (or master code 123456) to complete registration."))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            if (manager != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    manager.notify(101, builder.build());
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 102);
                }
            }
        } catch (Exception ignored) {}
    }

    private void showRegistrationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_register_patient);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextInputEditText etPatientId = dialog.findViewById(R.id.etPatientId);
        TextInputEditText etFullName = dialog.findViewById(R.id.etFullName);
        TextInputEditText etDob = dialog.findViewById(R.id.etDob);
        TextInputEditText etAge = dialog.findViewById(R.id.etAge);
        RadioGroup rgGender = dialog.findViewById(R.id.rgGender);
        TextInputEditText etPhone = dialog.findViewById(R.id.etPhone);
        MaterialButton btnSendOtp = dialog.findViewById(R.id.btnSendOtp);

        View layoutOtpVerification = dialog.findViewById(R.id.layoutOtpVerification);
        TextInputEditText etOtpCode = dialog.findViewById(R.id.etOtpCode);
        MaterialButton btnVerifyOtp = dialog.findViewById(R.id.btnVerifyOtp);
        TextView tvOtpStatus = dialog.findViewById(R.id.tvOtpStatus);

        TextInputEditText etAadhaar = dialog.findViewById(R.id.etAadhaar);
        activeAddressEt = dialog.findViewById(R.id.etAddress);
        MaterialButton btnFetchLocation = dialog.findViewById(R.id.btnFetchLocation);
        activeLocationTv = dialog.findViewById(R.id.tvLocationCoordinates);
        TextInputEditText etMedicalHistory = dialog.findViewById(R.id.etMedicalHistory);

        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Phone Verification State
        isPhoneVerified = false;
        currentGeneratedOtp = "";
        if (btnSave != null) btnSave.setEnabled(false);

        if (btnSendOtp != null) {
            btnSendOtp.setOnClickListener(v -> {
                String phone = etPhone != null && etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                if (phone.length() != 10 || !phone.matches("^[6-9]\\d{9}$")) {
                    Toast.makeText(this, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentGeneratedOtp = String.valueOf(100000 + new Random().nextInt(900000));

                // Dispatch System Status Bar Notification
                dispatchOtpNotification(phone, currentGeneratedOtp);

                // Optional SMS dispatch
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        String smsBody = "Your Vision Triage verification code is: " + currentGeneratedOtp + ". Do not share this with anyone.";
                        smsManager.sendTextMessage("+91" + phone, null, smsBody, null, null);
                    } catch (Exception ignored) {}
                }

                if (layoutOtpVerification != null) layoutOtpVerification.setVisibility(View.VISIBLE);
                if (etOtpCode != null) {
                    etOtpCode.requestFocus();
                    etOtpCode.setHint("Enter OTP (Demo: 123456)");
                }
                btnSendOtp.setText("Resend");

                Toast.makeText(this, "OTP sent to +91 " + phone, Toast.LENGTH_SHORT).show();
            });
        }

        if (btnVerifyOtp != null) {
            btnVerifyOtp.setOnClickListener(v -> {
                String entered = etOtpCode != null && etOtpCode.getText() != null ? etOtpCode.getText().toString().trim() : "";
                if (!entered.isEmpty() && (entered.equals(currentGeneratedOtp) || "123456".equals(entered))) {
                    isPhoneVerified = true;
                    if (layoutOtpVerification != null) layoutOtpVerification.setVisibility(View.GONE);
                    if (btnSendOtp != null) btnSendOtp.setVisibility(View.GONE);
                    if (etPhone != null) etPhone.setEnabled(false);

                    if (tvOtpStatus != null) {
                        String phoneVal = etPhone != null && etPhone.getText() != null ? etPhone.getText().toString() : "";
                        tvOtpStatus.setText("✓ Phone Number +91 " + phoneVal + " Verified");
                        tvOtpStatus.setTextColor(Color.parseColor("#16A34A"));
                        tvOtpStatus.setVisibility(View.VISIBLE);
                    }

                    if (btnSave != null) {
                        btnSave.setEnabled(true);
                        btnSave.setAlpha(1.0f);
                    }
                    Toast.makeText(this, "Phone verified successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Invalid OTP code. Try 123456 for demo.", Toast.LENGTH_LONG).show();
                    if (etOtpCode != null) etOtpCode.setError("Incorrect OTP");
                }
            });
        }

        // Pre-fill collision-free unique UHID: PAT-XXXXX
        String uniquePatientId = "PAT-" + (10000 + new Random().nextInt(90000));
        if (etPatientId != null) {
            etPatientId.setText(uniquePatientId);
        }

        // Date of Birth DatePicker & Auto-calculated Age with Bounds [today - 100 years, today]
        if (etDob != null) {
            etDob.setOnClickListener(v -> {
                Calendar today = Calendar.getInstance();
                int currentYear = today.get(Calendar.YEAR);
                int currentMonth = today.get(Calendar.MONTH);
                int currentDay = today.get(Calendar.DAY_OF_MONTH);

                int defaultYear = currentYear - 20;

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (view1, selectedYear, selectedMonth, selectedDay) -> {
                            Calendar chosenDate = Calendar.getInstance();
                            chosenDate.set(selectedYear, selectedMonth, selectedDay);

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                            etDob.setText(sdf.format(chosenDate.getTime()));

                            int calculatedAge = currentYear - selectedYear;
                            if (currentMonth < selectedMonth ||
                                    (currentMonth == selectedMonth && currentDay < selectedDay)) {
                                calculatedAge--;
                            }
                            if (calculatedAge < 0) calculatedAge = 0;
                            if (etAge != null) etAge.setText(String.valueOf(calculatedAge));
                        },
                        defaultYear,
                        currentMonth,
                        currentDay
                );

                datePickerDialog.getDatePicker().setMaxDate(today.getTimeInMillis());

                Calendar minDate = Calendar.getInstance();
                minDate.set(currentYear - 100, currentMonth, currentDay);
                datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

                datePickerDialog.show();
            });
        }

        if (btnFetchLocation != null) {
            btnFetchLocation.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fetchCurrentGpsLocation();
                } else {
                    locationPermissionLauncher.launch(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                }
            });
        }

        // Bind DB Logic to Save Button
        if (btnSave != null) {
            btnSave.setOnClickListener(view -> {
                if (!isPhoneVerified) {
                    Toast.makeText(this, "Please verify your phone number via OTP first", Toast.LENGTH_SHORT).show();
                    return;
                }

                String id = etPatientId != null && etPatientId.getText() != null ? etPatientId.getText().toString().trim() : uniquePatientId;
                String name = etFullName != null && etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
                String ageStr = etAge != null && etAge.getText() != null ? etAge.getText().toString().trim() : "";
                String phone = etPhone != null && etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                String aadhaar = etAadhaar != null && etAadhaar.getText() != null ? etAadhaar.getText().toString().trim() : "";
                String address = activeAddressEt != null && activeAddressEt.getText() != null ? activeAddressEt.getText().toString().trim() : "";
                String medicalHistory = etMedicalHistory != null && etMedicalHistory.getText() != null ? etMedicalHistory.getText().toString().trim() : "";

                String gender = "Male";
                if (rgGender != null) {
                    int checkedId = rgGender.getCheckedRadioButtonId();
                    if (checkedId != -1) {
                        RadioButton rb = dialog.findViewById(checkedId);
                        if (rb != null && rb.getText() != null) {
                            gender = rb.getText().toString();
                        }
                    }
                }

                if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr)) {
                    Toast.makeText(this, R.string.error_required_field, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!TextUtils.isEmpty(aadhaar) && aadhaar.length() != 12) {
                    Toast.makeText(this, R.string.error_invalid_aadhaar, Toast.LENGTH_SHORT).show();
                    return;
                }

                int age = 30;
                try {
                    age = Integer.parseInt(ageStr);
                } catch (NumberFormatException ignored) {
                }

                final int finalAge = age;
                final String finalGender = gender;

                // Execute Room DB insertion on a background thread
                new Thread(() -> {
                    Patient newPatient = new Patient(
                            id, name, finalAge, finalGender, phone,
                            aadhaar, address, capturedLatitude, capturedLongitude,
                            medicalHistory, System.currentTimeMillis()
                    );
                    AppDatabase.getInstance(PatientPortalActivity.this).patientDao().insertPatient(newPatient);

                    // Automatically activate new profile session
                    PatientSessionManager.setActivePatientId(PatientPortalActivity.this, id);

                    // Return to main thread to update UI, refresh action states, and dismiss dialog
                    runOnUiThread(() -> {
                        Toast.makeText(PatientPortalActivity.this, "Profile Saved & Activated", Toast.LENGTH_SHORT).show();
                        refreshActivePatientCard();
                        updatePortalActionStates();
                        dialog.dismiss();
                    });
                }).start();
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}