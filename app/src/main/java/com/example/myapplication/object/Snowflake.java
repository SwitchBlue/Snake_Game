package com.example.myapplication.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.Random;

public class Snowflake {
    public float x, y;
    private float speed;
    private float angle;
    private float rotateSpeed;
    private float drift;
    private Random random = new Random();

    public Snowflake(int width, int height) {
        reset(width, height);
    }

    public void reset(int width, int height) {
        x = random.nextFloat() * width;
        y = -20 - random.nextFloat() * height;
        speed = 1 + random.nextFloat() * 3;
        angle = random.nextFloat() * 360;
        rotateSpeed = -2 + random.nextFloat() * 4;
        drift = -1 + random.nextFloat() * 2;
    }

    public void update(int width, int height) {
        y += speed;
        x += drift + Math.sin(y / 40.0) * 1.0;
        angle += rotateSpeed;
        if (y > height + 20) reset(width, height);
    }

    public void draw(Canvas canvas, Paint paint, float pSize) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(angle);
        paint.setColor(Color.WHITE);
        paint.setAlpha(200);

        // Vẽ tinh thể tuyết pixel cross (+)
        canvas.drawRect(-pSize, -pSize * 3, pSize, pSize * 3, paint);
        canvas.drawRect(-pSize * 3, -pSize, pSize * 3, pSize, paint);
        
        // Vẽ 4 điểm chéo x
        canvas.rotate(45);
        canvas.drawRect(-pSize, -pSize * 2, pSize, pSize * 2, paint);
        canvas.drawRect(-pSize * 2, -pSize, pSize * 2, pSize, paint);

        canvas.restore();
        paint.setAlpha(255);
    }
}
