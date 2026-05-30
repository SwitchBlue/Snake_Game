package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        gameView = new GameView(this);

        setContentView(gameView);
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