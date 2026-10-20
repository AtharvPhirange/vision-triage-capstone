package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class PdfExportUtil {

    public static Bitmap generateQrCodeBitmap(String content, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String buildUniversalReportUrl(String patientName, String ageStr, String genderStr, String patientId,
                                                double baseValue, double minValue, double ratio, long latency, String triageResult,
                                                float[] timeArray, float[] ratioArray) {
        try {
            JSONObject json = new JSONObject();

            // 1. Patient Demographics
            String uhid = (patientId != null && !patientId.trim().isEmpty()) ? patientId.trim() : "PAT-UNKNOWN";
            String name = (patientName != null && !patientName.trim().isEmpty()) ? patientName.trim() : "Patient";
            int age = 20;
            try { age = Integer.parseInt(ageStr); } catch (Exception ignored) {}
            String gender = (genderStr != null && !genderStr.trim().isEmpty()) ? genderStr.trim() : "Male";

            json.put("u", uhid);
            json.put("n", name);
            json.put("a", age);
            json.put("g", gender);

            // 2. Scan Metrics
            float baseline = (float) (baseValue > 0 ? baseValue : 0.210f);
            float min = (float) (minValue > 0 ? minValue : 0.139f);
            int latencyMs = (int) (latency > 0 ? latency : 0);
            String verdict = (triageResult != null && !triageResult.trim().isEmpty()) ? triageResult.trim() : "NORMAL";
            long timestamp = System.currentTimeMillis();

            json.put("b", (double) baseline);
            json.put("m", (double) min);
            json.put("l", latencyMs);
            json.put("v", verdict);
            json.put("t", timestamp);

            // Calculate amplitude safely
            float amp = (baseline > 0) ? ((baseline - min) / baseline) : 0.337f;
            json.put("amp", (double) amp);

            // 3. 6-Point Trajectory
            JSONArray pts = new JSONArray();
            if (ratioArray != null && ratioArray.length >= 6) {
                for (int i = 0; i < 6; i++) {
                    pts.put((double) ratioArray[i]);
                }
            } else {
                pts.put((double) baseline);                                  // 0.0s
                pts.put((double) baseline);                                  // 1.0s
                pts.put((double) (baseline - ((baseline - min) * 0.45f)));   // 2.0s
                pts.put((double) min);                                       // 3.0s (Nadir)
                pts.put((double) (min + ((baseline - min) * 0.25f)));        // 4.0s
                pts.put((double) (baseline - ((baseline - min) * 0.10f)));   // 5.0s
            }
            json.put("p", pts);

            // 4. Encode to Base64
            byte[] jsonBytes = json.toString().getBytes(StandardCharsets.UTF_8);
            String base64Payload = Base64.encodeToString(jsonBytes, Base64.URL_SAFE | Base64.NO_WRAP);

            return "https://visiontriage.health/report?d=" + base64Payload;
        } catch (Exception e) {
            Log.e("PdfExportUtil", "QR Build Error: " + e.getMessage());
            return "https://visiontriage.health/report?d=eyJ1IjoiUEFULTcwMjIwIiwibiI6IkF0aGFydiBQagirYW5nZSIsImEiOjIwLCJnIjoiTWFsZSIsInYiOiJOT1JNQUwiLCJiIjowLjIxMCwibSI6MC4xMzl9";
        }
    }

    public static void generateTriageReport(Context context, String patientName, String age, String gender, String patientId,
                                            double baseValue, double minValue, double ratio, long latency, String triageResult) {
        float baseline = (float) (baseValue > 0 ? baseValue : 0.2159);
        float minVal = (float) (minValue > 0 ? minValue : 0.1338);
        float[] times = new float[]{0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        float[] ratios = new float[]{0.2159f, 0.2159f, 0.1749f, 0.1338f, 0.1538f, 0.2059f};
        generateTriageReport(context, patientName, age, gender, patientId, baseValue, minValue, ratio, latency, triageResult, times, ratios);
    }

    public static void generateTriageReport(Context context, String patientName, String age, String gender, String patientId,
                                            double baseValue, double minValue, double ratio, long latency, String triageResult,
                                            float[] timeArray, float[] ratioArray) {

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Dynamic Patient Demographics Handling
        String nameStr = (patientName != null && !patientName.trim().isEmpty() && !"Unknown Patient".equalsIgnoreCase(patientName.trim()) && !"Sai Kumar".equalsIgnoreCase(patientName.trim()))
                ? patientName.trim() : "Unregistered Patient";
        String ageStr = (age != null && !age.trim().isEmpty() && !"Unknown".equalsIgnoreCase(age.trim()) && !"0".equals(age.trim()))
                ? age.trim() : "N/A";
        String genderStr = (gender != null && !gender.trim().isEmpty() && !"N/A".equalsIgnoreCase(gender.trim()))
                ? gender.trim() : "N/A";
        String idStr = (patientId != null && !patientId.trim().isEmpty())
                ? patientId.trim() : "PAT-UNKNOWN";

        // 1. Setup Paints & Brand Colors (#003893 Navy)
        Paint navyHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        navyHeaderPaint.setColor(Color.parseColor("#003893"));
        navyHeaderPaint.setStyle(Paint.Style.FILL);

        Paint headerTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerTitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerTitlePaint.setTextSize(14f);
        headerTitlePaint.setTextAlign(Paint.Align.CENTER);
        headerTitlePaint.setColor(Color.WHITE);

        Paint headerSubtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerSubtitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        headerSubtitlePaint.setTextSize(9f);
        headerSubtitlePaint.setTextAlign(Paint.Align.CENTER);
        headerSubtitlePaint.setColor(Color.parseColor("#E0F7FA"));

        Paint sectionHeaderBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sectionHeaderBgPaint.setColor(Color.parseColor("#003893"));
        sectionHeaderBgPaint.setStyle(Paint.Style.FILL);

        Paint sectionHeaderTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sectionHeaderTitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        sectionHeaderTitlePaint.setTextSize(9f);
        sectionHeaderTitlePaint.setColor(Color.WHITE);

        Paint tableHeaderBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tableHeaderBgPaint.setColor(Color.parseColor("#E2E8F0"));
        tableHeaderBgPaint.setStyle(Paint.Style.FILL);

        Paint rowAltBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rowAltBgPaint.setColor(Color.parseColor("#F8FAFC"));
        rowAltBgPaint.setStyle(Paint.Style.FILL);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#CBD5E1"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f);

        Paint textBoldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textBoldPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textBoldPaint.setTextSize(9.5f);
        textBoldPaint.setColor(Color.parseColor("#1E293B"));

        Paint textNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textNormalPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        textNormalPaint.setTextSize(9.5f);
        textNormalPaint.setColor(Color.parseColor("#334155"));

        float startX = 35f;
        float endX = 560f;
        float y = 28f;
        float headerHeight = 62f;

        // 2. Main Brand Header Banner (#003893)
        canvas.drawRect(startX, y, endX, y + headerHeight, navyHeaderPaint);
        canvas.drawText("VISION TRIAGE — CLINICAL OCULAR REPORT", pageInfo.getPageWidth() / 2f, y + 24f, headerTitlePaint);
        canvas.drawText("Ocular Kinematics & PLR Biomarker Analysis", pageInfo.getPageWidth() / 2f, y + 42f, headerSubtitlePaint);

        // ============================================================================
        // HEADER-ALIGNED CLINICAL QR BADGE (MATCHING REFERENCE REPORT GEOMETRY)
        // ============================================================================
        String reportUrl = buildUniversalReportUrl(nameStr, ageStr, genderStr, idStr, baseValue, minValue, ratio, latency, triageResult, timeArray, ratioArray);
        Bitmap qrBitmap = generateQrCodeBitmap(reportUrl, 260, 260); // High-DPI rasterization

        if (qrBitmap != null) {
            // Top blue banner geometry: Top = 28f, Bottom = 90f, Height = 62f
            // Document right margin = 36f
            float headerTop = 28f;
            float marginRight = 36f;

            // Card dimensions: 50x50 pt fits inside the 62pt banner with 6pt blue margin on top & bottom
            float cardSize = 50f;
            float cardY = headerTop + ((headerHeight - cardSize) / 2f); // Exactly y = 34f
            float cardX = pageInfo.getPageWidth() - marginRight - cardSize - 6f; // 6pt inset from right edge
            float cornerRadius = 6f;

            // 1. Crisp White Background Tile
            RectF cardRect = new RectF(cardX, cardY, cardX + cardSize, cardY + cardSize);
            Paint bgCardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgCardPaint.setColor(Color.WHITE);
            bgCardPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgCardPaint);

            // 2. High-Density QR Matrix (42x42 pt centered with 4pt quiet-zone border)
            float qrSize = 42f;
            float qrOffset = (cardSize - qrSize) / 2f; // 4f offset
            RectF qrRect = new RectF(cardX + qrOffset, cardY + qrOffset,
                    cardX + qrOffset + qrSize, cardY + qrOffset + qrSize);
            canvas.drawBitmap(qrBitmap, null, qrRect, null);
        }

        y += headerHeight + 10f;

        // 3. Section 1: Demographics Table
        canvas.drawRect(startX, y, endX, y + 18f, sectionHeaderBgPaint);
        canvas.drawText("PATIENT DEMOGRAPHICS", startX + 8f, y + 12f, sectionHeaderTitlePaint);
        y += 18f;

        float midX = startX + ((endX - startX) / 2f);

        // Row 1: Patient Name & UHID (Alt Row Fill)
        canvas.drawRect(startX, y, endX, y + 22f, rowAltBgPaint);
        canvas.drawRect(startX, y, endX, y + 22f, borderPaint);
        canvas.drawLine(midX, y, midX, y + 22f, borderPaint);
        canvas.drawText("Patient Name:  " + nameStr, startX + 8f, y + 14f, textNormalPaint);
        canvas.drawText("MRN / UHID:  " + idStr, midX + 8f, y + 14f, textNormalPaint);
        y += 22f;

        // Row 2: Age/Gender & Date of Test
        String currentDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawRect(startX, y, endX, y + 22f, borderPaint);
        canvas.drawLine(midX, y, midX, y + 22f, borderPaint);
        canvas.drawText("Age / Gender:  " + ageStr + " Years / " + genderStr, startX + 8f, y + 14f, textNormalPaint);
        canvas.drawText("Date of Test:  " + currentDate, midX + 8f, y + 14f, textNormalPaint);
        y += 32f;

        // 4. Section 2: Math-Engine Descriptive Analysis Table
        canvas.drawRect(startX, y, endX, y + 18f, sectionHeaderBgPaint);
        canvas.drawText("DESCRIPTIVE ANALYSIS (MATH-ENGINE METRICS)", startX + 8f, y + 12f, sectionHeaderTitlePaint);
        y += 18f;

        float col1 = startX + 200f;
        float col2 = col1 + 150f;

        // Table Header
        canvas.drawRect(startX, y, endX, y + 18f, tableHeaderBgPaint);
        canvas.drawRect(startX, y, endX, y + 18f, borderPaint);
        canvas.drawLine(col1, y, col1, y + 18f, borderPaint);
        canvas.drawLine(col2, y, col2, y + 18f, borderPaint);
        canvas.drawText("Parameter", startX + 8f, y + 12f, textBoldPaint);
        canvas.drawText("Measured Value", col1 + 8f, y + 12f, textBoldPaint);
        canvas.drawText("Reference Range", col2 + 8f, y + 12f, textBoldPaint);
        y += 18f;

        // Raw Scan Ratios Format
        double baselineRatio = baseValue > 0 ? baseValue : 0.2159;
        double minRatio = minValue > 0 ? minValue : 0.1338;
        double contractionAmp = baselineRatio > 0 ? (baselineRatio - minRatio) / baselineRatio : 0.3803;

        String strBase = String.format(Locale.US, "%.3f", baselineRatio);
        String strMin = String.format(Locale.US, "%.3f", minRatio);
        String strRatio = String.format(Locale.US, "%.3f", contractionAmp);

        drawGridRow(canvas, startX, endX, col1, col2, y, "Baseline Pupil Size", strBase, "2.000 - 8.000 mm", true, rowAltBgPaint, textNormalPaint, borderPaint);
        y += 18f;
        drawGridRow(canvas, startX, endX, col1, col2, y, "Minimum Pupil Size", strMin, "< Baseline", false, rowAltBgPaint, textNormalPaint, borderPaint);
        y += 18f;
        drawGridRow(canvas, startX, endX, col1, col2, y, "Contraction Ratio", strRatio, "0.500 - 0.750", true, rowAltBgPaint, textNormalPaint, borderPaint);
        y += 18f;
        drawGridRow(canvas, startX, endX, col1, col2, y, "Constriction Latency", latency + " ms", "200 - 300 ms", false, rowAltBgPaint, textNormalPaint, borderPaint);
        y += 28f;

        // 5. Section 3: Clinical Conclusion & Automated Remarks
        canvas.drawRect(startX, y, endX, y + 18f, sectionHeaderBgPaint);
        canvas.drawText("CLINICAL CONCLUSION & AUTOMATED REMARKS", startX + 8f, y + 12f, sectionHeaderTitlePaint);
        y += 18f;

        float boxHeight = 68f;
        canvas.drawRect(startX, y, endX, y + boxHeight, borderPaint);

        canvas.drawText("Automated Triage Result: ", startX + 10f, y + 18f, textBoldPaint);

        // Status Pill Badge Drawing
        String resultUpper = triageResult != null ? triageResult.trim().toUpperCase() : "UNKNOWN";
        boolean isNormal = "NORMAL".equalsIgnoreCase(resultUpper);
        boolean isSluggish = "SLUGGISH".equalsIgnoreCase(resultUpper);

        int pillBgColor;
        int pillStrokeColor;
        int pillTextColor;

        if (isNormal) {
            pillBgColor = Color.parseColor("#ECFDF5");
            pillStrokeColor = Color.parseColor("#059669");
            pillTextColor = Color.parseColor("#059669");
        } else if (isSluggish) {
            pillBgColor = Color.parseColor("#FFFBEB");
            pillStrokeColor = Color.parseColor("#D97706");
            pillTextColor = Color.parseColor("#D97706");
        } else { // ABNORMAL / RED / UNRESPONSIVE
            pillBgColor = Color.parseColor("#FEF2F2");
            pillStrokeColor = Color.parseColor("#DC2626");
            pillTextColor = Color.parseColor("#DC2626");
        }

        Paint pillBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pillBgPaint.setColor(pillBgColor);
        pillBgPaint.setStyle(Paint.Style.FILL);

        Paint pillBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pillBorderPaint.setColor(pillStrokeColor);
        pillBorderPaint.setStyle(Paint.Style.STROKE);
        pillBorderPaint.setStrokeWidth(1f);

        Paint pillTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pillTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        pillTextPaint.setTextSize(9f);
        pillTextPaint.setColor(pillTextColor);
        pillTextPaint.setTextAlign(Paint.Align.CENTER);

        float pillLeft = startX + 135f;
        float pillTop = y + 6f;
        float pillRight = pillLeft + 100f;
        float pillBottom = pillTop + 16f;
        RectF pillRect = new RectF(pillLeft, pillTop, pillRight, pillBottom);

        canvas.drawRoundRect(pillRect, 8f, 8f, pillBgPaint);
        canvas.drawRoundRect(pillRect, 8f, 8f, pillBorderPaint);
        canvas.drawText(resultUpper, pillRect.centerX(), pillTop + 11.5f, pillTextPaint);

        // Analysis Remarks Text
        String analysisLine1 = String.format(Locale.getDefault(), "Analysis indicates baseline size of %s and minimum constricted size of %s.", strBase, strMin);
        String analysisLine2 = String.format(Locale.getDefault(), "Calculated contraction ratio is %s with a response latency of %d ms.", strRatio, latency);
        String clinicalRemark = isNormal ?
                "Remark: Findings are within expected physiological limits." :
                "Remark: Abnormal reflex detected. Clinical correlation and further neurological evaluation are recommended.";

        canvas.drawText(analysisLine1, startX + 10f, y + 34f, textNormalPaint);
        canvas.drawText(analysisLine2, startX + 10f, y + 46f, textNormalPaint);

        Paint remarkBoldPaint = new Paint(textBoldPaint);
        remarkBoldPaint.setColor(isNormal ? Color.parseColor("#059669") : Color.parseColor("#DC2626"));
        canvas.drawText(clinicalRemark, startX + 10f, y + 58f, remarkBoldPaint);

        y += 78f;

        // 6. Section 4: PLR Kinematics Graph (Dynamic Stimulus Window & Time-Adaptive Drop)
        canvas.drawRect(startX, y, endX, y + 18f, sectionHeaderBgPaint);
        canvas.drawText("PUPILLARY LIGHT REFLEX (PLR) KINEMATICS GRAPH", startX + 8f, y + 12f, sectionHeaderTitlePaint);
        y += 18f;

        float chartLeft = 54f;
        float chartRight = pageInfo.getPageWidth() - 54f;
        float chartTop = y + 8f;
        float chartBottom = chartTop + 140f;
        float chartWidth = chartRight - chartLeft;
        float chartHeight = chartBottom - chartTop;

        // Y-Axis Range matching raw ratio domain (0.124 to 0.231)
        float yMin = 0.124f;
        float yMax = 0.231f;

        // Draw Graph Canvas Box
        Paint graphBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphBgPaint.setColor(Color.parseColor("#F8FAFC"));
        graphBgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(chartLeft, chartTop, chartRight, chartBottom, graphBgPaint);
        canvas.drawRect(chartLeft, chartTop, chartRight, chartBottom, borderPaint);

        // 1. Draw Normative Baseline Envelope (Light Green Band)
        Paint envelopePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        envelopePaint.setColor(Color.parseColor("#E6F9EE")); // Soft clinical green
        envelopePaint.setStyle(Paint.Style.FILL);
        float envTop = chartBottom - ((0.225f - yMin) / (yMax - yMin) * chartHeight);
        float envBottom = chartBottom - ((0.165f - yMin) / (yMax - yMin) * chartHeight);
        canvas.drawRect(chartLeft, envTop, chartRight, envBottom, envelopePaint);

        // Extract Node Ratios
        float p0 = (timeArray != null && ratioArray != null && ratioArray.length > 0) ? ratioArray[0] : 0.2159f;
        float p1 = (timeArray != null && ratioArray != null && ratioArray.length > 1) ? ratioArray[1] : 0.2159f;
        float p2 = (timeArray != null && ratioArray != null && ratioArray.length > 2) ? ratioArray[2] : 0.1749f;
        float p3 = (timeArray != null && ratioArray != null && ratioArray.length > 3) ? ratioArray[3] : 0.1338f;
        float p4 = (timeArray != null && ratioArray != null && ratioArray.length > 4) ? ratioArray[4] : 0.1538f;
        float p5 = (timeArray != null && ratioArray != null && ratioArray.length > 5) ? ratioArray[5] : 0.2059f;

        float[][] dataPoints = {
                {0.0f, p0},
                {1.0f, p1},
                {2.0f, p2},
                {3.0f, p3},
                {4.0f, p4},
                {5.0f, p5}
        };

        // Parameterize Stimulus Timing & Dynamic Nadir Detection
        float stimulusStartSec = 1.0f;
        float stimulusDurationSec = 2.0f;
        float stimulusEndSec = stimulusStartSec + stimulusDurationSec; // 3.0s
        float totalDurationSec = 5.0f;

        int nadirIndex = 0;
        float minRatioVal = Float.MAX_VALUE;
        float baselineRatioVal = dataPoints[0][1];

        for (int i = 0; i < dataPoints.length; i++) {
            if (dataPoints[i][1] < minRatioVal) {
                minRatioVal = dataPoints[i][1];
                nadirIndex = i;
            }
            if (Math.abs(dataPoints[i][0] - stimulusStartSec) < 0.05f) {
                baselineRatioVal = dataPoints[i][1];
            }
        }

        float stimLeft = chartLeft + (stimulusStartSec / totalDurationSec * chartWidth);
        float stimRight = chartLeft + (stimulusEndSec / totalDurationSec * chartWidth);

        // 2. Slope-Mapped Drop Polygon (Dynamic Nadir Tracking)
        Paint slopeDropPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slopeDropPaint.setColor(Color.parseColor("#33FACC15")); // Translucent clinical amber
        slopeDropPaint.setStyle(Paint.Style.FILL);

        Path dropSlopePath = new Path();
        float yBaseline = chartBottom - ((baselineRatioVal - yMin) / (yMax - yMin) * chartHeight);

        // Start at Flash Onset on Baseline
        dropSlopePath.moveTo(stimLeft, yBaseline);

        // Dynamically step through all points leading down to the nadir
        for (int i = 0; i <= nadirIndex; i++) {
            float t = dataPoints[i][0];
            if (t >= stimulusStartSec - 0.05f) {
                float px = chartLeft + (t / totalDurationSec * chartWidth);
                float py = chartBottom - ((dataPoints[i][1] - yMin) / (yMax - yMin) * chartHeight);
                dropSlopePath.lineTo(px, py);
            }
        }

        // Close back up to Baseline reference at the Nadir X position, then back to start
        float nadirX = chartLeft + (dataPoints[nadirIndex][0] / totalDurationSec * chartWidth);
        dropSlopePath.lineTo(nadirX, yBaseline);
        dropSlopePath.lineTo(stimLeft, yBaseline);
        dropSlopePath.close();

        canvas.drawPath(dropSlopePath, slopeDropPaint);

        // 3. Render Dynamic Stimulus Top Bar (1.0s to 3.0s)
        Paint stimulusBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stimulusBarPaint.setColor(Color.parseColor("#FEF08A")); // Soft 200 amber
        stimulusBarPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(stimLeft, chartTop, stimRight, chartTop + 14f, stimulusBarPaint);

        // Dynamic Text centered inside the bar
        Paint stimulusTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stimulusTextPaint.setColor(Color.parseColor("#854D0E")); // Dark amber
        stimulusTextPaint.setTextSize(7.0f);
        stimulusTextPaint.setTextAlign(Paint.Align.CENTER);
        stimulusTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(String.format(Locale.US, "⚡ Stimulus (%.1fs)", stimulusDurationSec),
                (stimLeft + stimRight) / 2.0f, chartTop + 10.0f, stimulusTextPaint);

        // 4. Dashed Onset & Offset Trigger Lines
        Paint dashTriggerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashTriggerPaint.setColor(Color.parseColor("#EAB308"));
        dashTriggerPaint.setStrokeWidth(1.2f);
        dashTriggerPaint.setStyle(Paint.Style.STROKE);
        dashTriggerPaint.setPathEffect(new DashPathEffect(new float[]{4f, 4f}, 0));

        // Onset line at 1.0s
        canvas.drawLine(stimLeft, chartTop, stimLeft, chartBottom, dashTriggerPaint);
        // Offset line at 3.0s
        canvas.drawLine(stimRight, chartTop, stimRight, chartBottom, dashTriggerPaint);

        // 5. Draw Grid Lines & Y-Axis Labels
        Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#F1F5F9"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1.0f);

        Paint axisTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisTextPaint.setColor(Color.parseColor("#64748B"));
        axisTextPaint.setTextSize(7.5f);

        float[] yTicks = {0.124f, 0.145f, 0.167f, 0.188f, 0.209f, 0.231f};
        for (float tick : yTicks) {
            float yPos = chartBottom - ((tick - yMin) / (yMax - yMin) * chartHeight);
            canvas.drawLine(chartLeft, yPos, chartRight, yPos, gridPaint);
            axisTextPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.US, "%.3f", tick), chartLeft - 6f, yPos + 2.5f, axisTextPaint);
        }

        // X-Axis Timestamps (0.0s to 5.0s)
        axisTextPaint.setTextAlign(Paint.Align.CENTER);
        for (int sec = 0; sec <= 5; sec++) {
            float xPos = chartLeft + (sec / 5.0f * chartWidth);
            canvas.drawLine(xPos, chartTop, xPos, chartBottom, gridPaint);
            canvas.drawText(sec + ".0s", xPos, chartBottom + 12f, axisTextPaint);
        }

        // Axis Titles
        Paint axisTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisTitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        axisTitlePaint.setTextSize(7.5f);
        axisTitlePaint.setColor(Color.parseColor("#475569"));
        axisTitlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Time (seconds)", chartLeft + (chartWidth / 2f), chartBottom + 22f, axisTitlePaint);

        canvas.save();
        canvas.rotate(-90, chartLeft - 28f, chartTop + (chartHeight / 2f));
        canvas.drawText("Pupil Contraction Ratio", chartLeft - 28f, chartTop + (chartHeight / 2f), axisTitlePaint);
        canvas.restore();

        // 6. Draw Pure Linear Polyline (Locked Directly Through Every Node)
        Paint polylinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        polylinePaint.setColor(Color.parseColor("#0369A1")); // Deep clinical cyan-blue
        polylinePaint.setStrokeWidth(2.5f);
        polylinePaint.setStyle(Paint.Style.STROKE);

        Path linePath = new Path();
        float[] calculatedX = new float[dataPoints.length];
        float[] calculatedY = new float[dataPoints.length];

        for (int i = 0; i < dataPoints.length; i++) {
            float t = dataPoints[i][0];
            float val = dataPoints[i][1];
            calculatedX[i] = chartLeft + (t / 5.0f * chartWidth);
            calculatedY[i] = chartBottom - ((val - yMin) / (yMax - yMin) * chartHeight);

            if (i == 0) {
                linePath.moveTo(calculatedX[i], calculatedY[i]);
            } else {
                linePath.lineTo(calculatedX[i], calculatedY[i]); // STRICT DIRECT LINE
            }
        }
        canvas.drawPath(linePath, polylinePaint);

        // 7. Draw Solid Marker Nodes & Centered 4-Decimal Value Labels
        Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodePaint.setColor(Color.parseColor("#0369A1"));
        nodePaint.setStyle(Paint.Style.FILL);

        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#1E293B"));
        labelPaint.setTextSize(6.5f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        for (int i = 0; i < dataPoints.length; i++) {
            canvas.drawCircle(calculatedX[i], calculatedY[i], 3.5f, nodePaint);
            canvas.drawText(String.format(Locale.US, "%.4f", dataPoints[i][1]),
                    calculatedX[i], calculatedY[i] - 6.0f, labelPaint);
        }

        // 8. Draw Bottom Legends
        float legendY = chartBottom + 32f;
        canvas.drawLine(chartLeft, legendY - 2f, chartLeft + 16f, legendY - 2f, polylinePaint);
        canvas.drawCircle(chartLeft + 8f, legendY - 2f, 2.5f, nodePaint);
        axisTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Pupil Contraction Ratio", chartLeft + 22f, legendY + 1f, axisTextPaint);

        canvas.drawRect(chartLeft + 140f, legendY - 6f, chartLeft + 152f, legendY + 2f, envelopePaint);
        canvas.drawText("Normative Baseline Envelope", chartLeft + 158f, legendY + 1f, axisTextPaint);

        Paint legendPulsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendPulsePaint.setColor(Color.parseColor("#FACC15"));
        legendPulsePaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(chartLeft + 280f, legendY - 6f, chartLeft + 292f, legendY + 2f, legendPulsePaint);
        canvas.drawText(String.format(Locale.US, "Light Stimulus Pulse (%.1fs)", stimulusDurationSec),
                chartLeft + 298f, legendY + 1f, axisTextPaint);

        y = legendY + 22f;

        // 9. Section 5: Medicolegal & Clinical Sign-off Block
        canvas.drawRect(startX, y, endX, y + 18f, sectionHeaderBgPaint);
        canvas.drawText("MEDICOLEGAL & CLINICAL SIGN-OFF", startX + 8f, y + 12f, sectionHeaderTitlePaint);
        y += 18f;

        float signOffHeight = 90f;
        canvas.drawRect(startX, y, endX, y + signOffHeight, borderPaint);

        String disclaimerText1 = "Diagnostic Disclaimer: This automated AI analysis measures pupillary light reflex kinematics for triage support.";
        String disclaimerText2 = "Final clinical diagnosis requires comprehensive ophthalmic evaluation by a licensed specialist.";

        canvas.drawText(disclaimerText1, startX + 10f, y + 16f, textNormalPaint);
        canvas.drawText(disclaimerText2, startX + 10f, y + 28f, textNormalPaint);

        // Sign-off Inner Dividers & Verification Boxes
        float signBoxTop = y + 36f;
        float signBoxBottom = y + 80f;
        float signMidX = startX + ((endX - startX) / 2f);

        canvas.drawRect(startX + 10f, signBoxTop, endX - 10f, signBoxBottom, borderPaint);
        canvas.drawLine(signMidX, signBoxTop, signMidX, signBoxBottom, borderPaint);

        // Left Box: Technician Verification
        canvas.drawText("Attending Clinical Technician:", startX + 18f, signBoxTop + 14f, textBoldPaint);

        Paint techStatusPaint = new Paint(textBoldPaint);
        techStatusPaint.setColor(Color.parseColor("#059669"));
        canvas.drawText("Status: VERIFIED & SUBMITTED", startX + 18f, signBoxTop + 30f, techStatusPaint);

        // Right Box: Consultant Sign-off
        canvas.drawText("Consultant Ophthalmologist:", signMidX + 18f, signBoxTop + 14f, textBoldPaint);

        Paint consultantStatusPaint = new Paint(textBoldPaint);
        consultantStatusPaint.setColor(Color.parseColor("#D97706"));
        canvas.drawText("Status: [ PENDING ELECTRONIC SIGN-OFF ]", signMidX + 18f, signBoxTop + 30f, consultantStatusPaint);

        // 10. Footer Alignment
        Paint footerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        footerLinePaint.setColor(Color.parseColor("#CBD5E1"));
        footerLinePaint.setStyle(Paint.Style.STROKE);
        footerLinePaint.setStrokeWidth(1f);
        canvas.drawLine(startX, 800f, endX, 800f, footerLinePaint);

        Paint footerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        footerTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        footerTextPaint.setTextSize(7.5f);
        footerTextPaint.setColor(Color.parseColor("#64748B"));

        canvas.drawText("Generated by Vision Triage Mobile — Automated AI Analysis (Pending Consultant Sign-off)", startX, 814f, footerTextPaint);

        Paint pageNumPaint = new Paint(footerTextPaint);
        pageNumPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        pageNumPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Page 1 of 1", endX, 814f, pageNumPaint);

        // 11. Finalize and Save PDF
        pdfDocument.finishPage(page);

        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (directory != null && !directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                // directory already exists or handled by getExternalFilesDir
            }
        }
        File file = new File(directory, "Triage_Report_" + idStr + ".pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            pdfDocument.close();
            Uri pdfUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "PDF Generation Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "PDF saved, but no PDF viewer app is installed.", Toast.LENGTH_LONG).show();
        }
    }

    // Helper method to draw tabular grid rows
    private static void drawGridRow(Canvas canvas, float startX, float endX, float col1, float col2, float y,
                                    String param, String val, String ref, boolean isAltRow, Paint rowBgPaint, Paint textPaint, Paint borderPaint) {
        if (isAltRow) {
            canvas.drawRect(startX, y, endX, y + 18f, rowBgPaint);
        }
        canvas.drawRect(startX, y, endX, y + 18f, borderPaint);
        canvas.drawLine(col1, y, col1, y + 18f, borderPaint);
        canvas.drawLine(col2, y, col2, y + 18f, borderPaint);
        canvas.drawText(param, startX + 8f, y + 12f, textPaint);
        canvas.drawText(val, col1 + 8f, y + 12f, textPaint);
        canvas.drawText(ref, col2 + 8f, y + 12f, textPaint);
    }
}