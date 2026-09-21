package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
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
import com.example.myapplication.ui.ScanHistoryActivity;

public class MainActivity extends AppCompatActivity {

    private boolean isScanning = false;
    public Camera globalCamera;
    private TextView diagnosisText;
    private TextView warningText;
    private static final String TAG = "MainActivity";

    private LineChart pupilChart;
    private LineDataSet pupilDataSet;
    private LineData lineData;
    private long startTime = 0;

    private static final int CAMERA_PERMISSION_CODE = 1001;
    private PreviewView viewFinder;
    private VisionBrain visionBrain;

    public int lensFacing = CameraSelector.LENS_FACING_FRONT;
    public ProcessCameraProvider globalCameraProvider;

    private boolean isGraphActive = false;

    // UI and RecyclerView Elements
    private RecyclerView recyclerViewScans;
    private ScanRecordAdapter scanAdapter;

    private OverlayView overlayView;
    private TextView tvTimer;
    private Button btnStartScan;

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        viewFinder = findViewById(R.id.viewFinder);
        diagnosisText = findViewById(R.id.diagnosisText);
        warningText = findViewById(R.id.warningText);
        overlayView = findViewById(R.id.overlayView);
        tvTimer = findViewById(R.id.tvTimer);
        btnStartScan = findViewById(R.id.startScanButton);

        Button switchBtn = findViewById(R.id.flipCameraButton);
        switchBtn.setOnClickListener(v -> switchCamera());

        btnStartScan.setOnClickListener(v -> startTriageTest());

        // History Toggle Listener
        View historyRecycler = findViewById(R.id.recyclerViewScans);
        Button historyBtn = findViewById(R.id.btnViewHistory);
        if (historyBtn != null && historyRecycler != null) {
            historyBtn.setOnClickListener(v -> {
                if (historyRecycler.getVisibility() == View.VISIBLE) {
                    historyRecycler.setVisibility(View.GONE);
                    historyBtn.setText("VIEW HISTORY");
                } else {
                    historyRecycler.setVisibility(View.VISIBLE);
                    historyBtn.setText("HIDE HISTORY");
                }
            });
        }

        visionBrain = new VisionBrain();
        visionBrain.initializeAI(this);
        tvTimer.setText("00:03");

        setupChart();
        setupHistoryRecyclerView();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    public void startTriageTest() {
        btnStartScan.setEnabled(false);
        btnStartScan.setAlpha(0.5f);

        View historyRecycler = findViewById(R.id.recyclerViewScans);
        Button historyBtn = findViewById(R.id.btnViewHistory);

        diagnosisText.setVisibility(View.INVISIBLE);
        warningText.setVisibility(View.INVISIBLE);

        // Hide history cards during active scan
        if (historyRecycler != null) {
            historyRecycler.setVisibility(View.GONE);
        }
        if (historyBtn != null) {
            historyBtn.setText("VIEW HISTORY");
        }

        // Reset graph and UI completely before starting the new scan
        clearGraph();

        if (visionBrain != null && visionBrain.triageEngine != null) {
            visionBrain.triageEngine.validFrameCount = 0;
        }

        startTime = System.currentTimeMillis();
        isGraphActive = true;
        overlayView.setTargetLocked(true);
        tvTimer.setText("00:03");

        // Trigger stimulus (Flashlight or Screen Brightness)
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            blastScreenBrightness();
        } else if (globalCamera != null) {
            globalCamera.getCameraControl().enableTorch(true);
        }

        // 3-second scan sequence timer
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);
                tvTimer.setText("00:0" + secondsLeft);
            }

            public void onFinish() {
                tvTimer.setText("00:00");
                overlayView.setTargetLocked(false);

                // Restore lighting
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    restoreScreenBrightness();
                } else if (globalCamera != null) {
                    globalCamera.getCameraControl().enableTorch(false);
                }

                isGraphActive = false; // Freezes the graph rendering

                float baseline = visionBrain.triageEngine.baselineRatio;
                float minRatio = visionBrain.triageEngine.minConstrictedRatio;

                String result = visionBrain.triageEngine.getFinalDiagnosis(baseline, minRatio);

                if (result.equals("ERROR_INCOMPLETE")) {
                    diagnosisText.setText("Scan Failed: Face lost too often. Try again.");
                    diagnosisText.setTextColor(Color.parseColor("#FF9800"));
                } else {
                    diagnosisText.setText(result);
                    if (result.equals("NORMAL")) {
                        diagnosisText.setTextColor(Color.parseColor("#00FF00"));
                    } else if (result.equals("SLUGGISH")) {
                        diagnosisText.setTextColor(Color.parseColor("#FFFF00"));
                    } else {
                        diagnosisText.setTextColor(Color.parseColor("#FF0000"));
                    }
                    saveScanAndTriggerSync();
                }

                diagnosisText.setVisibility(View.VISIBLE);
                btnStartScan.setEnabled(true);
                btnStartScan.setAlpha(1.0f);
            }
        }.start();
    }

    private void setupChart() {
        pupilChart = findViewById(R.id.pupilChart);
        if (pupilChart != null) {
            pupilChart.getDescription().setEnabled(false);
            pupilChart.setTouchEnabled(true);
            pupilChart.setDragEnabled(true);
            pupilChart.setScaleEnabled(true);
            pupilChart.setPinchZoom(true);
            pupilChart.setBackgroundColor(Color.TRANSPARENT);

            lineData = new LineData();
            pupilChart.setData(lineData);

            pupilDataSet = new LineDataSet(new ArrayList<>(), "Pupil Ratio");
            pupilDataSet.setColor(Color.GREEN);
            pupilDataSet.setLineWidth(2f);
            pupilDataSet.setDrawCircles(false);
            pupilDataSet.setDrawValues(false);
            pupilDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            lineData.addDataSet(pupilDataSet);
        }
    }

    private void updateLiveGraph(float ratio) {
        if (pupilChart == null || pupilDataSet == null || lineData == null) return;

        long elapsed = System.currentTimeMillis() - startTime;
        float timeSeconds = elapsed / 1000f;

        lineData.addEntry(new Entry(timeSeconds, ratio), 0);
        lineData.notifyDataChanged();
        pupilChart.notifyDataSetChanged();
        pupilChart.setVisibleXRangeMaximum(8f);
        pupilChart.moveViewToX(lineData.getEntryCount());
    }

    private void setupHistoryRecyclerView() {
        recyclerViewScans = findViewById(R.id.recyclerViewScans);
        if (recyclerViewScans != null) {
            recyclerViewScans.setLayoutManager(new LinearLayoutManager(this));
            scanAdapter = new ScanRecordAdapter();
            recyclerViewScans.setAdapter(scanAdapter);

            AppDatabase.getInstance(this).scanRecordDao().getObservableRecordsByPatient("Patient-01")
                    .observe(this, records -> {
                        if (records != null) {
                            Log.d(TAG, "History observer triggered with " + records.size() + " records");
                            scanAdapter.setScanList(records);
                        }
                    });
        }
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
            SyncScheduler.scheduleSync(getApplicationContext());
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

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
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            long timestamp = imageProxy.getImageInfo().getTimestamp();
            visionBrain.detectFace(rotatedBitmap, timestamp);

            runOnUiThread(() -> {
                if (isGraphActive) {
                    if (visionBrain.latestRatio <= 0.0f) {
                        if (warningText != null) warningText.setVisibility(View.VISIBLE);
                    } else {
                        if (warningText != null) warningText.setVisibility(View.INVISIBLE);
                        updateLiveGraph(visionBrain.latestRatio);

                        if (visionBrain.triageEngine != null) {
                            visionBrain.triageEngine.validFrameCount++;
                        }
                    }
                } else {
                    if (warningText != null) warningText.setVisibility(View.INVISIBLE);
                }
            });

            imageProxy.close();
        });

        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
        cameraProvider.unbindAll();

        globalCamera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        globalCamera.getCameraControl().enableTorch(false);
    }

    public void blastScreenBrightness() {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 1.0f;
        getWindow().setAttributes(layout);
    }

    public void restoreScreenBrightness() {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = -1.0f; // Reset to system default
        getWindow().setAttributes(layout);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void switchCamera() {
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            lensFacing = CameraSelector.LENS_FACING_BACK;
        } else {
            lensFacing = CameraSelector.LENS_FACING_FRONT;
        }

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
}