package com.example.myapplication;

import android.util.Size;
import android.view.View;
import android.view.WindowManager;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.TextView;
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
import com.github.mikephil.charting.components.YAxis;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import android.os.Handler;
import android.os.Looper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import android.graphics.Color;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public Camera globalCamera;
    private TextView diagnosisText;
    private LineChart pupilChart;
    private LineDataSet pupilDataSet;
    private LineData lineData;
    private long startTime = 0;
    private PreviewView viewFinder;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private VisionBrain visionBrain;

    public int lensFacing = CameraSelector.LENS_FACING_FRONT;
    public ProcessCameraProvider globalCameraProvider;

    public boolean isGraphActive = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
                }
            });

    private TriageEngine triageEngine = new TriageEngine();
    private float latestRatio = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewFinder = findViewById(R.id.viewFinder);
        diagnosisText = findViewById(R.id.diagnosisText);

        android.widget.Button switchBtn = findViewById(R.id.flipCameraButton);
        switchBtn.setOnClickListener(v -> switchCamera());

        // Link the new Start button
        android.widget.Button startBtn = findViewById(R.id.startScanButton);
        startBtn.setOnClickListener(v -> startTriageTest());

        visionBrain = new VisionBrain();
        visionBrain.initializeAI(this);

        setupChart();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    public void startTriageTest() {
        android.widget.Button startBtn = findViewById(R.id.startScanButton);
        startBtn.setEnabled(false);
        startBtn.setAlpha(0.5f);

        diagnosisText.setVisibility(View.INVISIBLE);

        pupilDataSet.clear();
        lineData.notifyDataChanged();
        pupilChart.notifyDataSetChanged();
        pupilChart.invalidate();

        startTime = System.currentTimeMillis();
        isGraphActive = true;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            visionBrain.triageEngine.startTest(visionBrain.latestRatio);

            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                blastScreenBrightness();
            } else if (globalCamera != null) {
                globalCamera.getCameraControl().enableTorch(true);
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {

                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    restoreScreenBrightness();
                } else if (globalCamera != null) {
                    globalCamera.getCameraControl().enableTorch(false);
                }

                float baseline = visionBrain.triageEngine.baselineRatio;
                float minRatio = visionBrain.triageEngine.minConstrictedRatio;

                String result = visionBrain.triageEngine.getFinalDiagnosis(baseline, minRatio);
                diagnosisText.setText(result);

                isGraphActive = false;

                if (result.equals("NORMAL")) {
                    diagnosisText.setTextColor(android.graphics.Color.parseColor("#00FF00"));
                } else if (result.equals("SLUGGISH")) {
                    diagnosisText.setTextColor(android.graphics.Color.parseColor("#FFFF00"));
                } else {
                    diagnosisText.setTextColor(android.graphics.Color.parseColor("#FF0000"));
                }

                diagnosisText.setVisibility(View.VISIBLE);

                startBtn.setEnabled(true);
                startBtn.setAlpha(1.0f);

            }, 3000);
        }, 5000);
    }
    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                globalCameraProvider = cameraProvider;
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
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
                    updateLiveGraph(visionBrain.latestRatio);
                }
            });

            imageProxy.close();
        });

        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        cameraProvider.unbindAll();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

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
}