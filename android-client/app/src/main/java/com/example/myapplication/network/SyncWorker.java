package com.example.myapplication.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.ScanRecord;
import com.example.myapplication.db.ScanRecordDao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";
    private static final String SERVER_URL = "http://127.0.0.1:8080/api/v1/scans";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        ScanRecordDao dao = db.scanRecordDao();
        List<ScanRecord> unsyncedRecords = dao.getUnsyncedRecords();

        if (unsyncedRecords == null || unsyncedRecords.isEmpty()) {
            Log.d(TAG, "No unsynced records found.");
            return Result.success();
        }

        Log.d(TAG, "Found " + unsyncedRecords.size() + " unsynced records. Starting sync...");

        for (ScanRecord record : unsyncedRecords) {
            boolean success = syncRecordToServer(record);
            if (success) {
                record.setSynced(true);
                dao.update(record);
                Log.d(TAG, "Successfully synced record: " + record.getScanId());
            } else {
                Log.e(TAG, "Failed to sync record: " + record.getScanId() + ". Will retry later.");
                return Result.retry();
            }
        }

        return Result.success();
    }

    private boolean syncRecordToServer(ScanRecord record) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(SERVER_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);

            // API Level 24 compatible ISO-8601 formatting
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String capturedAtIso = sdf.format(new Date(record.getScanTimestamp()));

            String jsonPayload = String.format(Locale.US,
                    "{"
                            + "\"deviceId\":\"%s\","
                            + "\"verdict\":\"%s\","
                            + "\"minDiameterMm\":%.2f,"
                            + "\"maxDiameterMm\":%.2f,"
                            + "\"constrictionLatencyMs\":%d,"
                            + "\"capturedAt\":\"%s\","
                            + "\"dilationCurve\":[]"
                            + "}",
                    record.getScanId(),
                    record.getTriageResult(),
                    record.getMinimumPupilSize(),
                    record.getBaselinePupilSize(),
                    record.getConstrictionLatencyMs(),
                    capturedAtIso
            );

            Log.d(TAG, "Sending payload: " + jsonPayload);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Server response code: " + responseCode);

            if (responseCode == 200 || responseCode == 201) {
                return true;
            } else {
                if (conn.getErrorStream() != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.e(TAG, "Server error response: " + response.toString());
                    }
                }
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during sync: " + e.getMessage(), e);
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}