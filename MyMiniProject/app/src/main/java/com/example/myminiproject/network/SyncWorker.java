package com.example.myminiproject.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myminiproject.db.AppDatabase;
import com.example.myminiproject.db.ScanRecord;
import com.example.myminiproject.db.ScanRecordDao;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        ScanRecordDao dao = db.scanRecordDao();
        List<ScanRecord> unsyncedRecords = dao.getUnsyncedRecords();

        if (unsyncedRecords.isEmpty()) {
            return Result.success();
        }

        for (ScanRecord record : unsyncedRecords) {
            boolean success = syncRecordToServer(record);
            if (success) {
                record.setIsSynced(true);
                dao.update(record);
            } else {
                return Result.retry();
            }
        }

        return Result.success();
    }

    private boolean syncRecordToServer(ScanRecord record) {
        try {
            URL url = new URL("http://10.0.2.2:8080/api/v1/scans/sync");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String jsonPayload = String.format(
                    "{\"scanId\":\"%s\",\"patientName\":\"%s\",\"scanTimestamp\":%d,\"baselinePupilSize\":%f,\"minimumPupilSize\":%f,\"constrictionLatencyMs\":%d,\"triageResult\":\"%s\",\"isSynced\":true}",
                    record.getScanId(),
                    record.getPatientName(),
                    record.getScanTimestamp(),
                    record.getBaselinePupilSize(),
                    record.getMinimumPupilSize(),
                    record.getConstrictionLatencyMs(),
                    record.getTriageResult()
            );

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            return responseCode == 200 || responseCode == 201;
        } catch (Exception e) {
            return false;
        }
    }
}