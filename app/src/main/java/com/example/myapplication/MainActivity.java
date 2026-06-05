package com.example.myapplication;

import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.content.SharedPreferences;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.object.HandTracker;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;


public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 100;
    private HandTracker handTracker;

    GameView gameView;
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_REQUEST) {

            if (grantResults.length > 0 &&
                    grantResults[0]
                            == PackageManager.PERMISSION_GRANTED) {

                startCamera();
            }
        }
    }
    private void startCamera() {

        PreviewView previewView =
                findViewById(R.id.cameraPreview);

        ListenableFuture<ProcessCameraProvider>
                cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {

                ProcessCameraProvider cameraProvider =
                        cameraProviderFuture.get();

                Preview preview =
                        new Preview.Builder().build();

                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider());

                // Cấu hình ImageAnalysis để nhận diện tay
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), image -> {
                    try {
                        // Chuyển đổi ImageProxy sang Bitmap
                        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        bitmap.copyPixelsFromBuffer(buffer);
                        
                        // Lấy độ xoay của ảnh
                        int rotationDegrees = image.getImageInfo().getRotationDegrees();
                        
                        // Gửi cho HandTracker xử lý
                        handTracker.detect(bitmap, rotationDegrees);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        image.close();
                    }
                });

                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis);

            } catch (Exception e) {

                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs =
                getSharedPreferences(
                        "SnakeGamePrefs",
                        MODE_PRIVATE);

        String mode =
                prefs.getString(
                        "control_mode",
                        "SWIPE");

        // Ẩn thanh tiêu đề (ActionBar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Chế độ toàn màn hình
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_main);
        gameView = new GameView(this);
        handTracker =
                new HandTracker(
                        this,
                        gameView);
        if(mode.equals("HAND")){
            gameView.setControlMode(
                    GameView.ControlMode.HAND);
        }else{
            gameView.setControlMode(
                    GameView.ControlMode.SWIPE);
        }

        FrameLayout container =
                findViewById(R.id.gameContainer);

        container.addView(gameView);
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            startCamera();

        } else {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    CAMERA_REQUEST);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        gameView.pause();
    }
}
