package com.example.myapplication;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    Thread thread;
    boolean playing = true;

    SurfaceHolder holder;
    Paint paint;

    int tileSize = 50;

    ArrayList<Point> snake = new ArrayList<>();

    String direction = "RIGHT";

    Point food = new Point(10, 10);

    public GameView(Context context) {
        super(context);

        holder = getHolder();
        paint = new Paint();

        snake.add(new Point(5, 5));
    }

    @Override
    public void run() {

        while (playing) {

            update();
            draw();
            sleep();
        }
    }

    private void update() {

        Point head = snake.get(0);

        Point newHead = new Point(head.x, head.y);

        switch (direction) {

            case "UP":
                newHead.y--;
                break;

            case "DOWN":
                newHead.y++;
                break;

            case "LEFT":
                newHead.x--;
                break;

            case "RIGHT":
                newHead.x++;
                break;
        }

        snake.add(0, newHead);

        if (newHead.equals(food)) {

            Random random = new Random();

            food = new Point(
                    random.nextInt(20),
                    random.nextInt(20)
            );

        } else {

            snake.remove(snake.size() - 1);
        }

        // game over nếu đụng tường
        if (newHead.x < 0 ||
                newHead.y < 0 ||
                newHead.x >= 20 ||
                newHead.y >= 20) {

            playing = false;
        }
    }

    private void draw() {

        if (holder.getSurface().isValid()) {

            Canvas canvas = holder.lockCanvas();

            canvas.drawColor(Color.BLACK);

            // vẽ rắn
            paint.setColor(Color.GREEN);

            for (Point part : snake) {

                canvas.drawRect(
                        part.x * tileSize,
                        part.y * tileSize,
                        (part.x + 1) * tileSize,
                        (part.y + 1) * tileSize,
                        paint
                );
            }

            // vẽ thức ăn
            paint.setColor(Color.RED);

            canvas.drawRect(
                    food.x * tileSize,
                    food.y * tileSize,
                    (food.x + 1) * tileSize,
                    (food.y + 1) * tileSize,
                    paint
            );

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void sleep() {

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {

        playing = true;

        thread = new Thread(this);

        thread.start();
    }

    public void pause() {

        playing = false;

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (event.getX() > getWidth() / 2) {

                direction = "RIGHT";

            } else {

                direction = "LEFT";
            }
        }

        return true;
    }
}