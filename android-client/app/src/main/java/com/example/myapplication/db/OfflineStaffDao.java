package com.example.myapplication.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface OfflineStaffDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void registerStaff(OfflineStaff staff);

    @Query("SELECT * FROM offline_staff_credentials WHERE staffId = :staffId LIMIT 1")
    OfflineStaff getStaffById(String staffId);
}