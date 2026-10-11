package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.Patient;

public class PatientPortalActivity extends AppCompatActivity {

    private String activePatientId = "PAT-9105";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_portal);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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
            Intent intent = new Intent(PatientPortalActivity.this, MainActivity.class);
            intent.putExtra("PATIENT_ID", activePatientId);
            startActivity(intent);
        });

        cardViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(PatientPortalActivity.this, ScanHistoryActivity.class);
            intent.putExtra("PATIENT_ID", activePatientId);
            startActivity(intent);
        });

        btnRegisterPatient.setOnClickListener(v -> showRegistrationDialog());
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
        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Bind DB Logic to Save Button
        btnSave.setOnClickListener(view -> {
            String id = etPatientId.getText() != null ? etPatientId.getText().toString().trim() : "";
            String name = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
            String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";

            if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr)) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Execute Room DB insertion on a background thread
            new Thread(() -> {
                // Pass arguments directly into the constructor matching your Patient entity structure
                // Assuming the 5 arguments are: ID, FullName, Age, Gender, ContactNumber
                Patient newPatient = new Patient(id, name, Integer.parseInt(ageStr), "N/A", "");

                AppDatabase.getInstance(PatientPortalActivity.this).patientDao().insertPatient(newPatient);

                // Return to main thread to update UI and dismiss dialog
                runOnUiThread(() -> {
                    Toast.makeText(PatientPortalActivity.this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
                    activePatientId = id; // Automatically switch to the newly created profile
                    dialog.dismiss();
                });
            }).start();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}