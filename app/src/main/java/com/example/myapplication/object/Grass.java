package com.example.myapplication.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.core.graphics.ColorUtils;
import java.util.Random;

public class Grass {
    private float x, y;
    private float swayOffset;
    private float swaySpeed;
    private int color;
    private static final int[] GRASS_COLORS = {
        0xFF1B4D0B, // Xanh đậm 1
        0xFF235A0E, // Xanh đậm 2
        0xFF2D6A12  // Xanh lá cỏ
    };

    public Grass(float x, float y, Random random) {
        this.x = x;
        this.y = y;
        this.swayOffset = random.nextFloat() * (float)Math.PI * 2;
        this.swaySpeed = 1.5f + random.nextFloat() * 1.0f;
        this.color = GRASS_COLORS[random.nextInt(GRASS_COLORS.length)];
    }

    public void draw(Canvas canvas, Paint paint, float pSize, long currentTime, float winterProgress) {
        float sway = (float) Math.sin(currentTime / 500.0 * swaySpeed + swayOffset) * (pSize * 1.2f);
        
        // Chuyển màu cỏ sang xanh băng giá khi mùa đông đến
        int currentColor = ColorUtils.blendARGB(color, Color.rgb(200, 230, 255), winterProgress);
        paint.setColor(currentColor);
        
        // Vẽ bụi cỏ pixel (3 ngọn)
        // Ngọn giữa
        canvas.drawRect(x, y - pSize * 2, x + pSize, y, paint);
        // Ngọn trái đung đưa
        canvas.drawRect(x - pSize * 1.5f + sway * 0.5f, y - pSize * 1.5f, x - pSize * 0.5f + sway * 0.5f, y, paint);
        // Ngọn phải đung đưa mạnh hơn
        canvas.drawRect(x + pSize * 1.5f + sway, y - pSize * 1.8f, x + pSize * 2.5f + sway, y, paint);
    }
}
