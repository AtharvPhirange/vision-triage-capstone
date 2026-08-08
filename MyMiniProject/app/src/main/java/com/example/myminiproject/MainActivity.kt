package com.example.myminiproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myminiproject.db.AppDatabase
import com.example.myminiproject.db.ScanRecord
import com.example.myminiproject.network.SyncScheduler
import java.util.UUID

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example trigger: Call this function when a scan finishes or when button is tapped
        saveAndSyncScan(
            patientName = "PATIENT-101",
            baselineSize = 4.2,
            minSize = 2.5,
            latency = 210L,
            triageResult = "NORMAL"
        )
    }

    /**
     * Call this function whenever a new scan is completed.
     * It generates a unique scan ID, saves to Room, and triggers WorkManager sync.
     */
    private fun saveAndSyncScan(
        patientName: String,
        baselineSize: Double,
        minSize: Double,
        latency: Long,
        triageResult: String
    ) {
        Thread {
            val db = AppDatabase.getInstance(this)

            // Generate a dynamic unique ID for every new scan
            val uniqueScanId = UUID.randomUUID().toString()

            val dynamicScan = ScanRecord(
                uniqueScanId,
                patientName,
                System.currentTimeMillis(),
                baselineSize,
                minSize,
                latency,
                triageResult,
                false // isSynced = false so SyncWorker uploads it
            )

            // 1. Insert into local Room SQLite database
            db.scanRecordDao().insert(dynamicScan)

            // 2. Trigger WorkManager to push to Spring Boot backend
            SyncScheduler.scheduleSync(this)
        }.start()
    }
}