package com.example.myapplication;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import androidx.annotation.Nullable;

public class PlrWaveformView extends View {

    private Paint gridPaint;
    private Paint linePaint;
    private Paint dotPaint;
    private Paint flashLinePaint;
    private Paint labelPaint;

    private float baselineRatio = 0.45f;
    private float minRatio = 0.28f;
    private float animatedProgress = 1.0f; // 0.0 to 1.0
    private ValueAnimator playbackAnimator;

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
        gridPaint.setStrokeWidth(1f);
        gridPaint.setStyle(Paint.Style.STROKE);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#003893"));
        linePaint.setStrokeWidth(3f);
        linePaint.setStyle(Paint.Style.STROKE);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.parseColor("#003893"));
        dotPaint.setStyle(Paint.Style.FILL);

        flashLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flashLinePaint.setColor(Color.parseColor("#D97706"));
        flashLinePaint.setStrokeWidth(1.5f);
        flashLinePaint.setStyle(Paint.Style.STROKE);
        flashLinePaint.setPathEffect(new DashPathEffect(new float[]{6f, 6f}, 0));

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#64748B"));
        labelPaint.setTextSize(22f);
    }

    public void setWaveformMetrics(float baseRatio, float minConstrictedRatio) {
        this.baselineRatio = Math.max(0.1f, Math.min(0.8f, baseRatio));
        this.minRatio = Math.max(0.05f, Math.min(this.baselineRatio, minConstrictedRatio));
        invalidate();
    }

    public void startPlaybackAnimation() {
        if (playbackAnimator != null && playbackAnimator.isRunning()) {
            playbackAnimator.cancel();
        }
        playbackAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        playbackAnimator.setDuration(4000); // 4 seconds playback
        playbackAnimator.setInterpolator(new LinearInterpolator());
        playbackAnimator.addUpdateListener(animation -> {
            animatedProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        playbackAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float paddingLeft = 50f;
        float paddingRight = getWidth() - 30f;
        float paddingTop = 30f;
        float paddingBottom = getHeight() - 50f;

        float width = paddingRight - paddingLeft;
        float height = paddingBottom - paddingTop;

        // Draw Canvas Background Box
        canvas.drawRect(paddingLeft, paddingTop, paddingRight, paddingBottom, gridPaint);

        // Draw Y-Axis Gridlines
        int ySteps = 4;
        for (int i = 0; i <= ySteps; i++) {
            float y = paddingBottom - (height / ySteps) * i;
            canvas.drawLine(paddingLeft, y, paddingRight, y, gridPaint);
            float val = 0.1f * i;
            labelPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format("%.1f", val), paddingLeft - 8f, y + 6f, labelPaint);
        }

        // Draw X-Axis Gridlines (0s to 8s)
        int xSteps = 8;
        for (int i = 0; i <= xSteps; i++) {
            float x = paddingLeft + (width / xSteps) * i;
            canvas.drawLine(x, paddingTop, x, paddingBottom, gridPaint);
            labelPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(i + "s", x, paddingBottom + 28f, labelPaint);
        }

        // Draw Flash Trigger Line (t = 1.0s)
        float flashX = paddingLeft + (width / 8f) * 1.0f;
        canvas.drawLine(flashX, paddingTop, flashX, paddingBottom, flashLinePaint);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Flash Trigger (1s)", flashX + 6f, paddingTop + 20f, labelPaint);

        // Map 8-Second Curve
        float minY = paddingBottom - (baselineRatio / 0.5f) * height;
        float valleyY = paddingBottom - (minRatio / 0.5f) * height;

        float x0 = paddingLeft;
        float y0 = minY;

        float xFlash = paddingLeft + (width / 8f) * 1.0f;
        float yFlash = minY;

        float xValley = paddingLeft + (width / 8f) * 2.5f;
        float yValley = valleyY;

        float xEnd = paddingLeft + (width / 8f) * 8.0f;
        float yEnd = minY - (minY - valleyY) * 0.3f;

        Path path = new Path();
        path.moveTo(x0, y0);
        path.lineTo(xFlash, yFlash);
        path.cubicTo(xFlash + width * 0.05f, yFlash, xValley - width * 0.05f, yValley, xValley, yValley);
        path.cubicTo(xValley + width * 0.2f, yValley, xEnd - width * 0.2f, yEnd, xEnd, yEnd);

        canvas.drawPath(path, linePaint);

        // Draw Animated Scrubber Dot
        float currentX = paddingLeft + animatedProgress * width;
        float currentY;
        if (currentX <= xFlash) {
            currentY = yFlash;
        } else if (currentX <= xValley) {
            float t = (currentX - xFlash) / (xValley - xFlash);
            currentY = yFlash + t * (yValley - yFlash);
        } else {
            float t = (currentX - xValley) / (xEnd - xValley);
            currentY = yValley + t * (yEnd - yValley);
        }

        canvas.drawCircle(currentX, currentY, 8f, dotPaint);
    }
}