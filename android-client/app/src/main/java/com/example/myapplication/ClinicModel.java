package com.example.myapplication;

public class ClinicModel {
    private String name;
    private String address;
    private String contact;
    private double latitude;
    private double longitude;
    private float distanceKm;

    public ClinicModel(String name, String address, String contact, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = 0.0f;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getContact() { return contact; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getDistanceKm() { return distanceKm; }
    public void setDistanceKm(float distanceKm) { this.distanceKm = distanceKm; }
}