package com.example.myapplication;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DiscoverClinicsActivity extends BaseActivity {

    private double currentLat = 16.7050; // Kolhapur default
    private double currentLon = 74.2433;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_clinics);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        RecyclerView rvClinics = findViewById(R.id.rvClinics);
        if (rvClinics != null) {
            rvClinics.setLayoutManager(new LinearLayoutManager(this));
        }

        List<ClinicModel> clinicList = new ArrayList<>();
        clinicList.add(new ClinicModel("Nandadeep Eye Hospital", "107, Dattawad Renaissance, New Shahupuri, Kolhapur 416001", "0231-2651234", 16.7032, 74.2415));
        clinicList.add(new ClinicModel("Akashdeep Netralay (Laser & Eye Hospital)", "Lohia Lane, Sykes Extension, Rajarampuri, Kolhapur 416001", "0231-2524321", 16.6912, 74.2460));
        clinicList.add(new ClinicModel("Dr. Agarwal's Eye Hospital", "Raysons Galleria, Tarabai Park, Kolhapur 416003", "0231-6618000", 16.7118, 74.2384));
        clinicList.add(new ClinicModel("Vasan Eye Care Hospital", "Station Road, Shahupuri, Kolhapur 416001", "0231-3982000", 16.7056, 74.2482));
        clinicList.add(new ClinicModel("Aster Aadhar Hospital (Ophthalmology Wing)", "Pratibhanagar, Near KMT Workshop, Kolhapur 416008", "0231-6622555", 16.6784, 74.2278));
        clinicList.add(new ClinicModel("Pragati Netra Rugnalay", "Building 1982, 9th Lane, Rajarampuri, Kolhapur 416008", "0231-2538900", 16.6885, 74.2475));
        clinicList.add(new ClinicModel("Shri Navdurga Eye Hospital", "Near Rankala ST Stand, Rankala, Kolhapur 416012", "0231-2621100", 16.6890, 74.2180));

        float[] results = new float[1];
        for (ClinicModel clinic : clinicList) {
            Location.distanceBetween(currentLat, currentLon, clinic.getLatitude(), clinic.getLongitude(), results);
            clinic.setDistanceKm(results[0] / 1000.0f);
        }

        // Sort clinics in strict ascending distance order
        Collections.sort(clinicList, (c1, c2) -> Float.compare(c1.getDistanceKm(), c2.getDistanceKm()));

        if (rvClinics != null) {
            rvClinics.setAdapter(new ClinicAdapter(clinicList, clinic -> {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + clinic.getLatitude() + "," + clinic.getLongitude() + "&mode=d");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + clinic.getLatitude() + "," + clinic.getLongitude()));
                    startActivity(fallbackIntent);
                }
            }));
        }
    }

    // --- Inner Adapter ---
    private static class ClinicAdapter extends RecyclerView.Adapter<ClinicAdapter.ViewHolder> {
        private final List<ClinicModel> clinics;
        private final OnClinicClickListener listener;

        interface OnClinicClickListener {
            void onClinicClick(ClinicModel clinic);
        }

        ClinicAdapter(List<ClinicModel> clinics, OnClinicClickListener listener) {
            this.clinics = clinics;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clinic_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ClinicModel clinic = clinics.get(position);
            if (clinic != null) {
                if (holder.tvClinicName != null) holder.tvClinicName.setText(clinic.getName());
                if (holder.tvClinicAddress != null) holder.tvClinicAddress.setText(clinic.getAddress());

                String distText = String.format(Locale.US, "%.1f km away", clinic.getDistanceKm());
                if (holder.tvClinicDistance != null) {
                    holder.tvClinicDistance.setText(distText);
                }

                if (holder.btnGetDirections != null) {
                    holder.btnGetDirections.setOnClickListener(v -> {
                        if (listener != null) listener.onClinicClick(clinic);
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return clinics != null ? clinics.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvClinicName, tvClinicAddress, tvClinicDistance;
            MaterialButton btnGetDirections;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvClinicName = itemView.findViewById(R.id.tvClinicName);
                tvClinicAddress = itemView.findViewById(R.id.tvClinicAddress);
                tvClinicDistance = itemView.findViewById(R.id.tvClinicDistance);
                btnGetDirections = itemView.findViewById(R.id.btnGetDirections);
            }
        }
    }
}