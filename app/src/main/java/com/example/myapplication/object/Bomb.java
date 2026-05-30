package com.example.myapplication.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.ArrayList;
import java.util.List;

public class Bomb {
    public Point position;
    private long spawnTime;
    private final long FUSE_TIME = 3000; // 3 giây
    public boolean exploded = false;
    public boolean finished = false;
    private long explosionShowTime = 500; // Hiển thị vụ nổ trong 0.5s

    public Bomb(Point position) {
        this.position = position;
        this.spawnTime = System.currentTimeMillis();
    }

    public void update() {
        long elapsed = System.currentTimeMillis() - spawnTime;
        if (elapsed >= FUSE_TIME && !exploded) {
            exploded = true;
        } else if (elapsed >= FUSE_TIME + explosionShowTime) {
            finished = true;
        }
    }

    public boolean isInRange(Point p) {
        return Math.abs(p.x - position.x) <= 1 && Math.abs(p.y - position.y) <= 1;
    }

    public void draw(Canvas canvas, Paint paint, int tileSize) {
        if (finished) return;

        float centerX = position.x * tileSize + tileSize / 2f;
        float centerY = position.y * tileSize + tileSize / 2f;

        if (!exploded) {
            // Vẽ quả bom Pixel Art chuẩn bị nổ
            long elapsed = System.currentTimeMillis() - spawnTime;
            float ratio = (float) elapsed / FUSE_TIME;
            
            // Nhấp nháy đỏ theo nhịp nổ
            int rValue = (int) (100 + 155 * Math.abs(Math.sin(elapsed / 100.0)));
            
            // Pattern 8x8 cho Bom
            int[][] bombPattern = {
                {0, 0, 0, 2, 2, 0, 0, 0},
                {0, 0, 1, 1, 1, 1, 0, 0},
                {0, 1, 1, 1, 1, 1, 1, 0},
                {0, 1, 1, 3, 3, 1, 1, 0},
                {0, 1, 1, 3, 3, 1, 1, 0},
                {0, 1, 1, 1, 1, 1, 1, 0},
                {0, 0, 1, 1, 1, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0}
            };

            float pSize = tileSize / 8f;
            float startX = position.x * tileSize;
            float startY = position.y * tileSize;

            // Hiệu ứng to dần
            canvas.save();
            canvas.translate(centerX, centerY);
            float scale = 1.0f + ratio * 0.3f;
            canvas.scale(scale, scale);
            canvas.translate(-centerX, -centerY);

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (bombPattern[i][j] != 0) {
                        switch (bombPattern[i][j]) {
                            case 1: paint.setColor(Color.rgb(rValue, 0, 0)); break; // Thân bom (nhấp nháy)
                            case 2: paint.setColor(Color.rgb(200, 200, 0)); break; // Ngòi nổ
                            case 3: paint.setColor(Color.WHITE); break; // Điểm bóng sáng (Glare)
                        }
                        canvas.drawRect(startX + j * pSize, startY + i * pSize, 
                                        startX + (j + 1) * pSize, startY + (i + 1) * pSize, paint);
                    }
                }
            }
            canvas.restore();
        } else {
            // Vẽ vụ nổ 3x3 theo phong cách Pixel Art
            float pSize = tileSize / 8f;
            long elapsedSinceExplosion = System.currentTimeMillis() - (spawnTime + FUSE_TIME);
            float explosionProgress = (float) elapsedSinceExplosion / explosionShowTime;
            
            // Lớp lửa cam bên ngoài
            paint.setColor(Color.rgb(255, 140, 0));
            drawPixelCircle(canvas, centerX, centerY, (tileSize * 1.5f) * explosionProgress, pSize, paint);
            
            // Lớp lửa vàng bên trong
            paint.setColor(Color.YELLOW);
            drawPixelCircle(canvas, centerX, centerY, (tileSize * 1.0f) * explosionProgress, pSize, paint);
            
            // Lõi trắng trung tâm
            paint.setColor(Color.WHITE);
            drawPixelCircle(canvas, centerX, centerY, (tileSize * 0.5f) * (1 - explosionProgress), pSize, paint);
            
            // Thêm các hạt lửa (sparks) bay ra
            paint.setColor(Color.rgb(255, 69, 0));
            for (int i = 0; i < 12; i++) {
                double angle = i * (Math.PI * 2 / 12);
                float dist = tileSize * 1.5f * explosionProgress;
                float sparkX = centerX + (float) (Math.cos(angle) * dist);
                float sparkY = centerY + (float) (Math.sin(angle) * dist);
                canvas.drawRect(sparkX - pSize, sparkY - pSize, sparkX + pSize, sparkY + pSize, paint);
            }
        }
    }

    private void drawPixelCircle(Canvas canvas, float cx, float cy, float radius, float pSize, Paint paint) {
        if (radius <= 0) return;
        for (float x = -radius; x <= radius; x += pSize) {
            for (float y = -radius; y <= radius; y += pSize) {
                if (x * x + y * y <= radius * radius) {
                    canvas.drawRect(cx + x, cy + y, cx + x + pSize, cy + y + pSize, paint);
                }
            }
        }
    }
}
