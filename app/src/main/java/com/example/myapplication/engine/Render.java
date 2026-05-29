package com.example.myapplication.engine;



import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.myapplication.object.Food;
import com.example.myapplication.object.Snake;
import com.example.myapplication.object.Portal;

public class Render {

    public void draw(
            Canvas canvas,
            Paint paint,
            Snake snake,
            Food food,
            Portal portal,
            int tileSize
    ) {

        canvas.drawColor(Color.BLACK);

        // snake
        paint.setColor(Color.GREEN);

        for (Point part : snake.body) {

            canvas.drawRect(
                    part.x * tileSize,
                    part.y * tileSize,
                    (part.x + 1) * tileSize,
                    (part.y + 1) * tileSize,
                    paint
            );
        }

        // food
        paint.setColor(Color.RED);

        canvas.drawCircle(
                food.position.x * tileSize + tileSize / 2f,
                food.position.y * tileSize + tileSize / 2f,
                tileSize / 2f,
                paint
        );

        // portal
        if (portal != null) {
            paint.setColor(Color.BLUE);
            canvas.drawRect(
                    portal.position1.x * tileSize,
                    portal.position1.y * tileSize,
                    (portal.position1.x + 1) * tileSize,
                    (portal.position1.y + 1) * tileSize,
                    paint
            );
            canvas.drawRect(
                    portal.position2.x * tileSize,
                    portal.position2.y * tileSize,
                    (portal.position2.x + 1) * tileSize,
                    (portal.position2.y + 1) * tileSize,
                    paint
            );
        }
    }
}