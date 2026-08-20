package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class OverlayView extends View {

    private Paint reticlePaint;
    private Paint guidePaint;
    private boolean isTargetLocked = false;

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        reticlePaint = new Paint();
        reticlePaint.setColor(Color.CYAN);
        reticlePaint.setStyle(Paint.Style.STROKE);
        reticlePaint.setStrokeWidth(6f);
        reticlePaint.setAntiAlias(true);

        guidePaint = new Paint();
        guidePaint.setColor(Color.argb(100, 255, 255, 255));
        guidePaint.setStyle(Paint.Style.STROKE);
        guidePaint.setStrokeWidth(2f);
        guidePaint.setAntiAlias(true);
    }

    public void setTargetLocked(boolean locked) {
        this.isTargetLocked = locked;
        reticlePaint.setColor(locked ? Color.GREEN : Color.CYAN);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2.5f;
        float radius = Math.min(getWidth(), getHeight()) * 0.22f;

        canvas.drawCircle(centerX, centerY, radius, reticlePaint);

        canvas.drawLine(centerX - radius - 30, centerY, centerX + radius + 30, centerY, guidePaint);
        canvas.drawLine(centerX, centerY - radius - 30, centerX, centerY + radius + 30, guidePaint);
    }
}