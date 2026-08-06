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
    private FaceLandmarker faceLandmarker;
    public TriageEngine triageEngine = new TriageEngine();
    public float latestRatio = 0.0f;

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
                        float irisDiameter = calculatePupilDiameter(leftEdge.x(), leftEdge.y(), rightEdge.x(), rightEdge.y());

                        NormalizedLandmark eyeOuterCorner = faceLandmarks.get(33);
                        NormalizedLandmark eyeInnerCorner = faceLandmarks.get(133);
                        float eyeWidth = calculatePupilDiameter(eyeOuterCorner.x(), eyeOuterCorner.y(), eyeInnerCorner.x(), eyeInnerCorner.y());

                        float pupilToEyeRatio = irisDiameter / eyeWidth;

                        latestRatio = pupilToEyeRatio;
                        triageEngine.processNewRatio(pupilToEyeRatio);

                        Log.d("VisionBrain", "Distance-Proof Ratio: " + pupilToEyeRatio);
                        Log.d("VisionBrain", "Left Iris Diameter: " + irisDiameter);
                    }
                })
                .setErrorListener(error -> {
                    Log.e("VisionBrain", "AI Error: " + error.getMessage());
                })
                .build();

        faceLandmarker = FaceLandmarker.createFromOptions(context, options);
    }

    public void detectFace(Bitmap bitmap, long timestamp) {
        if (faceLandmarker != null) {
            MPImage mpImage = new BitmapImageBuilder(bitmap).build();
            faceLandmarker.detectAsync(mpImage, timestamp);
        }
    }

    public float calculatePupilDiameter(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x2 - x1, y2 - y1);
    }
}