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
    float startX, startY;
    boolean playing = true;
    boolean gameStarted = false;
    float foodPulse = 1.0f;
    boolean growing = true;

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
        paint.setAntiAlias(true);

        snake.add(new Point(5, 5));
    }

    @Override
    public void run() {

        while (playing) {

            if (gameStarted) {

                update();
            }

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


        if (newHead.x < 0 ||
                newHead.y < 0 ||
                newHead.x >= 20 ||
                newHead.y >= 20) {

            playing = false;
        }
        if (growing) {

            foodPulse += 0.05f;

            if (foodPulse >= 1.3f) {

                growing = false;
            }

        } else {

            foodPulse -= 0.05f;

            if (foodPulse <= 0.7f) {

                growing = true;
            }
        }
    }

    private void draw() {

        if (holder.getSurface().isValid()) {

            Canvas canvas = holder.lockCanvas();

            canvas.drawColor(Color.BLACK);

            // START MENU
            if (!gameStarted) {

                paint.setColor(Color.GREEN);
                paint.setTextSize(120);

                canvas.drawText(
                        "SNAKE GAME",
                        80,
                        400,
                        paint
                );

                paint.setColor(Color.WHITE);
                paint.setTextSize(60);

                canvas.drawText(
                        "Tap To Start",
                        180,
                        600,
                        paint
                );

                holder.unlockCanvasAndPost(canvas);

                return;
            }

            // vẽ rắn
            paint.setColor(Color.GREEN);

            for (Point part : snake) {

                float left = part.x * tileSize;
                float top = part.y * tileSize;
                float right = (part.x + 1) * tileSize;
                float bottom = (part.y + 1) * tileSize;

                // glow
                paint.setColor(Color.argb(80, 0, 255, 0));

                canvas.drawRect(
                        left - 10,
                        top - 10,
                        right + 10,
                        bottom + 10,
                        paint
                );

                // thân rắn
                paint.setColor(Color.GREEN);

                canvas.drawRect(
                        left,
                        top,
                        right,
                        bottom,
                        paint
                );
            }

// vẽ thức ăn glow

            float centerX = food.x * tileSize + tileSize / 2f;
            float centerY = food.y * tileSize + tileSize / 2f;

            float radius = (tileSize / 2f) * foodPulse;

// glow ngoài
            paint.setColor(Color.argb(100, 255, 0, 0));

            canvas.drawCircle(
                    centerX,
                    centerY,
                    radius + 20,
                    paint
            );

// glow giữa
            paint.setColor(Color.argb(180, 255, 50, 50));

            canvas.drawCircle(
                    centerX,
                    centerY,
                    radius + 10,
                    paint
            );

// lõi thức ăn
            paint.setColor(Color.RED);

            canvas.drawCircle(
                    centerX,
                    centerY,
                    radius,
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
        if (!gameStarted) {

            gameStarted = true;

            return true;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                startX = event.getX();
                startY = event.getY();
                break;

            case MotionEvent.ACTION_UP:

                float endX = event.getX();
                float endY = event.getY();

                float diffX = endX - startX;
                float diffY = endY - startY;

                if (Math.abs(diffX) > Math.abs(diffY)) {

                    // vuốt ngang

                    if (diffX > 0 && !direction.equals("LEFT")) {

                        direction = "RIGHT";

                    } else if (diffX < 0 && !direction.equals("RIGHT")) {

                        direction = "LEFT";
                    }

                } else {

                    // vuốt dọc

                    if (diffY > 0 && !direction.equals("UP")) {

                        direction = "DOWN";

                    } else if (diffY < 0 && !direction.equals("DOWN")) {

                        direction = "UP";
                    }
                }

                break;
        }

        return true;
    }
}