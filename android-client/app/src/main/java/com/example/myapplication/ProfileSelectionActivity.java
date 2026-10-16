package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.Patient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileSelectionActivity extends BaseActivity {

    private RecyclerView rvProfiles;
    private TextView tvEmptyState;
    private ProfileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        rvProfiles = findViewById(R.id.rvProfiles);
        ImageView btnBack = findViewById(R.id.btnBack);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (rvProfiles != null) {
            rvProfiles.setLayoutManager(new LinearLayoutManager(this));
            loadProfilesFromDatabase();
        }
    }

    private void loadProfilesFromDatabase() {
        AppDatabase.getInstance(this).patientDao().getAllPatients().observe(this, savedPatients -> {
            if (savedPatients == null || savedPatients.isEmpty()) {
                if (tvEmptyState != null) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                }
                if (rvProfiles != null) {
                    rvProfiles.setVisibility(View.GONE);
                }
            } else {
                if (tvEmptyState != null) {
                    tvEmptyState.setVisibility(View.GONE);
                }
                if (rvProfiles != null) {
                    rvProfiles.setVisibility(View.VISIBLE);
                }

                adapter = new ProfileAdapter(savedPatients, new ProfileAdapter.OnProfileClickListener() {
                    @Override
                    public void onProfileClick(Patient patient) {
                        // Activate Selected Patient in Session
                        PatientSessionManager.setActivePatientId(ProfileSelectionActivity.this, patient.getPatientId());

                        Toast.makeText(ProfileSelectionActivity.this,
                                "Active Patient Updated: " + patient.getFullName(),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ProfileSelectionActivity.this, PatientPortalActivity.class);
                        intent.putExtra("SELECTED_PATIENT_ID", patient.getPatientId());
                        intent.putExtra("PATIENT_NAME", patient.getFullName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onDeleteClick(Patient patient) {
                        confirmAndDeletePatient(patient);
                    }
                });
                rvProfiles.setAdapter(adapter);
            }
        });
    }

    private void confirmAndDeletePatient(Patient patient) {
        if (patient == null) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_patient_title)
                .setMessage(R.string.dialog_delete_patient_desc)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    new Thread(() -> {
                        Context context = getApplicationContext();

                        // 1. Delete associated scan records from Room DB
                        AppDatabase.getInstance(context).scanRecordDao().deleteScanRecordsByPatient(patient.getPatientId());

                        // 2. Delete patient profile from Room DB
                        AppDatabase.getInstance(context).patientDao().deletePatientById(patient.getPatientId());

                        // 3. Delete cached PDF file from disk
                        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                        if (directory != null && directory.exists()) {
                            File pdfFile = new File(directory, "Triage_Report_" + patient.getPatientId() + ".pdf");
                            if (pdfFile.exists()) {
                                boolean deleted = pdfFile.delete();
                            }
                        }

                        // 4. Clear active session if this was the active patient
                        if (PatientSessionManager.getActivePatientId(context).equalsIgnoreCase(patient.getPatientId())) {
                            PatientSessionManager.clearActivePatient(context);
                        }

                        // 5. Notify user on UI thread
                        runOnUiThread(() -> Toast.makeText(ProfileSelectionActivity.this,
                                "Profile Deleted Successfully", Toast.LENGTH_SHORT).show());
                    }).start();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    // --- Inner Adapter Class ---
    private static class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
        private final List<Patient> patients;
        private final OnProfileClickListener listener;

        interface OnProfileClickListener {
            void onProfileClick(Patient patient);
            void onDeleteClick(Patient patient);
        }

        ProfileAdapter(List<Patient> patients, OnProfileClickListener listener) {
            this.patients = patients != null ? patients : new ArrayList<>();
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Patient patient = patients.get(position);
            if (patient != null) {
                if (holder.tvProfileName != null) {
                    holder.tvProfileName.setText(patient.getFullName());
                }
                if (holder.tvProfileId != null) {
                    holder.tvProfileId.setText(patient.getPatientId());
                }

                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onProfileClick(patient);
                    }
                });

                if (holder.btnDeletePatient != null) {
                    holder.btnDeletePatient.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onDeleteClick(patient);
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return patients.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvProfileName;
            TextView tvProfileId;
            ImageView btnDeletePatient;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvProfileName = itemView.findViewById(R.id.tvProfileName);
                tvProfileId = itemView.findViewById(R.id.tvProfileId);
                btnDeletePatient = itemView.findViewById(R.id.btnDeletePatient);
            }
        }
    }
}