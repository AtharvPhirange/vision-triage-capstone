package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.Patient;

import java.util.List;
import java.util.Locale;

public class PatientPortalActivity extends AppCompatActivity {

    private TextView tvActivePatientDisplay;

    private double capturedLatitude = 16.7050; // Default GPS Latitude
    private double capturedLongitude = 74.2433; // Default GPS Longitude
    private TextView activeLocationTv;
    private TextInputEditText activeAddressEt;

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
        CardView cardInitiateScan = findViewById(R.id.cardInitiateScan);
        CardView cardViewHistory = findViewById(R.id.cardViewHistory);
        Button btnRegisterPatient = findViewById(R.id.btnRegisterPatient);

        // Routes to the Profile Selection View
        cardActiveProfile.setOnClickListener(v -> {
            Intent intent = new Intent(PatientPortalActivity.this, ProfileSelectionActivity.class);
            startActivity(intent);
        });

        cardInitiateScan.setOnClickListener(v -> {
            String activeId = PatientSessionManager.getActivePatientId(PatientPortalActivity.this);
            Intent intent = new Intent(PatientPortalActivity.this, MainActivity.class);
            intent.putExtra("PATIENT_ID", activeId);
            startActivity(intent);
        });

        cardViewHistory.setOnClickListener(v -> {
            String activeId = PatientSessionManager.getActivePatientId(PatientPortalActivity.this);
            Intent intent = new Intent(PatientPortalActivity.this, ScanHistoryActivity.class);
            intent.putExtra("PATIENT_ID", activeId);
            startActivity(intent);
        });

        btnRegisterPatient.setOnClickListener(v -> showRegistrationDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshActivePatientCard();
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
                        activeLocationTv.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", capturedLatitude, capturedLongitude));
                    }
                } else {
                    if (activeLocationTv != null) {
                        activeLocationTv.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f (Default)", capturedLatitude, capturedLongitude));
                    }
                }

                reverseGeocodeAddress(capturedLatitude, capturedLongitude);
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
                    if (addr.getSubLocality() != null) sb.append(addr.getSubLocality()).append(", ");
                    if (addr.getLocality() != null) sb.append(addr.getLocality()).append(", ");
                    if (addr.getAdminArea() != null) sb.append(addr.getAdminArea()).append(" ");
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

    private void showRegistrationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_register_patient);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextInputEditText etPatientId = dialog.findViewById(R.id.etPatientId);
        TextInputEditText etFullName = dialog.findViewById(R.id.etFullName);
        TextInputEditText etAge = dialog.findViewById(R.id.etAge);
        RadioGroup rgGender = dialog.findViewById(R.id.rgGender);
        TextInputEditText etPhone = dialog.findViewById(R.id.etPhone);
        TextInputEditText etAadhaar = dialog.findViewById(R.id.etAadhaar);
        activeAddressEt = dialog.findViewById(R.id.etAddress);
        MaterialButton btnFetchLocation = dialog.findViewById(R.id.btnFetchLocation);
        activeLocationTv = dialog.findViewById(R.id.tvLocationCoordinates);
        TextInputEditText etMedicalHistory = dialog.findViewById(R.id.etMedicalHistory);

        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Pre-fill auto-generated patient ID
        if (etPatientId != null && (etPatientId.getText() == null || TextUtils.isEmpty(etPatientId.getText().toString()))) {
            etPatientId.setText("PAT-" + (System.currentTimeMillis() % 10000));
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
        btnSave.setOnClickListener(view -> {
            String id = etPatientId != null && etPatientId.getText() != null ? etPatientId.getText().toString().trim() : "";
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
            } catch (NumberFormatException ignored) {}

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

                // Set as active patient session
                PatientSessionManager.setActivePatientId(PatientPortalActivity.this, id);

                // Return to main thread to update UI and dismiss dialog
                runOnUiThread(() -> {
                    Toast.makeText(PatientPortalActivity.this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
                    refreshActivePatientCard();
                    dialog.dismiss();
                });
            }).start();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}