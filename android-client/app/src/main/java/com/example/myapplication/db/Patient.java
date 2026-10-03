package com.example.myapplication.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
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

    public Patient(@NonNull String patientId, String fullName, int age, String gender, String contactNumber) {
        this.patientId = patientId;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
    }

    @NonNull
    public String getPatientId() { return patientId; }
    public String getFullName() { return fullName; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getContactNumber() { return contactNumber; }
}