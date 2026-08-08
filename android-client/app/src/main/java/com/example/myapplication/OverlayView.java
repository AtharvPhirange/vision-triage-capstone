package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;

public class OverlayView extends View {
    private float pupilX = -1f;
    private float pupilY = -1f;
    private float normalizedDiameter = 0f;
    private Size imageSize = new Size(1, 1);
    private Paint paint;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        paint.setAntiAlias(true);
    }

    public void updatePupilState(float x, float y, float diameter, Size size) {
        this.pupilX = x;
        this.pupilY = y;
        this.normalizedDiameter = diameter;
        this.imageSize = size;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pupilX > 0 && pupilY > 0 && normalizedDiameter > 0) {
            float viewWidth = getWidth();
            float viewHeight = getHeight();

            float imageWidth = imageSize.getWidth();
            float imageHeight = imageSize.getHeight();

            if (viewHeight > viewWidth && imageWidth > imageHeight) {
                imageWidth = imageSize.getHeight();
                imageHeight = imageSize.getWidth();
            }

            float scale = Math.max(viewWidth / imageWidth, viewHeight / imageHeight);

            float scaledImageWidth = imageWidth * scale;
            float scaledImageHeight = imageHeight * scale;

            float offsetX = (viewWidth - scaledImageWidth) / 2f;
            float offsetY = (viewHeight - scaledImageHeight) / 2f;

            float mirroredX = 1.0f - pupilX;

            float drawX = (mirroredX * scaledImageWidth) + offsetX;
            float drawY = (pupilY * scaledImageHeight) + offsetY;

            float pixelDiameter = normalizedDiameter * scaledImageWidth;
            float pixelRadius = pixelDiameter / 2f;

            canvas.drawCircle(drawX, drawY, pixelRadius, paint);
        }
    }
}