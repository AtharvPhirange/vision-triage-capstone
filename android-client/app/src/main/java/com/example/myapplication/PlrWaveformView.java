package com.example.myapplication;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import androidx.annotation.Nullable;
import java.util.Locale;

public class PlrWaveformView extends View {

    private Paint gridPaint;
    private Paint linePaint;
    private Paint fillPaint;
    private Paint dotPaint;
    private Paint flashLinePaint;
    private Paint labelPaint;
    private Paint phasePaint;
    private Paint scrubberPaint;
    private Paint tooltipBgPaint;
    private Paint tooltipTextPaint;

    private float baselineMm = 5.20f;
    private float minMm = 3.10f;
    private float animatedProgress = 1.0f; // 0.0 to 1.0
    private ValueAnimator playbackAnimator;

    private float touchX = -1f;
    private boolean isTouching = false;

    public PlrWaveformView(Context context) {
        super(context);
        init();
    }

    public PlrWaveformView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E2E8F0"));
        gridPaint.setStrokeWidth(1.5f);
        gridPaint.setStyle(Paint.Style.STROKE);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#0284C7"));
        linePaint.setStrokeWidth(4.5f);
        linePaint.setStyle(Paint.Style.STROKE);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.parseColor("#0284C7"));
        dotPaint.setStyle(Paint.Style.FILL);

        flashLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flashLinePaint.setColor(Color.parseColor("#D97706"));
        flashLinePaint.setStrokeWidth(2f);
        flashLinePaint.setStyle(Paint.Style.STROKE);
        flashLinePaint.setPathEffect(new DashPathEffect(new float[]{8f, 6f}, 0));

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#64748B"));
        labelPaint.setTextSize(22f);

        phasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        phasePaint.setStyle(Paint.Style.FILL);

        scrubberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scrubberPaint.setColor(Color.parseColor("#0F172A"));
        scrubberPaint.setStrokeWidth(2.5f);

        tooltipBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipBgPaint.setColor(Color.parseColor("#0F172A"));
        tooltipBgPaint.setStyle(Paint.Style.FILL);

        tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipTextPaint.setColor(Color.WHITE);
        tooltipTextPaint.setTextSize(22f);
    }

    public void setWaveformMetrics(float baseMmVal, float minMmVal) {
        // Convert normalized ratios < 1.0 to physiological mm (e.g. 0.15 * 28 = 4.20 mm)
        float baseConverted = baseMmVal <= 1.0f ? baseMmVal * 28.0f : baseMmVal;
        float minConverted = minMmVal <= 1.0f ? minMmVal * 28.0f : minMmVal;

        this.baselineMm = Math.max(2.0f, Math.min(8.0f, baseConverted));
        this.minMm = Math.max(1.0f, Math.min(this.baselineMm - 0.4f, minConverted));
        invalidate();
    }

    public void startPlaybackAnimation() {
        if (playbackAnimator != null && playbackAnimator.isRunning()) {
            playbackAnimator.cancel();
        }
        playbackAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        playbackAnimator.setDuration(5000); // 5.0 seconds playback
        playbackAnimator.setInterpolator(new LinearInterpolator());
        playbackAnimator.addUpdateListener(animation -> {
            animatedProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        playbackAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                touchX = event.getX();
                isTouching = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouching = false;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float density = getResources().getDisplayMetrics().density;
        float padLeft = 58f * density;
        float padRight = 24f * density;
        float padTop = 32f * density;
        float padBottom = 32f * density;

        float chartLeft = padLeft;
        float chartRight = getWidth() - padRight;
        float chartTop = padTop;
        float chartBottom = getHeight() - padBottom;

        float chartWidth = chartRight - chartLeft;
        float chartHeight = chartBottom - chartTop;

        if (chartWidth <= 0 || chartHeight <= 0) return;

        // Auto-scale Y-axis range with 35% top/bottom vertical breathing room
        float rawMin = this.minMm;
        float rawMax = this.baselineMm;
        float span = Math.max(0.6f, rawMax - rawMin);
        float yMinScale = Math.max(0.5f, rawMin - (span * 0.35f));
        float yMaxScale = rawMax + (span * 0.35f);

        // Lock Canvas Clipping inside grid plane
        int saveCount = canvas.save();
        canvas.clipRect(chartLeft, chartTop, chartRight, chartBottom);

        // 3 Shaded Clinical Phase Backgrounds across 5 seconds
        float xFlash = chartLeft + (chartWidth / 5.0f) * 1.0f; // 1.0s
        float xMin = chartLeft + (chartWidth / 5.0f) * 2.5f;   // 2.5s

        // Phase 1: Baseline (0s - 1s)
        phasePaint.setColor(Color.parseColor("#080284C7"));
        canvas.drawRect(chartLeft, chartTop, xFlash, chartBottom, phasePaint);

        // Phase 2: Constriction (1s - 2.5s)
        phasePaint.setColor(Color.parseColor("#12FEF3C7"));
        canvas.drawRect(xFlash, chartTop, xMin, chartBottom, phasePaint);

        // Phase 3: Recovery (2.5s - 5s)
        phasePaint.setColor(Color.parseColor("#08059669"));
        canvas.drawRect(xMin, chartTop, chartRight, chartBottom, phasePaint);

        // Flash Trigger Vertical Line (1.0s)
        canvas.drawLine(xFlash, chartTop, xFlash, chartBottom, flashLinePaint);

        // Compute Curve Y Positions
        float yBaseline = valueToY(baselineMm, yMinScale, yMaxScale, chartTop, chartBottom);
        float yMin = valueToY(minMm, yMinScale, yMaxScale, chartTop, chartBottom);
        float yEnd = valueToY(baselineMm - (baselineMm - minMm) * 0.35f, yMinScale, yMaxScale, chartTop, chartBottom);

        // Path
        Path path = new Path();
        path.moveTo(chartLeft, yBaseline);
        path.lineTo(xFlash, yBaseline);
        path.cubicTo(xFlash + chartWidth * 0.08f, yBaseline, xMin - chartWidth * 0.08f, yMin, xMin, yMin);
        path.cubicTo(xMin + chartWidth * 0.2f, yMin, chartRight - chartWidth * 0.1f, yEnd, chartRight, yEnd);

        // Fill Path
        Path fillPath = new Path(path);
        fillPath.lineTo(chartRight, chartBottom);
        fillPath.lineTo(chartLeft, chartBottom);
        fillPath.close();

        fillPaint.setShader(new LinearGradient(0, chartTop, 0, chartBottom,
                Color.parseColor("#440284C7"), Color.parseColor("#000284C7"), Shader.TileMode.CLAMP));
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(path, linePaint);

        canvas.restoreToCount(saveCount);

        // Draw Canvas Outline Box
        canvas.drawRect(chartLeft, chartTop, chartRight, chartBottom, gridPaint);

        // Y-Axis mm Scale Labels (Right-aligned inside padLeft)
        int ySteps = 4;
        labelPaint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= ySteps; i++) {
            float mm = yMinScale + (yMaxScale - yMinScale) * (i / (float) ySteps);
            float y = chartBottom - (chartHeight / ySteps) * i;
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);
            canvas.drawText(String.format(Locale.getDefault(), "%.1f mm", mm), chartLeft - 8f, y + 6f, labelPaint);
        }

        // X-Axis Timeline (0s to 5s)
        int xSteps = 5;
        labelPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i <= xSteps; i++) {
            float x = chartLeft + (chartWidth / xSteps) * i;
            canvas.drawLine(x, chartTop, x, chartBottom, gridPaint);
            canvas.drawText(i + "s", x, chartBottom + 26f, labelPaint);
        }

        labelPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Flash (1.0s)", xFlash + 8f, chartTop + 22f, labelPaint);

        // Active Scrubber Progress
        float activeX = isTouching ? Math.max(chartLeft, Math.min(chartRight, touchX)) : (chartLeft + animatedProgress * chartWidth);
        float activeT = (activeX - chartLeft) / chartWidth * 5.0f; // in seconds
        float activeY;

        if (activeX <= xFlash) {
            activeY = yBaseline;
        } else if (activeX <= xMin) {
            float t = (activeX - xFlash) / (xMin - xFlash);
            activeY = yBaseline + t * (yMin - yBaseline);
        } else {
            float t = (activeX - xMin) / (chartRight - xMin);
            activeY = yMin + t * (yEnd - yMin);
        }

        // Draw Scrubber Line & Dot
        canvas.drawLine(activeX, chartTop, activeX, chartBottom, scrubberPaint);
        canvas.drawCircle(activeX, activeY, 10f, dotPaint);

        // Draw Scrubber Tooltip Pill (Clamped inside chart left/right)
        float activeMm = yMaxScale - ((activeY - chartTop) / chartHeight) * (yMaxScale - yMinScale);
        String tooltipText = String.format(Locale.getDefault(), "%.1fs | %.2f mm", activeT, activeMm);
        float tooltipW = tooltipTextPaint.measureText(tooltipText) + 24f;
        float tooltipX = Math.max(chartLeft + tooltipW / 2, Math.min(chartRight - tooltipW / 2, activeX));

        canvas.drawRoundRect(tooltipX - tooltipW / 2, chartTop - 26f, tooltipX + tooltipW / 2, chartTop + 6f, 10f, 10f, tooltipBgPaint);
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(tooltipText, tooltipX, chartTop - 4f, tooltipTextPaint);
    }

    private float valueToY(float val, float yMin, float yMax, float chartTop, float chartBottom) {
        float normalized = (val - yMin) / Math.max(0.0001f, (yMax - yMin));
        float y = chartBottom - (normalized * (chartBottom - chartTop));
        return Math.max(chartTop + 2f, Math.min(chartBottom - 2f, y));
    }
}