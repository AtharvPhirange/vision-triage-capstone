package com.example.myapplication;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DiscoverFragment extends Fragment {

    private static class ClinicItem {
        String name;
        String address;
        double lat;
        double lon;

        ClinicItem(String name, String address, double lat, double lon) {
            this.name = name;
            this.address = address;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private final double currentLat = 16.7050; // Kolhapur default
    private final double currentLon = 74.2433;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvClinics = view.findViewById(R.id.rvClinics);
        if (rvClinics != null) {
            rvClinics.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        List<ClinicItem> clinics = new ArrayList<>();
        clinics.add(new ClinicItem("Gokul Shirgaon Ophthalmic Center", "Gokul Shirgaon MIDC, MH 416234", 16.6340, 74.2750));
        clinics.add(new ClinicItem("Shiroli Netralaya & Laser Vision Care", "Shiroli Highway, Kolhapur, MH 416122", 16.7520, 74.2810));
        clinics.add(new ClinicItem("Sanjeevan Netralaya & Retinal Center", "Peth Vadgaon, Kolhapur, MH 416112", 16.8280, 74.3015));
        clinics.add(new ClinicItem("Drishti Eye & Micro-Surgery Clinic", "Kagal City, Kolhapur, MH 416216", 16.5780, 74.3150));
        clinics.add(new ClinicItem("Panhala Community Ophthalmic Hospital", "Panhala Fort Road, MH 416201", 16.8120, 74.1180));
        clinics.add(new ClinicItem("Lions Eye Hospital & Corneal Clinic", "Hatkanangale, Kolhapur, MH 416109", 16.7510, 74.4420));

        // Sort clinics in ascending distance order
        Collections.sort(clinics, (c1, c2) -> {
            float[] r1 = new float[1];
            float[] r2 = new float[1];
            Location.distanceBetween(currentLat, currentLon, c1.lat, c1.lon, r1);
            Location.distanceBetween(currentLat, currentLon, c2.lat, c2.lon, r2);
            return Float.compare(r1[0], r2[0]);
        });

        if (rvClinics != null) {
            rvClinics.setAdapter(new ClinicAdapter(clinics, currentLat, currentLon, clinic -> {
                Uri gmmIntentUri = Uri.parse("geo:" + clinic.lat + "," + clinic.lon + "?q=" + Uri.encode(clinic.name));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    startActivity(fallbackIntent);
                }
            }));
        }
    }

    // --- Inner Adapter ---
    private static class ClinicAdapter extends RecyclerView.Adapter<ClinicAdapter.ViewHolder> {
        private final List<ClinicItem> clinics;
        private final double userLat, userLon;
        private final OnClinicClickListener listener;

        interface OnClinicClickListener {
            void onClinicClick(ClinicItem clinic);
        }

        ClinicAdapter(List<ClinicItem> clinics, double userLat, double userLon, OnClinicClickListener listener) {
            this.clinics = clinics;
            this.userLat = userLat;
            this.userLon = userLon;
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
            ClinicItem clinic = clinics.get(position);
            if (clinic != null) {
                if (holder.tvClinicName != null) holder.tvClinicName.setText(clinic.name);
                if (holder.tvClinicAddress != null) holder.tvClinicAddress.setText(clinic.address);

                float[] results = new float[1];
                Location.distanceBetween(userLat, userLon, clinic.lat, clinic.lon, results);
                float distMeters = results[0];

                String distText = (distMeters < 1000)
                        ? Math.round(distMeters) + " m away"
                        : String.format(Locale.getDefault(), "%.1f km away", distMeters / 1000f);

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