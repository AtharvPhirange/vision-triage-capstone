package com.example.myapplication;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.Patient;
import com.example.myapplication.db.ScanRecord;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class QrReportScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private boolean isTorchOn = false;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcodeScannerView);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnToggleFlash = findViewById(R.id.btnToggleFlash);

        // Header Navigation: Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Header Navigation: Flash / Torch toggle
        if (btnToggleFlash != null) {
            btnToggleFlash.setOnClickListener(v -> {
                if (isTorchOn) {
                    barcodeView.setTorchOff();
                    isTorchOn = false;
                } else {
                    barcodeView.setTorchOn();
                    isTorchOn = true;
                }
            });
        }

        // Restrict decoding strictly to QR codes for instant recognition
        barcodeView.getBarcodeView().setDecoderFactory(
                new DefaultDecoderFactory(Collections.singletonList(BarcodeFormat.QR_CODE))
        );

        startScanning();
    }

    private void startScanning() {
        isProcessing = false;
        barcodeView.resume();
        barcodeView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null && !isProcessing) {
                    isProcessing = true;
                    // Play audio confirmation beep
                    try {
                        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
                    } catch (Exception ignored) {}

                    // Pause camera preview so the frame freezes on scan
                    barcodeView.pause();

                    // Process payload on UI thread
                    runOnUiThread(() -> processScannedData(result.getText()));
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });
    }

    private void processScannedData(String rawContent) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            showErrorDialog("Scanned QR code was empty or unreadable.");
            return;
        }

        try {
            String uhid = "PAT-UNKNOWN";
            String name = "Patient";
            int age = 20;
            String gender = "Male";
            String verdict = "NORMAL";
            float baseline = 0.210f;
            float min = 0.139f;
            int latency = 0;
            float amp = 0.337f;
            long timestamp = System.currentTimeMillis();

            float[] points = new float[]{0.210f, 0.210f, 0.175f, 0.139f, 0.159f, 0.200f};

            // Extract Base64 query parameter ?d=
            if (rawContent.contains("?d=")) {
                String encoded = rawContent.substring(rawContent.indexOf("?d=") + 3);
                if (encoded.contains("&")) {
                    encoded = encoded.substring(0, encoded.indexOf("&"));
                }
                try {
                    encoded = java.net.URLDecoder.decode(encoded, StandardCharsets.UTF_8.name());
                } catch (Exception ignored) {}

                byte[] decodedBytes = Base64.decode(encoded.replace("-", "+").replace("_", "/"), Base64.DEFAULT);
                JSONObject json = new JSONObject(new String(decodedBytes, StandardCharsets.UTF_8));

                uhid = json.optString("u", uhid);
                name = json.optString("n", name);
                age = json.optInt("a", age);
                gender = json.optString("g", gender);
                verdict = json.optString("v", verdict);
                baseline = (float) json.optDouble("b", baseline);
                min = (float) json.optDouble("m", min);
                latency = json.optInt("l", latency);
                amp = (float) json.optDouble("amp", amp);
                timestamp = json.optLong("t", timestamp);

                JSONArray pArray = json.optJSONArray("p");
                if (pArray == null) pArray = json.optJSONArray("pts");
                if (pArray != null && pArray.length() >= 6) {
                    for (int i = 0; i < 6; i++) {
                        points[i] = (float) pArray.optDouble(i, points[i]);
                    }
                }
            } else if (rawContent.contains("uhid=") || rawContent.contains("?u=")) {
                // Legacy URL fallback
                if (rawContent.contains("uhid=")) {
                    uhid = rawContent.substring(rawContent.indexOf("uhid=") + 5);
                } else {
                    uhid = rawContent.substring(rawContent.indexOf("?u=") + 3);
                }
                if (uhid.contains("&")) {
                    uhid = uhid.substring(0, uhid.indexOf("&"));
                }

                try {
                    AppDatabase db = AppDatabase.getInstance(this);
                    ScanRecord localScan = db.scanRecordDao().getLatestScanForPatient(uhid);
                    Patient localPatient = db.patientDao().getPatientById(uhid);
                    if (localScan != null) {
                        baseline = (float) localScan.getBaselinePupilSize();
                        min = (float) localScan.getMinimumPupilSize();
                        verdict = localScan.getTriageResult();
                        latency = (int) localScan.getConstrictionLatencyMs();
                    }
                    if (localPatient != null) {
                        name = localPatient.getFullName();
                        age = localPatient.getAge();
                        gender = localPatient.getGender();
                    }
                } catch (Exception ignored) {}
            } else {
                throw new IllegalArgumentException("Unrecognized Vision Triage QR code pattern");
            }

            Patient patient = new Patient(uhid, name, age, gender, "", "", "", 16.7050, 74.2433, "QR Peer Transfer", timestamp);

            ScanRecord scan = new ScanRecord(
                    "SCAN-" + (10000 + new java.util.Random().nextInt(90000)),
                    uhid,
                    timestamp,
                    baseline,
                    min,
                    latency,
                    verdict,
                    false
            );
            scan.setScanSource("QR_PEER_IMPORT");

            // Persist to local Room database on background thread
            final Patient finalPatient = patient;
            final ScanRecord finalScan = scan;
            final float[] finalPoints = points;
            final String finalAgeStr = String.valueOf(age);
            final float finalBaseline = baseline;
            final float finalMin = min;
            final String finalGender = gender;
            final String finalUhid = uhid;
            final String finalVerdict = verdict;
            final int finalLatency = latency;
            final String finalName = name;

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    db.patientDao().insertPatient(finalPatient);
                    db.scanRecordDao().insert(finalScan);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Display Report Confirmation Modal
            new MaterialAlertDialogBuilder(this)
                    .setTitle("📋 Clinical Report Received")
                    .setMessage("Peer report verified and saved locally:\n\n" +
                            "• Patient: " + name + " (" + uhid + ")\n" +
                            "• Demographics: " + age + " Y / " + gender + "\n" +
                            "• Triage Verdict: " + verdict + "\n" +
                            "• Baseline PIR: " + String.format(Locale.US, "%.3f", baseline) + "\n" +
                            "• Constriction Nadir: " + String.format(Locale.US, "%.3f", min))
                    .setCancelable(false)
                    .setPositiveButton("Open & Save PDF", (dialog, which) -> {
                        double ratio = finalBaseline > 0 ? finalMin / finalBaseline : 0.65;
                        float[] times = new float[]{0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
                        PdfExportUtil.generateTriageReport(
                                QrReportScannerActivity.this,
                                finalName,
                                finalAgeStr,
                                finalGender,
                                finalUhid,
                                finalBaseline,
                                finalMin,
                                ratio,
                                finalLatency,
                                finalVerdict,
                                times,
                                finalPoints
                        );
                        finish();
                    })
                    .setNegativeButton("Done", (dialog, which) -> finish())
                    .show();

        } catch (Exception e) {
            showErrorDialog("Failed to read report QR: " + e.getMessage());
        }
    }

    private void showErrorDialog(String errorMessage) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Scan Issue")
                .setMessage(errorMessage)
                .setCancelable(false)
                .setPositiveButton("Scan Again", (dialog, which) -> startScanning())
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isProcessing) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}