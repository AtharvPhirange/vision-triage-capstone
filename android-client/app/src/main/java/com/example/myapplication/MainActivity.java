package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.util.concurrent.ListenableFuture;

import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.ScanDataMapper;
import com.example.myapplication.db.ScanRecord;
import com.example.myapplication.network.SyncScheduler;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    public Camera globalCamera;
    public int lensFacing = CameraSelector.LENS_FACING_FRONT;
    public ProcessCameraProvider globalCameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    // UI Elements mapped to new XML
    private PreviewView viewFinder;
    private TextView tvStatus;
    private TextView tvTimer;
    private Button btnScan;
    private Button btnFlipLens;
    private LineChart pupilChart;

    // Chart Data
    private LineDataSet pupilDataSet;
    private LineData lineData;
    private long startTime = 0;
    private boolean isGraphActive = false;

    // Core AI
    private VisionBrain visionBrain;

    // Dynamic Patient ID
    private String currentPatientId = "Patient-01"; // Default fallback

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera Permission Required", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View btnScannerBack = findViewById(R.id.btnScannerBack);
        if (btnScannerBack != null) {
            btnScannerBack.setOnClickListener(v -> finish());
        }

        // Catch the dynamic Patient ID passed from HomeActivity
        if (getIntent() != null && getIntent().hasExtra("PATIENT_ID")) {
            currentPatientId = getIntent().getStringExtra("PATIENT_ID");
        }

        // Initialize UI Elements
        viewFinder = findViewById(R.id.viewFinder);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        btnScan = findViewById(R.id.btnScan);
        btnFlipLens = findViewById(R.id.btnFlipLens);

        // Click Listeners
        btnFlipLens.setOnClickListener(v -> switchCamera());
        btnScan.setOnClickListener(v -> startTriageTest());

        // Initialize AI
        visionBrain = new VisionBrain();
        visionBrain.initializeAI(this);

        setupChart();

        // Check Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    public void startTriageTest() {
        btnScan.setEnabled(false);
        btnScan.setAlpha(0.5f);
        tvTimer.setVisibility(View.VISIBLE);
        tvStatus.setText("Scanning...");
        tvStatus.setTextColor(Color.WHITE);

        clearGraph();

        if (visionBrain != null) {
            visionBrain.resetSmoothing(); // Clear old graph data
            if (visionBrain.triageEngine != null) {
                visionBrain.triageEngine.validFrameCount = 0;
                visionBrain.triageEngine.startTest(visionBrain.latestRatio);
            }
        }

        startTime = System.currentTimeMillis();
        isGraphActive = true;

        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000 + 1;
                tvTimer.setText("00:0" + secondsLeft);
            }
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                isGraphActive = false;
            }
        }.start();

        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());

        // STIMULUS PHASE
        handler.postDelayed(() -> {
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                blastScreenBrightness();
            } else if (globalCamera != null) {
                try {
                    globalCamera.getCameraControl().enableTorch(true);
                    Log.d(TAG, "Hardware override: Forced Flash ON");
                } catch (Exception e) {
                    Log.e(TAG, "Torch failed to fire", e);
                }
            }
        }, 1000);

        // RECOVERY PHASE
        handler.postDelayed(() -> {
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                restoreScreenBrightness();
            } else if (globalCamera != null) {
                try {
                    globalCamera.getCameraControl().enableTorch(false);
                    Log.d(TAG, "Hardware override: Forced Flash OFF");
                } catch (Exception e) {}
            }
        }, 2000);

        // FINISH PHASE
        handler.postDelayed(() -> {
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                restoreScreenBrightness();
            } else if (globalCamera != null) {
                try {
                    globalCamera.getCameraControl().enableTorch(false);
                } catch (Exception e) {}
            }

            float baseline = visionBrain.triageEngine.baselineRatio;
            float minRatio = visionBrain.triageEngine.minConstrictedRatio;
            String result = visionBrain.triageEngine.getFinalDiagnosis(baseline, minRatio);

            tvStatus.setText(result);

            if (result.equals("NORMAL")) {
                tvStatus.setTextColor(android.graphics.Color.parseColor("#00FF00"));
            } else if (result.equals("SLUGGISH")) {
                tvStatus.setTextColor(android.graphics.Color.parseColor("#FFFF00"));
            } else {
                tvStatus.setTextColor(android.graphics.Color.parseColor("#FF0000"));
            }

            btnScan.setEnabled(true);
            btnScan.setAlpha(1.0f);
            btnScan.setText("SCAN AGAIN");

            if (!result.contains("ERROR")) {
                saveScanAndTriggerSync();
            }
        }, 3000);
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                globalCameraProvider = cameraProvider;
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error binding camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
            Bitmap bitmap = imageProxy.toBitmap();

            android.graphics.Matrix matrix = new android.graphics.Matrix();
            matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());
            Bitmap rotatedBitmap = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            long timestamp = imageProxy.getImageInfo().getTimestamp();
            visionBrain.detectFace(rotatedBitmap, timestamp);

            runOnUiThread(() -> {
                if (isGraphActive) {
                    if (visionBrain.latestRatio <= 0.0f) {
                        tvStatus.setText("Face Lost! Align Eyes.");
                        tvStatus.setTextColor(Color.parseColor("#FF9800"));
                    } else {
                        tvStatus.setText("Scanning...");
                        tvStatus.setTextColor(Color.WHITE);
                        updateLiveGraph(visionBrain.latestRatio);

                        if (visionBrain.triageEngine != null) {
                            visionBrain.triageEngine.validFrameCount++;
                        }
                    }
                }
            });

            imageProxy.close();
        });

        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
        cameraProvider.unbindAll();

        globalCamera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        globalCamera.getCameraControl().enableTorch(false);
    }

    private void setupChart() {
        pupilChart = findViewById(R.id.pupilChart);

        YAxis leftAxis = pupilChart.getAxisLeft();
        leftAxis.setAxisMinimum(0.0f);
        leftAxis.setAxisMaximum(0.6f);
        leftAxis.setTextColor(Color.WHITE);

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

    public void switchCamera() {
        lensFacing = (lensFacing == CameraSelector.LENS_FACING_FRONT)
                ? CameraSelector.LENS_FACING_BACK
                : CameraSelector.LENS_FACING_FRONT;

        if (globalCameraProvider != null) {
            globalCameraProvider.unbindAll();
            bindPreview(globalCameraProvider);
        }
    }

    private void clearGraph() {
        if (pupilDataSet != null && lineData != null && pupilChart != null) {
            pupilDataSet.clear();
            lineData.notifyDataChanged();
            pupilChart.notifyDataSetChanged();
            pupilChart.invalidate();
        }
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

    private void saveScanAndTriggerSync() {
        double baselinePupil = visionBrain.triageEngine.getBaselineRatio();
        double minPupil = visionBrain.triageEngine.getMinRatio();
        long latencyMs = visionBrain.triageEngine.getLatencyMs();
        String triageResult = visionBrain.triageEngine.getTriageResult() != null
                ? visionBrain.triageEngine.getTriageResult()
                : "NORMAL";

        // Now uses the dynamic Patient ID typed into the dashboard!
        ScanRecord record = ScanDataMapper.createRecord(
                currentPatientId,
                baselinePupil,
                minPupil,
                latencyMs,
                triageResult
        );

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.scanRecordDao().insert(record);
            Log.d(TAG, "Inserted new scan record for " + currentPatientId);
            SyncScheduler.scheduleSync(getApplicationContext());
        });
    }
}