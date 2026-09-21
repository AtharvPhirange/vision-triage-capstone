package com.example.myapplication;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class OverlayView extends View {
    private Paint paint;
    private boolean isLocked = false;
    private float currentRadius = 350f;
    private final float defaultRadius = 350f;
    private final float lockedRadius = 280f;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);
        paint.setAntiAlias(true);
    }

    public void setTargetLocked(boolean locked) {
        this.isLocked = locked;

        ValueAnimator animator = ValueAnimator.ofFloat(
                currentRadius,
                locked ? lockedRadius : defaultRadius
        );
        animator.setDuration(400);
        animator.setInterpolator(new OvershootInterpolator());
        animator.addUpdateListener(animation -> {
            currentRadius = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        paint.setColor(isLocked ? Color.GREEN : Color.CYAN);

        canvas.drawCircle(cx, cy, currentRadius, paint);

        canvas.drawLine(cx, cy - currentRadius - 40, cx, cy - currentRadius + 10, paint);
        canvas.drawLine(cx, cy + currentRadius - 10, cx, cy + currentRadius + 40, paint);
        canvas.drawLine(cx - currentRadius - 40, cy, cx - currentRadius + 10, cy, paint);
        canvas.drawLine(cx + currentRadius - 10, cy, cx + currentRadius + 40, cy, paint);
    }
}