package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.data.model.EyeHospital;
import com.google.android.material.button.MaterialButton;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.ViewHolder> {

    private final Context context;
    private final List<EyeHospital> hospitals = new ArrayList<>();

    public HospitalAdapter(Context context) {
        this.context = context;
    }

    public void setHospitals(List<EyeHospital> newHospitals) {
        this.hospitals.clear();
        if (newHospitals != null) {
            this.hospitals.addAll(newHospitals);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_hospital_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EyeHospital hospital = hospitals.get(position);

        if (holder.tvName != null) holder.tvName.setText(hospital.getName());
        if (holder.tvAddress != null) holder.tvAddress.setText(hospital.getAddress());

        if (holder.tvDistance != null) {
            if (hospital.getDistanceKm() < 1.0f) {
                int meters = Math.round(hospital.getDistanceKm() * 1000f);
                holder.tvDistance.setText(context.getString(R.string.distance_meters_away, meters));
            } else {
                holder.tvDistance.setText(context.getString(R.string.distance_km_away, hospital.getDistanceKm()));
            }
        }

        if (holder.btnDirections != null) {
            holder.btnDirections.setText(context.getString(R.string.btn_get_directions));
        }

        // Labeled Google Maps Navigation: Opens Google Maps showing Clinic Name instead of coordinates
        View.OnClickListener launchNav = v -> {
            try {
                String destinationQuery = hospital.getName() + ", " + hospital.getAddress();
                String encodedDest = URLEncoder.encode(destinationQuery, StandardCharsets.UTF_8.name());

                // Official Google Maps Directions API URL format with place ID
                StringBuilder mapUrl = new StringBuilder("https://www.google.com/maps/dir/?api=1&destination=");
                mapUrl.append(encodedDest);

                if (hospital.getId() != null && hospital.getId().startsWith("ChIJ")) {
                    mapUrl.append("&destination_place_id=").append(hospital.getId());
                }
                mapUrl.append("&travelmode=driving");

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl.toString()));
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                } else {
                    // Fallback to browser or any mapping app
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl.toString())));
                }
            } catch (Exception ex) {
                // Secondary labeled geo intent fallback
                String geoUri = "geo:" + hospital.getLatitude() + "," + hospital.getLongitude()
                        + "?q=" + Uri.encode(hospital.getLatitude() + "," + hospital.getLongitude() + "(" + hospital.getName() + ")");
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)));
                } catch (Exception ignored) {}
            }
        };

        if (holder.btnDirections != null) holder.btnDirections.setOnClickListener(launchNav);
        holder.itemView.setOnClickListener(launchNav);
    }

    @Override
    public int getItemCount() {
        return hospitals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvDistance;
        MaterialButton btnDirections;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHospitalName);
            tvAddress = itemView.findViewById(R.id.tvHospitalAddress);
            tvDistance = itemView.findViewById(R.id.tvDistanceBadge);
            btnDirections = itemView.findViewById(R.id.btnGetDirections);
        }
    }
}