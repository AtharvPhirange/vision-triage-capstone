package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

public class VisionBrain {
    public Bitmap currentBitmap = null;
    public float normalizedIrisDiameter = 0f;
    public float pupilX = -1f;
    public float pupilY = -1f;
    private FaceLandmarker faceLandmarker;
    public TriageEngine triageEngine = new TriageEngine();
    public float latestRatio = 0.0f;
    private float smoothedRatio = 0.0f;
    private static final float ALPHA = 0.15f;

    public void initializeAI(Context context) {
        BaseOptions baseOptions = BaseOptions.builder()
                .setModelAssetPath("face_landmarker.task")
                .build();

        FaceLandmarker.FaceLandmarkerOptions options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumFaces(1)
                .setResultListener((result, image) -> {
                    if (result != null && result.faceLandmarks() != null && !result.faceLandmarks().isEmpty()) {
                        List<NormalizedLandmark> faceLandmarks = result.faceLandmarks().get(0);

                        NormalizedLandmark leftEdge = faceLandmarks.get(471);
                        NormalizedLandmark rightEdge = faceLandmarks.get(469);
                        float maxIrisDiameter = calculatePupilDiameter(leftEdge.x(), leftEdge.y(), rightEdge.x(), rightEdge.y());

                        NormalizedLandmark eyeOuterCorner = faceLandmarks.get(33);
                        NormalizedLandmark eyeInnerCorner = faceLandmarks.get(133);
                        float eyeWidth = calculatePupilDiameter(eyeOuterCorner.x(), eyeOuterCorner.y(), eyeInnerCorner.x(), eyeInnerCorner.y());

                        NormalizedLandmark eyeTop = faceLandmarks.get(159);
                        NormalizedLandmark eyeBottom = faceLandmarks.get(145);
                        float eyeHeight = calculatePupilDiameter(eyeTop.x(), eyeTop.y(), eyeBottom.x(), eyeBottom.y());

                        float eyeOpenness = eyeHeight / eyeWidth;

                        NormalizedLandmark pupilCenter = faceLandmarks.get(468);
                        pupilX = pupilCenter.x();
                        pupilY = pupilCenter.y();

                        if (currentBitmap != null) {
                            float rawPupilRadius = calculateTruePupilRadius(currentBitmap, pupilX, pupilY);
                            normalizedIrisDiameter = (rawPupilRadius * 2) / currentBitmap.getWidth();

                        } else {
                            normalizedIrisDiameter = 0f;
                        }

                        if (eyeOpenness < 0.12f) {
                            pupilX = -1f;
                            pupilY = -1f;
                            normalizedIrisDiameter = 0f;
                            smoothedRatio = 0.0f;
                            latestRatio = 0.0f;
                        } else {
                            float rawRatio = normalizedIrisDiameter / eyeWidth;

                            if (rawRatio > 0.42f) {
                                rawRatio = 0.42f;
                            }

                            if (rawRatio < 0.1f) {
                                rawRatio = smoothedRatio;
                            }

                            if (smoothedRatio == 0.0f) {
                                smoothedRatio = rawRatio;
                            } else {
                                smoothedRatio = smoothedRatio + ALPHA * (rawRatio - smoothedRatio);
                            }
                            latestRatio = smoothedRatio;
                        }

                        triageEngine.processNewRatio(latestRatio);

                    } else {
                        pupilX = -1f;
                        pupilY = -1f;
                        normalizedIrisDiameter = 0f;
                        smoothedRatio = 0.0f;
                        latestRatio = 0.0f;
                    }
                })
                .setErrorListener(error -> {
                    Log.e("VisionBrain", "AI Error: " + error.getMessage());
                })
                .build();

        faceLandmarker = FaceLandmarker.createFromOptions(context, options);
    }

    public void detectFace(Bitmap bitmap, long timestamp) {
        this.currentBitmap = bitmap;
        if (faceLandmarker != null) {
            MPImage mpImage = new BitmapImageBuilder(bitmap).build();
            faceLandmarker.detectAsync(mpImage, timestamp);
        }
    }

    public float calculatePupilDiameter(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x2 - x1, y2 - y1);
    }

    public float calculateTruePupilRadius(Bitmap faceBitmap, float normalizedX, float normalizedY) {
        int centerX = (int) (normalizedX * faceBitmap.getWidth());
        int centerY = (int) (normalizedY * faceBitmap.getHeight());

        int boxSize = 60;
        int startX = Math.max(0, centerX - (boxSize / 2));
        int startY = Math.max(0, centerY - (boxSize / 2));
        int endX = Math.min(faceBitmap.getWidth(), startX + boxSize);
        int endY = Math.min(faceBitmap.getHeight(), startY + boxSize);

        if (startX >= endX || startY >= endY) {
            return 0f;
        }

        int darkPixelCount = 0;
        int threshold = 4;

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int pixel = faceBitmap.getPixel(x, y);

                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                int luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                if (luminance < threshold) {
                    darkPixelCount++;
                }
            }
        }

        return (float) Math.sqrt(darkPixelCount / Math.PI);
    }
}