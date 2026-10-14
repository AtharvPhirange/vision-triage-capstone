package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.ScanSyncQueue;
import java.util.List;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SyncWorker started execution. Checking network connectivity...");
        Context context = getApplicationContext();

        try {
            List<ScanSyncQueue> pendingItems = AppDatabase.getInstance(context)
                    .scanSyncDao()
                    .getPendingSyncItems();

            if (pendingItems != null && !pendingItems.isEmpty()) {
                Log.d(TAG, "Found " + pendingItems.size() + " pending scans to synchronize.");

                for (ScanSyncQueue item : pendingItems) {
                    // Simulate/execute cloud payload upload
                    Log.d(TAG, "Uploading scan record ID: " + item.getId() + " for " + item.getPatientName());

                    // Mark item as SYNCED in local Room DB
                    AppDatabase.getInstance(context)
                            .scanSyncDao()
                            .markAsSynced(item.getId());
                }
            } else {
                Log.d(TAG, "No pending scans found in local upload queue.");
            }

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "SyncWorker execution failed", e);
            return Result.retry();
        }
    }
}