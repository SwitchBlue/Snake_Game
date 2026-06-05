package com.example.myapplication.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class Gift {
    public Point position;
    private long spawnTime;

    public Gift(int x, int y) {
        this.position = new Point(x, y);
        this.spawnTime = System.currentTimeMillis();
    }

    public void draw(Canvas canvas, Paint paint, int tileSize) {
        float pSize = tileSize / 8f;
        float startX = position.x * tileSize;
        float startY = position.y * tileSize;

        // Vẽ hộp quà Pixel Art
        // Hộp đỏ
        paint.setColor(Color.RED);
        canvas.drawRect(startX + pSize, startY + 2 * pSize, startX + 7 * pSize, startY + 7 * pSize, paint);
        
        // Nơ vàng
        paint.setColor(Color.YELLOW);
        canvas.drawRect(startX + 3.5f * pSize, startY + pSize, startX + 4.5f * pSize, startY + 7 * pSize, paint);
        canvas.drawRect(startX + pSize, startY + 4 * pSize, startX + 7 * pSize, startY + 5 * pSize, paint);
        
        // Điểm sáng
        paint.setColor(Color.WHITE);
        canvas.drawRect(startX + 2 * pSize, startY + 3 * pSize, startX + 3 * pSize, startY + 4 * pSize, paint);
    }
}
