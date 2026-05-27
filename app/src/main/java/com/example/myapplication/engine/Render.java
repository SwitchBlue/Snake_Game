package com.example.myapplication.engine;



import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.myapplication.object.Food;
import com.example.myapplication.object.Snake;

public class Render {

    public void draw(
            Canvas canvas,
            Paint paint,
            Snake snake,
            Food food,
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
    }
}