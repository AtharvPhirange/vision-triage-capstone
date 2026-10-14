package com.example.myapplication.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_staff_credentials")
public class OfflineStaff {

    @PrimaryKey
    @NonNull
    private String staffId;
    private String fullName;
    private String role;
    private String hashedPin;
    private long registeredAt;

    public OfflineStaff(@NonNull String staffId, String fullName, String role, String hashedPin, long registeredAt) {
        this.staffId = staffId;
        this.fullName = fullName;
        this.role = role;
        this.hashedPin = hashedPin;
        this.registeredAt = registeredAt;
    }

    @NonNull
    public String getStaffId() { return staffId; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getHashedPin() { return hashedPin; }
    public long getRegisteredAt() { return registeredAt; }
}