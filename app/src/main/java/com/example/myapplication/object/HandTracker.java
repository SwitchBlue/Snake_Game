package com.example.myapplication.object;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.example.myapplication.GameView;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

public class HandTracker {

    private final GameView gameView;
    private HandLandmarker handLandmarker;

    public HandTracker(Context context, GameView gameView) {
        this.gameView = gameView;
        setupHandLandmarker(context);
    }

    private void setupHandLandmarker(Context context) {
        try {
            HandLandmarker.HandLandmarkerOptions options = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(BaseOptions.builder().setModelAssetPath("hand_landmarker.task").build())
                    .setRunningMode(RunningMode.LIVE_STREAM)
                    .setResultListener(this::returnLivestreamResult)
                    .build();
            handLandmarker = HandLandmarker.createFromOptions(context, options);
            Log.d("HandTracker", "HandLandmarker initialized successfully");
        } catch (Exception e) {
            Log.e("HandTracker", "Failed to initialize HandLandmarker: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void detect(Bitmap bitmap, int rotationDegrees) {
        if (handLandmarker == null) return;
        
        MPImage mpImage = new BitmapImageBuilder(bitmap).build();
        ImageProcessingOptions processingOptions = ImageProcessingOptions.builder()
                .setRotationDegrees(rotationDegrees)
                .build();
        
        handLandmarker.detectAsync(mpImage, processingOptions, SystemClock.uptimeMillis());
    }

    private void returnLivestreamResult(HandLandmarkerResult result, MPImage mpImage) {
        if (result.landmarks().isEmpty()) {
            gameView.post(() -> gameView.setHandPosition(-1, -1));
            return;
        }

        Log.d("HandTracker", "Hand detected!");

        // Lấy tọa độ cổ tay (Wrist - Landmark 0)
        float x = result.landmarks().get(0).get(0).x();
        float y = result.landmarks().get(0).get(0).y();

        // Đảo ngược X vì dùng camera trước (hiệu ứng gương)
        x = 1.0f - x;

        // Cập nhật tọa độ để GameView vẽ Visual Feedback
        final float finalX = x;
        final float finalY = y;
        gameView.post(() -> gameView.setHandPosition(finalX, finalY));

        float dx = x - 0.5f;
        float dy = y - 0.5f;
        float threshold = 0.15f;

        String direction = "";
        
        // Logic: Hướng nào lệch nhiều hơn thì rẽ theo hướng đó
        if (Math.abs(dx) > Math.abs(dy)) {
            if (Math.abs(dx) > threshold) {
                direction = (dx < 0) ? "LEFT" : "RIGHT";
            }
        } else {
            if (Math.abs(dy) > threshold) {

                direction = (dy < 0) ? "UP" : "DOWN";
            }
        }

        if (!direction.isEmpty()) {
            final String finalDir = direction;
            gameView.post(() -> gameView.setHandDirection(finalDir));
        }
    }

    public void updateDirection(String direction) {
        gameView.setHandDirection(direction);
    }
}
