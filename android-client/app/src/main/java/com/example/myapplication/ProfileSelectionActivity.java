package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.Patient;
import java.util.ArrayList;
import java.util.List;

public class ProfileSelectionActivity extends AppCompatActivity {

    private RecyclerView rvProfiles;
    private ProfileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        rvProfiles = findViewById(R.id.rvProfiles);

        // Ensure LayoutManager is set before setting the adapter
        if (rvProfiles != null) {
            rvProfiles.setLayoutManager(new LinearLayoutManager(this));
            loadProfilesFromDatabase();
        }
    }

    private void loadProfilesFromDatabase() {
        AppDatabase.getInstance(this).patientDao().getAllPatients().observe(this, savedPatients -> {
            if (savedPatients != null) {
                adapter = new ProfileAdapter(savedPatients, patient -> {
                    Intent intent = new Intent(ProfileSelectionActivity.this, PatientPortalActivity.class);
                    // IMPORTANT: Change getPatientId() to match your Patient.java class if it highlights red
                    intent.putExtra("SELECTED_PATIENT_ID", patient.getPatientId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
                rvProfiles.setAdapter(adapter);
            }
        });
    }

    // --- Inner Adapter Class ---
    private static class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
        private final List<Patient> patients;
        private final OnProfileClickListener listener;

        interface OnProfileClickListener {
            void onProfileClick(Patient patient);
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
            holder.tvProfileName.setText(patient.getFullName());
            // IMPORTANT: Change getPatientId() to match your Patient.java class if it highlights red
            holder.tvProfileId.setText(patient.getPatientId());
            holder.itemView.setOnClickListener(v -> listener.onProfileClick(patient));
        }

        @Override
        public int getItemCount() {
            return patients.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvProfileName, tvProfileId;
            ViewHolder(View itemView) {
                super(itemView);
                tvProfileName = itemView.findViewById(R.id.tvProfileName);
                tvProfileId = itemView.findViewById(R.id.tvProfileId);
            }
        }
    }
}