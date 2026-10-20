package com.example.myapplication.data.model;

import java.util.Locale;

public class EyeHospital {
    private final String id;
    private final String name;
    private final String address;
    private final double latitude;
    private final double longitude;
    private float distanceKm;

    public EyeHospital(String id, String name, String address, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getDistanceKm() { return distanceKm; }

    public void setDistanceKm(float distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getFormattedDistance() {
        if (distanceKm < 1.0f) {
            int meters = Math.round(distanceKm * 1000f);
            return meters + " m away";
        }
        return String.format(Locale.US, "%.1f km away", distanceKm);
    }
}