package com.example.myapplication.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScanRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ScanRecord scanRecord);

    @Update
    void update(ScanRecord scanRecord);

    @Query("SELECT * FROM scan_records WHERE isSynced = 0")
    List<ScanRecord> getUnsyncedRecords();

    @Query("UPDATE scan_records SET isSynced = 1 WHERE scanId = :scanId")
    void markAsSynced(String scanId);

    @Query("SELECT * FROM scan_records WHERE patientName = :patientName ORDER BY scanTimestamp DESC")
    List<ScanRecord> getRecordsByPatient(String patientName);

    @Query("SELECT * FROM scan_records WHERE patientName = :patientName ORDER BY scanTimestamp DESC")
    LiveData<List<ScanRecord>> getObservableRecordsByPatient(String patientName);

    @Query("SELECT * FROM scan_records ORDER BY scanTimestamp DESC")
    List<ScanRecord> getAllRecords();
}