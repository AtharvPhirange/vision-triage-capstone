package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

// Database and Sync Imports
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.ScanDataMapper;
import com.example.myapplication.db.ScanRecord;
import com.example.myapplication.network.SyncScheduler;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private LineChart pupilChart;
    private LineDataSet pupilDataSet;
    private LineData lineData;
    private long startTime = 0;
    private PreviewView viewFinder;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private VisionBrain visionBrain;

    // UI and RecyclerView Elements
    private RecyclerView recyclerViewScans;
    private ScanRecordAdapter scanAdapter;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewFinder = findViewById(R.id.viewFinder);

        visionBrain = new VisionBrain();
        visionBrain.initializeAI(this);

        setupChart();
        setupHistoryRecyclerView();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // Test cycle: starts test at 5s, blasts brightness, restores & persists at 8s
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            visionBrain.triageEngine.startTest(visionBrain.latestRatio);

            blastScreenBrightness();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                restoreScreenBrightness();
                saveScanAndTriggerSync();
            }, 3000);
        }, 5000);
    }

    private void setupHistoryRecyclerView() {
        recyclerViewScans = findViewById(R.id.recyclerViewScans);
        recyclerViewScans.setLayoutManager(new LinearLayoutManager(this));
        scanAdapter = new ScanRecordAdapter();
        recyclerViewScans.setAdapter(scanAdapter);

        // Reactive LiveData observer: Room automatically notifies adapter when isSynced changes
        AppDatabase.getInstance(this).scanRecordDao().getObservableRecordsByPatient("Patient-01")
                .observe(this, records -> {
                    if (records != null) {
                        Log.d(TAG, "History observer triggered with " + records.size() + " records");
                        scanAdapter.setScanList(records);
                    }
                });
    }

    private void saveScanAndTriggerSync() {
        double baselinePupil = visionBrain.triageEngine.getBaselineRatio();
        double minPupil = visionBrain.triageEngine.getMinRatio();
        long latencyMs = visionBrain.triageEngine.getLatencyMs();
        String triageResult = visionBrain.triageEngine.getTriageResult() != null
                ? visionBrain.triageEngine.getTriageResult()
                : "NORMAL";

        ScanRecord record = ScanDataMapper.createRecord(
                "Patient-01",
                baselinePupil,
                minPupil,
                latencyMs,
                triageResult
        );

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.scanRecordDao().insert(record);
            Log.d(TAG, "Inserted new scan record: " + record.getScanId());

            // Enqueue WorkManager sync immediately
            SyncScheduler.scheduleSync(getApplicationContext());
        });
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error binding camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
            Bitmap bitmap = imageProxy.toBitmap();
            long timestamp = imageProxy.getImageInfo().getTimestamp();

            visionBrain.detectFace(bitmap, timestamp);

            runOnUiThread(() -> updateLiveGraph(visionBrain.latestRatio));

            imageProxy.close();
        });

        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        cameraProvider.unbindAll();

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

        camera.getCameraControl().enableTorch(false);
    }

    public void blastScreenBrightness() {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 1.0f;
        getWindow().setAttributes(layout);
    }

    public void restoreScreenBrightness() {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = -1.0f;
        getWindow().setAttributes(layout);
    }

    private void setupChart() {
        pupilChart = findViewById(R.id.pupilChart);

        YAxis leftAxis = pupilChart.getAxisLeft();
        leftAxis.setAxisMinimum(0.0f);
        leftAxis.setAxisMaximum(0.6f);

        YAxis rightAxis = pupilChart.getAxisRight();
        rightAxis.setEnabled(false);

        pupilChart.setDrawGridBackground(false);
        pupilChart.getDescription().setEnabled(false);
        pupilChart.getLegend().setEnabled(false);
        pupilChart.getXAxis().setDrawLabels(false);

        pupilDataSet = new LineDataSet(new ArrayList<>(), "");
        pupilDataSet.setColor(Color.parseColor("#00FF00"));
        pupilDataSet.setDrawCircles(false);
        pupilDataSet.setDrawValues(false);
        pupilDataSet.setLineWidth(2.5f);
        pupilDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineData = new LineData(pupilDataSet);
        pupilChart.setData(lineData);
        startTime = System.currentTimeMillis();
    }

    public void updateLiveGraph(float currentRatio) {
        float timeElapsed = (System.currentTimeMillis() - startTime) / 1000f;

        lineData.addEntry(new Entry(timeElapsed, currentRatio), 0);
        lineData.notifyDataChanged();

        pupilChart.notifyDataSetChanged();
        pupilChart.setVisibleXRangeMaximum(10);
        pupilChart.moveViewToX(lineData.getEntryCount());
    }
}