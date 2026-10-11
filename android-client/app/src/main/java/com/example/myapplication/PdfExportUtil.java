package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // 1. Setup Paints for Grid and Typography
        Paint titlePaint = new Paint();
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(18f);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(Color.BLACK);

        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(Color.parseColor("#E0E0E0"));
        headerBgPaint.setStyle(Paint.Style.FILL);

        Paint tableBorderPaint = new Paint();
        tableBorderPaint.setColor(Color.BLACK);
        tableBorderPaint.setStyle(Paint.Style.STROKE);
        tableBorderPaint.setStrokeWidth(1f);

        Paint textBoldPaint = new Paint();
        textBoldPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textBoldPaint.setTextSize(10f);
        textBoldPaint.setColor(Color.BLACK);

        Paint textNormalPaint = new Paint();
        textNormalPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        textNormalPaint.setTextSize(10f);
        textNormalPaint.setColor(Color.BLACK);

        float startX = 40f;
        float endX = 555f;
        float y = 50f;

        // 2. Draw Main Hospital Title
        canvas.drawText("VISION TRIAGE - NEURO-OPHTHALMOLOGY REPORT", pageInfo.getPageWidth() / 2f, y, titlePaint);
        y += 25;

        // 3. Section 1: Demographics Table
        canvas.drawRect(startX, y, endX, y + 20, headerBgPaint);
        canvas.drawRect(startX, y, endX, y + 20, tableBorderPaint);
        canvas.drawText("PATIENT DEMOGRAPHICS", startX + 180, y + 14, textBoldPaint);
        y += 20;

        float midX = startX + ((endX - startX) / 2f);
        canvas.drawRect(startX, y, endX, y + 25, tableBorderPaint);
        canvas.drawLine(midX, y, midX, y + 25, tableBorderPaint);
        canvas.drawText("Patient Name:  " + patientName, startX + 5, y + 16, textNormalPaint);
        canvas.drawText("MRNo./UHID:  " + patientId, midX + 5, y + 16, textNormalPaint);
        y += 25;

        String currentDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawRect(startX, y, endX, y + 25, tableBorderPaint);
        canvas.drawLine(midX, y, midX, y + 25, tableBorderPaint);
        // Updated label to just "Age:" and appended " Years" to the value
        canvas.drawText("Age:  " + age + " Years", startX + 5, y + 16, textNormalPaint);
        canvas.drawText("Date of Test:  " + currentDate, midX + 5, y + 16, textNormalPaint);
        y += 40;

        // 4. Section 2: Math-Engine Descriptive Analysis
        canvas.drawRect(startX, y, endX, y + 20, headerBgPaint);
        canvas.drawRect(startX, y, endX, y + 20, tableBorderPaint);
        canvas.drawText("DESCRIPTIVE ANALYSIS (MATH-ENGINE METRICS)", startX + 130, y + 14, textBoldPaint);
        y += 20;

        float col1 = startX + 200;
        float col2 = col1 + 150;
        canvas.drawRect(startX, y, endX, y + 20, tableBorderPaint);
        canvas.drawLine(col1, y, col1, y + 20, tableBorderPaint);
        canvas.drawLine(col2, y, col2, y + 20, tableBorderPaint);
        canvas.drawText("Parameter", startX + 5, y + 14, textBoldPaint);
        canvas.drawText("Measured Value", col1 + 5, y + 14, textBoldPaint);
        canvas.drawText("Reference Range", col2 + 5, y + 14, textBoldPaint);
        y += 20;

        String strBase = String.format(Locale.getDefault(), "%.3f mm", baseValue);
        String strMin = String.format(Locale.getDefault(), "%.3f mm", minValue);
        String strRatio = String.format(Locale.getDefault(), "%.3f", ratio);

        drawGridRow(canvas, startX, endX, col1, col2, y, "Baseline Pupil Size", strBase, "2.000 - 8.000 mm", textNormalPaint, tableBorderPaint);
        y += 20;
        drawGridRow(canvas, startX, endX, col1, col2, y, "Minimum Pupil Size", strMin, "< Baseline", textNormalPaint, tableBorderPaint);
        y += 20;
        drawGridRow(canvas, startX, endX, col1, col2, y, "Contraction Ratio", strRatio, "0.500 - 0.750", textNormalPaint, tableBorderPaint);
        y += 20;
        drawGridRow(canvas, startX, endX, col1, col2, y, "Constriction Latency", latency + " ms", "200 - 300 ms", textNormalPaint, tableBorderPaint);
        y += 40;

        // 5. Section 3: Clinical Conclusion & Automated Remarks
        canvas.drawRect(startX, y, endX, y + 20, headerBgPaint);
        canvas.drawRect(startX, y, endX, y + 20, tableBorderPaint);
        canvas.drawText("CLINICAL CONCLUSION & AUTOMATED REMARKS", startX + 120, y + 14, textBoldPaint);
        y += 20;

        float boxHeight = 120f;
        canvas.drawRect(startX, y, endX, y + boxHeight, tableBorderPaint);

        canvas.drawText("Automated Triage Result: ", startX + 10, y + 20, textBoldPaint);

        Paint resultColorPaint = new Paint(textBoldPaint);
        resultColorPaint.setTextSize(12f);
        boolean isNormal = "NORMAL".equalsIgnoreCase(triageResult);
        if (isNormal) {
            resultColorPaint.setColor(Color.parseColor("#388E3C")); // Green
        } else {
            resultColorPaint.setColor(Color.parseColor("#D32F2F")); // Red
        }
        canvas.drawText(triageResult != null ? triageResult.toUpperCase() : "UNKNOWN", startX + 140, y + 20, resultColorPaint);

        // Generate Detailed Clinical Summary
        String analysisLine1 = String.format(Locale.getDefault(), "Analysis indicates a baseline size of %s and a minimum constricted size of %s.", strBase, strMin);
        String analysisLine2 = String.format(Locale.getDefault(), "The calculated contraction ratio is %s with a response latency of %d ms.", strRatio, latency);
        String analysisLine3 = String.format(Locale.getDefault(), "Based on these parameters, the autonomic reflex response is classified as %s.", triageResult != null ? triageResult.toUpperCase() : "UNKNOWN");

        String clinicalRemark = isNormal ?
                "Remark: Findings are within expected physiological limits." :
                "Remark: Abnormal reflex detected. Clinical correlation and further neurological evaluation are recommended.";

        Paint remarkBoldPaint = new Paint(textBoldPaint);
        remarkBoldPaint.setColor(Color.DKGRAY);

        canvas.drawText(analysisLine1, startX + 10, y + 50, textNormalPaint);
        canvas.drawText(analysisLine2, startX + 10, y + 70, textNormalPaint);
        canvas.drawText(analysisLine3, startX + 10, y + 90, textNormalPaint);
        canvas.drawText(clinicalRemark, startX + 10, y + 110, remarkBoldPaint);

        // Footer
        textNormalPaint.setTextSize(8f);
        canvas.drawText("Generated by Vision Triage Mobile Application (Not for Medicolegal Purposes)", startX, 800, textNormalPaint);

        // 6. Finalize and Save
        pdfDocument.finishPage(page);

        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (directory != null && !directory.exists()) directory.mkdirs();
        File file = new File(directory, "Triage_Report_" + patientId + ".pdf");

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
                                    String param, String val, String ref, Paint textPaint, Paint borderPaint) {
        canvas.drawRect(startX, y, endX, y + 20, borderPaint);
        canvas.drawLine(col1, y, col1, y + 20, borderPaint);
        canvas.drawLine(col2, y, col2, y + 20, borderPaint);
        canvas.drawText(param, startX + 5, y + 14, textPaint);
        canvas.drawText(val, col1 + 5, y + 14, textPaint);
        canvas.drawText(ref, col2 + 5, y + 14, textPaint);
    }
}