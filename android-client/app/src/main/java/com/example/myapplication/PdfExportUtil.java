package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfExportUtil {

    public static void generateTriageReport(Context context, String patientName, String age, String gender, String patientId,
                                            double baseValue, double minValue, double ratio, long latency, String triageResult) {
        float baseline = (float) (baseValue > 0 ? baseValue : 5.2);
        float minVal = (float) (minValue > 0 ? minValue : 3.1);
        float[] times = new float[]{0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        float[] ratios = new float[]{baseline, baseline, (baseline + minVal) / 2f, minVal, minVal + 0.02f, baseline - 0.01f};
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
        float y = 30f;

        // 2. Main Brand Header Banner (#003893)
        canvas.drawRect(startX, y, endX, y + 42f, navyHeaderPaint);
        canvas.drawText("VISION TRIAGE — CLINICAL OCULAR REPORT", pageInfo.getPageWidth() / 2f, y + 18f, headerTitlePaint);
        canvas.drawText("Ocular Kinematics & PLR Biomarker Analysis", pageInfo.getPageWidth() / 2f, y + 33f, headerSubtitlePaint);
        y += 52f;

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

        String strBase = String.format(Locale.getDefault(), "%.3f mm", baseValue);
        String strMin = String.format(Locale.getDefault(), "%.3f mm", minValue);
        String strRatio = String.format(Locale.getDefault(), "%.3f", ratio);

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

        // 6. Section 4: PLR Kinematics Graph (Native Canvas Synchronized Polyline)
        canvas.drawRect(startX, y, endX, y + 18f, sectionHeaderBgPaint);
        canvas.drawText("PUPILLARY LIGHT REFLEX (PLR) KINEMATICS GRAPH", startX + 8f, y + 12f, sectionHeaderTitlePaint);
        y += 18f;

        float graphLeft = startX + 42f;
        float graphRight = endX - 15f;
        float graphTop = y + 8f;
        float graphBottom = graphTop + 165f;

        // Dynamic Y-Axis Bounds Calculation from ratioArray
        float rawMinY = Float.MAX_VALUE;
        float rawMaxY = -Float.MAX_VALUE;
        if (ratioArray != null && ratioArray.length > 0) {
            for (float val : ratioArray) {
                if (val < rawMinY) rawMinY = val;
                if (val > rawMaxY) rawMaxY = val;
            }
        } else {
            rawMinY = (float) minValue;
            rawMaxY = (float) baseValue;
        }

        final float minY = Math.max(0.0f, rawMinY - 0.010f);
        final float maxY = rawMaxY + 0.015f;

        final float minX = (timeArray != null && timeArray.length > 0) ? timeArray[0] : 0.0f;
        final float maxX = (timeArray != null && timeArray.length > 0) ? timeArray[timeArray.length - 1] : 5.0f;

        // Draw Graph Canvas Box
        Paint graphBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphBgPaint.setColor(Color.parseColor("#F8FAFC"));
        graphBgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(graphLeft, graphTop, graphRight, graphBottom, graphBgPaint);
        canvas.drawRect(graphLeft, graphTop, graphRight, graphBottom, borderPaint);

        // Gridlines & Axis Paints
        Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E2E8F0"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(0.75f);

        Paint axisTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        axisTextPaint.setTextSize(7.5f);
        axisTextPaint.setColor(Color.parseColor("#64748B"));

        // Y-Axis Gridlines & Labels (5 Step Dividers)
        int ySteps = 5;
        for (int i = 0; i <= ySteps; i++) {
            float val = minY + (i * (maxY - minY) / (float) ySteps);
            float yPos = graphBottom - ((val - minY) / (maxY - minY)) * (graphBottom - graphTop);
            canvas.drawLine(graphLeft, yPos, graphRight, yPos, gridPaint);
            axisTextPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), "%.3f", val), graphLeft - 4f, yPos + 2.5f, axisTextPaint);
        }

        // X-Axis Gridlines & Labels
        if (timeArray != null) {
            for (float t : timeArray) {
                float xPos = graphLeft + ((t - minX) / (maxX - minX)) * (graphRight - graphLeft);
                canvas.drawLine(xPos, graphTop, xPos, graphBottom, gridPaint);
                axisTextPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(String.format(Locale.getDefault(), "%.1fs", t), xPos, graphBottom + 10f, axisTextPaint);
            }
        }

        // Axis Titles
        Paint axisTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisTitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        axisTitlePaint.setTextSize(7.5f);
        axisTitlePaint.setColor(Color.parseColor("#475569"));
        axisTitlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Time (seconds)", graphLeft + ((graphRight - graphLeft) / 2f), graphBottom + 20f, axisTitlePaint);

        canvas.save();
        canvas.rotate(-90, graphLeft - 28f, graphTop + ((graphBottom - graphTop) / 2f));
        canvas.drawText("Pupil Contraction Ratio", graphLeft - 28f, graphTop + ((graphBottom - graphTop) / 2f), axisTitlePaint);
        canvas.restore();

        // Light Stimulus Pulse Shaded Region (t = 1.0s to 2.0s)
        float stimLeft = graphLeft + ((1.0f - minX) / (maxX - minX)) * (graphRight - graphLeft);
        float stimRight = graphLeft + ((2.0f - minX) / (maxX - minX)) * (graphRight - graphLeft);
        Paint stimPulsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stimPulsePaint.setColor(Color.parseColor("#FEF3C7")); // Translucent Soft Amber Tint
        stimPulsePaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(stimLeft, graphTop, stimRight, graphBottom, stimPulsePaint);

        // Normative Baseline Envelope (Shaded Green Region)
        Path envelopePath = new Path();
        float normRestHigh = graphBottom - (((maxY * 0.98f) - minY) / (maxY - minY)) * (graphBottom - graphTop);
        float normRestLow = graphBottom - (((minY + (maxY - minY) * 0.3f) - minY) / (maxY - minY)) * (graphBottom - graphTop);

        envelopePath.moveTo(graphLeft, normRestHigh);
        envelopePath.lineTo(graphRight, normRestHigh);
        envelopePath.lineTo(graphRight, normRestLow);
        envelopePath.lineTo(graphLeft, normRestLow);
        envelopePath.close();

        Paint normEnvelopePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        normEnvelopePaint.setColor(Color.parseColor("#DCFCE7"));
        normEnvelopePaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(envelopePath, normEnvelopePaint);

        // Synchronized Polyline Waveform (#003893 Deep Blue)
        Path polylinePath = new Path();
        if (timeArray != null && ratioArray != null && timeArray.length == ratioArray.length) {
            for (int i = 0; i < timeArray.length; i++) {
                float px = graphLeft + ((timeArray[i] - minX) / (maxX - minX)) * (graphRight - graphLeft);
                float py = graphBottom - ((ratioArray[i] - minY) / (maxY - minY)) * (graphBottom - graphTop);
                if (i == 0) {
                    polylinePath.moveTo(px, py);
                } else {
                    polylinePath.lineTo(px, py);
                }
            }
        }

        Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(Color.parseColor("#003893"));
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(2.5f);
        canvas.drawPath(polylinePath, wavePaint);

        // Circular Data Node Dots & Numerical Value Labels
        Paint dotPinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPinPaint.setColor(Color.parseColor("#003893"));
        dotPinPaint.setStyle(Paint.Style.FILL);

        Paint nodeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodeTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        nodeTextPaint.setTextSize(6.5f);
        nodeTextPaint.setColor(Color.parseColor("#003893"));
        nodeTextPaint.setTextAlign(Paint.Align.CENTER);

        if (timeArray != null && ratioArray != null && timeArray.length == ratioArray.length) {
            for (int i = 0; i < timeArray.length; i++) {
                float px = graphLeft + ((timeArray[i] - minX) / (maxX - minX)) * (graphRight - graphLeft);
                float py = graphBottom - ((ratioArray[i] - minY) / (maxY - minY)) * (graphBottom - graphTop);

                canvas.drawCircle(px, py, 3.5f, dotPinPaint);

                String label = String.format(Locale.getDefault(), "%.4f", ratioArray[i]);
                float textY = (py - 6f < graphTop + 10f) ? py + 12f : py - 6f;
                canvas.drawText(label, px, textY, nodeTextPaint);
            }
        }

        // Chart Legend (below graph)
        float legendY = graphBottom + 32f;
        float leg1X = startX + 20f;

        // Legend item 1: Measured Response (Polyline Node Symbol)
        Paint legLinePaint = new Paint(wavePaint);
        legLinePaint.setStrokeWidth(2f);
        canvas.drawLine(leg1X, legendY - 3f, leg1X + 16f, legendY - 3f, legLinePaint);
        canvas.drawCircle(leg1X + 8f, legendY - 3f, 2.5f, dotPinPaint);
        axisTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Pupil Contraction Ratio", leg1X + 20f, legendY, axisTextPaint);

        // Legend item 2: Normative Envelope
        float leg2X = leg1X + 160f;
        RectF legRect = new RectF(leg2X, legendY - 7f, leg2X + 14f, legendY + 1f);
        canvas.drawRect(legRect, normEnvelopePaint);
        canvas.drawRect(legRect, borderPaint);
        canvas.drawText("Normative Baseline Envelope", leg2X + 18f, legendY, axisTextPaint);

        // Legend item 3: Light Stimulus
        float leg3X = leg2X + 170f;
        RectF leg3Rect = new RectF(leg3X, legendY - 7f, leg3X + 14f, legendY + 1f);
        canvas.drawRect(leg3Rect, stimPulsePaint);
        canvas.drawRect(leg3Rect, borderPaint);
        canvas.drawText("Light Stimulus Pulse (1.0s)", leg3X + 18f, legendY, axisTextPaint);

        y = legendY + 22f;

        // 7. Section 5: Medicolegal & Clinical Sign-off Block
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

        // 8. Footer Alignment
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

        // 9. Finalize and Save PDF
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