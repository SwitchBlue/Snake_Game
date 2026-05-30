package com.example.myapplication.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.Random;

public class Flower {
    private float x, y;
    private int petalColor;
    private int centerColor = Color.YELLOW;
    private float swayAngle = 0;
    private float swaySpeed;
    private float swayRange;
    private Random random = new Random();

    public Flower(int width, int height) {
        x = random.nextFloat() * width;
        y = random.nextFloat() * height;
        
        // Các màu hoa rực rỡ
        int[] colors = {
            Color.rgb(255, 105, 180), // Pink
            Color.rgb(255, 255, 255), // White
            Color.rgb(147, 112, 219), // Purple
            Color.rgb(0, 191, 255),   // Blue
            Color.rgb(255, 165, 0)    // Orange
        };
        petalColor = colors[random.nextInt(colors.length)];
        
        swaySpeed = 0.02f + random.nextFloat() * 0.03f;
        swayRange = 5 + random.nextFloat() * 10;
        swayAngle = random.nextFloat() * (float)Math.PI * 2;
    }

    public void update() {
        swayAngle += swaySpeed;
    }

    public void draw(Canvas canvas, Paint paint, int tileSize) {
        float pSize = tileSize / 12f; 
        float currentSway = (float) Math.sin(swayAngle) * swayRange;

        canvas.save();
        canvas.translate(x, y);
        
        // Làm mờ hoa (Alpha thấp để chìm vào nền)
        int originalAlpha = paint.getAlpha();
        paint.setAlpha(120); 

        // 1. Vẽ cành hoa đung đưa (Stem)
        paint.setColor(Color.rgb(10, 60, 10)); // Xanh đậm hơn
        for (int i = 0; i < 4; i++) {
            float segmentOffset = (float) Math.sin(swayAngle + i * 0.2f) * (i * pSize * 0.5f);
            canvas.drawRect(-pSize/2 + segmentOffset, i * pSize, pSize/2 + segmentOffset, (i + 1) * pSize, paint);
        }

        // 2. Vẽ hoa đung đưa theo cành (Flower Head)
        float headOffset = (float) Math.sin(swayAngle + 4 * 0.2f) * (4 * pSize * 0.5f);
        canvas.translate(headOffset, 0);
        canvas.rotate(currentSway / 2);

        // Cánh hoa Pixel
        paint.setColor(petalColor);
        canvas.drawRect(-pSize, -pSize * 2, pSize, -pSize, paint);
        canvas.drawRect(-pSize, pSize, pSize, pSize * 2, paint);
        canvas.drawRect(-pSize * 2, -pSize, -pSize, pSize, paint);
        canvas.drawRect(pSize, -pSize, pSize * 2, pSize, paint);

        // Nhụy hoa
        paint.setColor(centerColor);
        canvas.drawRect(-pSize, -pSize, pSize, pSize, paint);

        paint.setAlpha(originalAlpha);
        canvas.restore();
    }
}
