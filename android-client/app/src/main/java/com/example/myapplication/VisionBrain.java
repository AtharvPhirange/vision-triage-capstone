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

                        NormalizedLandmark leftIris = faceLandmarks.get(474);
                        NormalizedLandmark rightIris = faceLandmarks.get(469);

                        Log.d("VisionBrain", "Left Iris X: " + leftIris.x() + " Y: " + leftIris.y());
                        Log.d("VisionBrain", "Right Iris X: " + rightIris.x() + " Y: " + rightIris.y());
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
}