package com.example.myapplication.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "patients")
public class Patient {

    @PrimaryKey
    @NonNull
    private String patientId;
    private String fullName;
    private int age;
    private String gender;
    private String contactNumber;
    private String aadhaarNumber;
    private String address;
    private double latitude;
    private double longitude;
    private String medicalHistory;
    private long createdAt;

    public Patient(@NonNull String patientId, String fullName, int age, String gender, String contactNumber,
                   String aadhaarNumber, String address, double latitude, double longitude,
                   String medicalHistory, long createdAt) {
        this.patientId = patientId;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.aadhaarNumber = aadhaarNumber;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.medicalHistory = medicalHistory;
        this.createdAt = createdAt;
    }

    @Ignore
    public Patient(@NonNull String patientId, String fullName, int age, String gender, String contactNumber) {
        this(patientId, fullName, age, gender, contactNumber, "", "", 0.0, 0.0, "", System.currentTimeMillis());
    }

    @NonNull
    public String getPatientId() { return patientId; }
    public String getFullName() { return fullName; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getContactNumber() { return contactNumber; }
    public String getAadhaarNumber() { return aadhaarNumber; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getMedicalHistory() { return medicalHistory; }
    public long getCreatedAt() { return createdAt; }
}