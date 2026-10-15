package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FloatingBubbleView extends View {

    private static class Bubble {
        float x, y;
        float radius;
        float speedY;
        float sineOffset;
        float sineSpeed;
        int color;

        Bubble(float x, float y, float radius, float speedY, float sineOffset, float sineSpeed, int color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speedY = speedY;
            this.sineOffset = sineOffset;
            this.sineSpeed = sineSpeed;
            this.color = color;
        }
    }

    private final List<Bubble> bubbles = new ArrayList<>();
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private boolean initialized = false;

    private static final int[] BUBBLE_COLORS = {
            Color.parseColor("#3338BDF8"), // Translucent Sky Blue
            Color.parseColor("#220284C7"), // Translucent Cerulean
            Color.parseColor("#262DD4BF"), // Translucent Teal
            Color.parseColor("#1F818CF8")  // Translucent Indigo
    };

    public FloatingBubbleView(Context context) {
        super(context);
    }

    public FloatingBubbleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private void initBubbles(int width, int height) {
        bubbles.clear();
        int count = 18;
        for (int i = 0; i < count; i++) {
            float x = random.nextFloat() * width;
            float y = random.nextFloat() * height;
            float radius = 20f + random.nextFloat() * 45f;
            float speedY = 1.2f + random.nextFloat() * 2.2f;
            float sineOffset = random.nextFloat() * (float) (2 * Math.PI);
            float sineSpeed = 0.02f + random.nextFloat() * 0.03f;
            int color = BUBBLE_COLORS[random.nextInt(BUBBLE_COLORS.length)];

            bubbles.add(new Bubble(x, y, radius, speedY, sineOffset, sineSpeed, color));
        }
        initialized = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        if (!initialized) {
            initBubbles(width, height);
        }

        for (Bubble b : bubbles) {
            // Update bubble location
            b.y -= b.speedY;
            b.sineOffset += b.sineSpeed;
            float driftX = b.x + (float) Math.sin(b.sineOffset) * 25f;

            // Reset bubble to bottom if floating off top
            if (b.y + b.radius < 0) {
                b.y = height + b.radius + random.nextFloat() * 50f;
                b.x = random.nextFloat() * width;
            }

            bubblePaint.setColor(b.color);
            bubblePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(driftX, b.y, b.radius, bubblePaint);
        }

        // 60fps continuous smooth animation loop
        postInvalidateOnAnimation();
    }
}