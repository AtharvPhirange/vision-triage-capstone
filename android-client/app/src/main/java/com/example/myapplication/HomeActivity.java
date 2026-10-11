package com.example.myapplication;
import android.content.Intent;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Module Cards
        CardView cardPatient = findViewById(R.id.cardPatientPortal);
        CardView cardTechnician = findViewById(R.id.cardTechnicianPortal);
        CardView cardConsultant = findViewById(R.id.cardConsultantPortal);
        FloatingActionButton fabScan = findViewById(R.id.fabScan);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // Remove background tinting for the bottom nav to keep it transparent inside the BottomAppBar
        bottomNav.setBackground(null);
        // Disable the placeholder item meant for the FAB spacing
        bottomNav.getMenu().getItem(2).setEnabled(false);

        // Set Card Click Listeners
        cardPatient.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PatientPortalActivity.class);
            startActivity(intent);
        });

        cardTechnician.setOnClickListener(v ->
                Toast.makeText(this, "Opening Technician Analytics...", Toast.LENGTH_SHORT).show()
        );

        cardConsultant.setOnClickListener(v ->
                Toast.makeText(this, "Opening Consultant Review Queue...", Toast.LENGTH_SHORT).show()
        );

        // FAB Scan Action[cite: 22]
        fabScan.setOnClickListener(v ->
                Toast.makeText(this, "Initiating Hardware Scan...", Toast.LENGTH_SHORT).show()
        );

        // Bottom Navigation Listeners[cite: 22]
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_records) {
                Toast.makeText(this, "Loading Medical Records...", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_consents) {
                Toast.makeText(this, "Loading Scan History...", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Opening Profile Settings...", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}