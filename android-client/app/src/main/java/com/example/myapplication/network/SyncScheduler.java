package com.example.myapplication.network;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class SyncScheduler {

    private static final String TAG = "SyncScheduler";
    private static final String WORK_NAME = "scan_sync_work";

    public static void scheduleSync(Context context) {
        Log.d(TAG, "Enqueuing immediate sync request...");

        // CONNECTED allows any network type (Wi-Fi, 5G/LTE, or USB reverse tethering)
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        // Enqueue unique work to avoid redundant parallel triggers
        WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
        );
    }
}