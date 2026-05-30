package com.example.myapplication.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.Random;

public class Leaf {
    public float x, y;
    private float speed;
    private float angle;
    private float rotateSpeed;
    private int[][] pattern;
    private int baseColor;
    private int veinColor;
    private Random random = new Random();

    public Leaf(int width, int height) {
        reset(width, height);
    }

    public void reset(int width, int height) {
        x = random.nextFloat() * width;
        y = -50 - random.nextFloat() * height;
        speed = 2 + random.nextFloat() * 4;
        angle = random.nextFloat() * 360;
        rotateSpeed = -3 + random.nextFloat() * 6;

        // Chọn màu ngẫu nhiên (Xanh lá, Vàng mùa thu, Cam, Nâu)
        int type = random.nextInt(4);
        if (type == 0) { // Xanh
            baseColor = Color.rgb(34, 139, 34);
            veinColor = Color.rgb(0, 100, 0);
        } else if (type == 1) { // Vàng
            baseColor = Color.rgb(218, 165, 32);
            veinColor = Color.rgb(139, 69, 19);
        } else if (type == 2) { // Cam
            baseColor = Color.rgb(255, 140, 0);
            veinColor = Color.rgb(128, 0, 0);
        } else { // Nâu
            baseColor = Color.rgb(139, 69, 19);
            veinColor = Color.rgb(92, 51, 23);
        }

        // Định nghĩa pattern pixel cho lá kim (8x8)
        pattern = new int[][]{
                {0, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 0, 0, 0, 1, 1, 0},
                {0, 0, 0, 0, 1, 1, 0, 0},
                {0, 0, 0, 1, 2, 0, 0, 0},
                {0, 0, 1, 2, 1, 0, 0, 0},
                {0, 1, 2, 1, 0, 0, 0, 0},
                {0, 1, 1, 0, 0, 0, 0, 0},
                {0, 2, 0, 0, 0, 0, 0, 0} // Cuống lá
        };
    }

    public void update(int width, int height) {
        y += speed;
        x += Math.sin(y / 60.0) * 1.5; // Bay lượn mềm mại hơn
        angle += rotateSpeed;
        if (y > height + 50) reset(width, height);
    }

    public void draw(Canvas canvas, Paint paint) {
        float pSize = 4f; // Kích thước mỗi pixel của lá
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(angle);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (pattern[i][j] != 0) {
                    paint.setColor(pattern[i][j] == 1 ? baseColor : veinColor);
                    canvas.drawRect((j - 4) * pSize, (i - 4) * pSize, (j - 3) * pSize, (i - 3) * pSize, paint);
                }
            }
        }
        canvas.restore();
    }
}
