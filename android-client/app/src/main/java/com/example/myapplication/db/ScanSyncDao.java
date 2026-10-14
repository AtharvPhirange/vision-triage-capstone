package com.example.myapplication.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScanSyncDao {

    @Insert
    void insertSyncItem(ScanSyncQueue item);

    @Update
    void updateSyncItem(ScanSyncQueue item);

    @Query("SELECT * FROM scan_sync_queue ORDER BY timestamp DESC")
    LiveData<List<ScanSyncQueue>> getObservableSyncQueue();

    @Query("SELECT * FROM scan_sync_queue WHERE syncStatus = 'PENDING_SYNC'")
    List<ScanSyncQueue> getPendingSyncItems();

    @Query("UPDATE scan_sync_queue SET syncStatus = 'SYNCED' WHERE id = :id")
    void markAsSynced(long id);
}