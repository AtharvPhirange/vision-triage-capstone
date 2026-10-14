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
        clinics.add(new ClinicItem("District Ophthalmic Hospital", "Tarabai Park, Kolhapur, MH 416003", 16.7110, 74.2480));
        clinics.add(new ClinicItem("Vision Retina Institute", "Rajaram Puri, Kolhapur, MH 416008", 16.6980, 74.2390));
        clinics.add(new ClinicItem("Apex Eye Super Speciality Hospital", "Station Road, Kolhapur, MH 416001", 16.7020, 74.2510));

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
                float distKm = results[0] / 1000f;

                if (holder.tvClinicDistance != null) {
                    holder.tvClinicDistance.setText(String.format(Locale.getDefault(), "%.1f km away", distKm));
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