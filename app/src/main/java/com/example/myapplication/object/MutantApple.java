package com.example.myapplication.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.Random;
import java.util.List;

public class MutantApple {
    public Point position;
    private boolean hasLegs = false;
    private float legAnimation = 0;
    private Random random = new Random();

    public MutantApple(Point startPos) {
        this.position = startPos;
    }

    public void update(Point snakeHead, int numTilesX, int numTilesY, List<Point> snakeBody, Portal portal) {
        double distance = Math.sqrt(Math.pow(position.x - snakeHead.x, 2) + Math.pow(position.y - snakeHead.y, 2));

        if (distance <= 5) {
            hasLegs = true;
            runAway(snakeHead, numTilesX, numTilesY, snakeBody, portal);
            legAnimation += 0.5f;
        } else {
            hasLegs = false;
        }
    }

    private void runAway(Point snakeHead, int numTilesX, int numTilesY, List<Point> snakeBody, Portal portal) {
        // Xác định hướng chạy xa khỏi đầu rắn
        int dx = position.x - snakeHead.x;
        int dy = position.y - snakeHead.y;

        Point nextPos = new Point(position.x, position.y);

        if (Math.abs(dx) > Math.abs(dy)) {
            nextPos.x += (dx > 0) ? 1 : -1;
        } else {
            nextPos.y += (dy > 0) ? 1 : -1;
        }

        // Kiểm tra cạnh màn hình (Bị kẹt - không thể ra ngoài)
        if (nextPos.x < 0 || nextPos.x >= numTilesX || nextPos.y < 0 || nextPos.y >= numTilesY) {
            return; // Bị kẹt
        }

        // Kiểm tra Portal (Không thể đi qua)
        if (nextPos.equals(portal.position1) || nextPos.equals(portal.position2)) {
            return;
        }

        // Kiểm tra thân rắn (Không chạy vào thân rắn)
        for (Point p : snakeBody) {
            if (nextPos.equals(p)) return;
        }

        // Nếu hợp lệ thì di chuyển
        position = nextPos;
    }

    public void draw(Canvas canvas, Paint paint, int tileSize) {
        float pSize = (float) tileSize / 8;
        int x = position.x * tileSize;
        int y = position.y * tileSize;

        // Vẽ chân nếu đang chạy
        if (hasLegs) {
            paint.setColor(Color.rgb(101, 67, 33)); // Màu nâu
            float legOffset = (float) Math.sin(legAnimation) * (pSize * 2);
            // Chân trái
            canvas.drawRect(x + 2 * pSize, y + 6 * pSize, x + 3 * pSize, y + 8 * pSize + legOffset, paint);
            // Chân phải
            canvas.drawRect(x + 5 * pSize, y + 6 * pSize, x + 6 * pSize, y + 8 * pSize - legOffset, paint);
        }

        // Vẽ quả táo pixel (Màu tím đột biến)
        int[][] apple = {
                {0, 0, 0, 4, 4, 0, 0, 0},
                {0, 0, 3, 3, 4, 0, 0, 0},
                {0, 1, 1, 1, 1, 1, 0, 0},
                {1, 1, 1, 1, 1, 1, 1, 0},
                {1, 1, 1, 1, 1, 1, 1, 0},
                {1, 1, 1, 1, 1, 1, 1, 0},
                {0, 1, 1, 1, 1, 1, 0, 0},
                {0, 0, 1, 1, 1, 0, 0, 0}
        };

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (apple[i][j] != 0) {
                    switch (apple[i][j]) {
                        case 1: paint.setColor(Color.rgb(150, 0, 255)); break; // Tím đột biến
                        case 3: paint.setColor(Color.GREEN); break;
                        case 4: paint.setColor(Color.rgb(101, 67, 33)); break; 
                    }
                    canvas.drawRect(x + j * pSize, y + i * pSize, x + (j + 1) * pSize, y + (i + 1) * pSize, paint);
                }
            }
        }

        // Vẽ biểu cảm sợ hãi (Mắt to trợn trừng và run rẩy)
        if (hasLegs) {
            float jitterX = (random.nextFloat() - 0.5f) * (pSize * 0.5f);
            float jitterY = (random.nextFloat() - 0.5f) * (pSize * 0.5f);

            // Lòng trắng mắt to
            paint.setColor(Color.WHITE);
            canvas.drawRect(x + 1.5f * pSize + jitterX, y + 2.5f * pSize + jitterY, 
                            x + 3.5f * pSize + jitterX, y + 4.5f * pSize + jitterY, paint);
            canvas.drawRect(x + 4.5f * pSize + jitterX, y + 2.5f * pSize + jitterY, 
                            x + 6.5f * pSize + jitterX, y + 4.5f * pSize + jitterY, paint);

            // Con ngươi nhỏ xíu (biểu hiện sốc/sợ hãi)
            paint.setColor(Color.BLACK);
            canvas.drawRect(x + 2.2f * pSize + jitterX, y + 3.2f * pSize + jitterY, 
                            x + 2.8f * pSize + jitterX, y + 3.8f * pSize + jitterY, paint);
            canvas.drawRect(x + 5.2f * pSize + jitterX, y + 3.2f * pSize + jitterY, 
                            x + 5.8f * pSize + jitterX, y + 3.8f * pSize + jitterY, paint);
            
            // Miệng nhỏ há hốc vì sợ
            canvas.drawRect(x + 3.5f * pSize, y + 5.5f * pSize, x + 4.5f * pSize, y + 6.5f * pSize, paint);
        }
    }
}
