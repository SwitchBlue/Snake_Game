package com.example.myapplication.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class MenuScreen {

    public void draw(Canvas canvas, Paint paint) {

        canvas.drawColor(Color.BLACK);

        paint.setColor(Color.GREEN);
        paint.setTextSize(120);

        canvas.drawText(
                "SNAKE GAME",
                80,
                400,
                paint
        );

        paint.setColor(Color.WHITE);
        paint.setTextSize(60);

        canvas.drawText(
                "Tap To Start",
                180,
                600,
                paint
        );
    }
}