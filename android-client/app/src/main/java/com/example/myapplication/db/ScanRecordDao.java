package com.example.myapplication.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScanRecordDao {

    @Insert
    void insertRecord(ScanRecord record);

    @Insert
    void insert(ScanRecord record);

    @Update
    void update(ScanRecord record);

    @Delete
    void deleteRecord(ScanRecord record);

    @Query("SELECT * FROM scan_records ORDER BY scanTimestamp DESC")
    LiveData<List<ScanRecord>> getAllObservableRecords();

    @Query("SELECT * FROM scan_records WHERE patientName = :patientId ORDER BY scanTimestamp DESC")
    LiveData<List<ScanRecord>> getObservableRecordsByPatient(String patientId);

    @Query("SELECT * FROM scan_records WHERE isSynced = 0")
    List<ScanRecord> getUnsyncedRecords();

    @Query("SELECT * FROM scan_records")
    List<ScanRecord> getAllRecordsForSync();
}