package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.example.myapplication.object.Portal;
import com.example.myapplication.object.Leaf;
import com.example.myapplication.object.Flower;
import com.example.myapplication.object.MutantApple;
import com.example.myapplication.object.Bomb;
import com.example.myapplication.object.Grass;

public class GameView extends SurfaceView implements Runnable {

    Thread thread;
    float startX, startY;
    boolean playing = true;
    boolean gameStarted = false;
    boolean gameOver = false;

    SurfaceHolder holder;
    Paint paint;
    Paint pupilPaint;
    Paint tonguePaint;
    android.graphics.Bitmap backgroundBitmap;

    int tileSize;
    int numTilesX, numTilesY;

    ArrayList<Point> snake = new ArrayList<>();
    ArrayList<Point> prevSnake = new ArrayList<>();
    float moveProgress = 0f;
    float moveSpeed = 0.12f; // Tăng lại một chút để bù trừ overhead rendering

    String direction = "RIGHT";
    String nextDirection = "RIGHT";
    List<Point> foods = new ArrayList<>();
    MutantApple mutantApple;
    List<Bomb> bombs = new ArrayList<>();
    Portal portal = new Portal(2, 2, 17, 17);

    private SharedPreferences prefs;
    private Random random = new Random();
    private List<Leaf> leaves = new ArrayList<>();
    private List<Flower> flowers = new ArrayList<>();
    private List<Grass> grassField = new ArrayList<>();
    
    private long lastUpdateTime = 0;

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        paint = new Paint();
        paint.setAntiAlias(true);
        pupilPaint = new Paint();
        pupilPaint.setColor(Color.BLACK);
        tonguePaint = new Paint();
        tonguePaint.setColor(Color.RED);
        
        prefs = context.getSharedPreferences("SnakeGamePrefs", Context.MODE_PRIVATE);
        initGame();
    }

    private void initGame() {
        snake.clear();
        snake.add(new Point(5, 5));
        prevSnake.clear();
        prevSnake.add(new Point(5, 5));
        direction = "RIGHT";
        nextDirection = "RIGHT";
        moveProgress = 0f;
        gameOver = false;
        bombs.clear();
        
        if (numTilesX > 0 && numTilesY > 0) {
            spawnFood();
            spawnPortal();
        }
    }

    @Override
    public void run() {
        while (playing) {
            long startTime = System.currentTimeMillis();
            
            // Tính Delta Time để giữ tốc độ ổn định
            if (lastUpdateTime == 0) lastUpdateTime = startTime;
            long deltaTime = startTime - lastUpdateTime;
            lastUpdateTime = startTime;

            if (gameStarted && !gameOver) {
                updateSmooth(deltaTime);
            }
            drawSmooth();

            long timeTaken = System.currentTimeMillis() - startTime;
            long sleepTime = 16 - timeTaken; 
            if (sleepTime > 0) {
                try { Thread.sleep(sleepTime); } catch (InterruptedException e) {}
            }
        }
    }

    private void updateSmooth(long deltaTime) {
        if (tileSize <= 0 || numTilesX <= 0 || numTilesY <= 0) return;
        
        for (Leaf leaf : leaves) leaf.update(getWidth(), getHeight());
        for (Flower flower : flowers) flower.update();

        if (mutantApple != null) {
            mutantApple.update(snake.get(0), numTilesX, numTilesY, snake, portal);
        }

        // Cập nhật Bomb
        List<Bomb> toRemove = new ArrayList<>();
        for (Bomb bomb : bombs) {
            bomb.update();
            if (bomb.exploded) checkBombExplosion(bomb);
            if (bomb.finished) toRemove.add(bomb);
        }
        bombs.removeAll(toRemove);

        if (random.nextInt(1000) < 5 && bombs.size() < 3) spawnBombs();

        // Di chuyển dựa trên thời gian thực (Delta Time) thay vì số khung hình
        // Giúp tốc độ rắn không bị ảnh hưởng bởi độ lag của đồ họa
        float speedFactor = deltaTime / 16.67f; // Chuẩn hóa về 60 FPS
        moveProgress += moveSpeed * speedFactor;
        
        if (moveProgress >= 1.0f) {
            moveProgress = 0f;
            updateLogic();
        }
    }

    private void updateLogic() {
        direction = nextDirection;
        prevSnake = new ArrayList<>();
        for (Point p : snake) prevSnake.add(new Point(p.x, p.y));

        Point head = snake.get(0);
        Point newHead = new Point(head.x, head.y);

        switch (direction) {
            case "UP": newHead.y--; break;
            case "DOWN": newHead.y++; break;
            case "LEFT": newHead.x--; break;
            case "RIGHT": newHead.x++; break;
        }

        if (newHead.x < 0) newHead.x = numTilesX - 1;
        else if (newHead.x >= numTilesX) newHead.x = 0;
        if (newHead.y < 0) newHead.y = numTilesY - 1;
        else if (newHead.y >= numTilesY) newHead.y = 0;

        Point teleported = portal.teleport(newHead);
        if (teleported != null) newHead = teleported;

        for (int i = 1; i < snake.size(); i++) {
            if (newHead.equals(snake.get(i))) {
                handleGameOver();
                return;
            }
        }

        snake.add(0, newHead);

        Point eatenFood = null;
        for (Point f : foods) {
            if (newHead.equals(f)) { eatenFood = f; break; }
        }

        if (eatenFood != null) {
            foods.remove(eatenFood);
            if (foods.isEmpty() && mutantApple == null) spawnFood();
            spawnPortal();
        } else if (mutantApple != null && newHead.equals(mutantApple.position)) {
            mutantApple = null;
            if (foods.isEmpty()) spawnFood();
            spawnPortal();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void spawnFood() {
        foods.clear();
        int totalCount = random.nextInt(5) + 1;
        List<Point> emptyTiles = getEmptyTiles();
        if (!emptyTiles.isEmpty()) {
            mutantApple = new MutantApple(emptyTiles.remove(random.nextInt(emptyTiles.size())));
        }
        int normalCount = totalCount - 1;
        for (int i = 0; i < normalCount; i++) {
            if (!emptyTiles.isEmpty()) foods.add(emptyTiles.remove(random.nextInt(emptyTiles.size())));
        }
    }

    private void spawnPortal() {
        List<Point> emptyTiles = getEmptyTiles();
        if (emptyTiles.size() < 2) return;
        Random r = new Random();
        Point p1 = emptyTiles.get(r.nextInt(emptyTiles.size()));
        Point p2 = emptyTiles.get(r.nextInt(emptyTiles.size()));
        portal = new Portal(p1.x, p1.y, p2.x, p2.y);
    }

    private List<Point> getEmptyTiles() {
        List<Point> emptyTiles = new ArrayList<>();
        for (int x = 0; x < numTilesX; x++) {
            for (int y = 0; y < numTilesY; y++) {
                Point p = new Point(x, y);
                if (isPointEmpty(p)) emptyTiles.add(p);
            }
        }
        return emptyTiles;
    }

    private boolean isPointEmpty(Point p) {
        for (Point f : foods) if (p.equals(f)) return false;
        if (mutantApple != null && p.equals(mutantApple.position)) return false;
        for (Bomb b : bombs) if (p.equals(b.position)) return false;
        for (Point s : snake) if (s.equals(p)) return false;
        return true;
    }

    private void handleGameOver() {
        gameOver = true;
        saveHighScore(snake.size() - 1);
    }

    private void checkBombExplosion(Bomb bomb) {
        if (bomb.isInRange(snake.get(0))) { handleGameOver(); return; }
        for (int i = 1; i < snake.size(); i++) {
            if (bomb.isInRange(snake.get(i))) {
                if (snake.size() > i) snake.subList(i, snake.size()).clear();
                break;
            }
        }
    }

    private void spawnBombs() {
        int count = random.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            List<Point> emptyTiles = getEmptyTiles();
            if (!emptyTiles.isEmpty()) bombs.add(new Bomb(emptyTiles.get(random.nextInt(emptyTiles.size()))));
        }
    }

    private void saveHighScore(int score) {
        List<Integer> highScores = getHighScores();
        highScores.add(score);
        Collections.sort(highScores, Collections.reverseOrder());
        if (highScores.size() > 5) highScores = highScores.subList(0, 5);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < highScores.size(); i++) editor.putInt("score_" + i, highScores.get(i));
        editor.apply();
    }

    private List<Integer> getHighScores() {
        List<Integer> scores = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int s = prefs.getInt("score_" + i, -1);
            if (s != -1) scores.add(s);
        }
        return scores;
    }

    private void drawSmooth() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            if (tileSize == 0) {
                numTilesY = 20;
                tileSize = canvas.getHeight() / numTilesY;
                numTilesX = canvas.getWidth() / tileSize;
                initGame();
                createBackgroundBitmap(canvas.getWidth(), canvas.getHeight());
                for (Leaf leaf : leaves) leaf.reset(canvas.getWidth(), canvas.getHeight());
                flowers.clear();
                for (int i = 0; i < 40; i++) flowers.add(new Flower(canvas.getWidth(), canvas.getHeight()));
                grassField.clear();
                for (int x = 0; x < numTilesX; x++) {
                    for (int y = 0; y < numTilesY; y++) {
                        grassField.add(new Grass(x * tileSize + random.nextFloat() * tileSize, (y + 1) * tileSize, random));
                    }
                }
            }

            if (backgroundBitmap != null) canvas.drawBitmap(backgroundBitmap, 0, 0, null);
            else canvas.drawColor(Color.BLACK);

            long currentTime = System.currentTimeMillis();
            float gpSize = tileSize / 15f;
            for (Grass grass : grassField) grass.draw(canvas, paint, gpSize, currentTime);
            for (Flower flower : flowers) flower.draw(canvas, paint, tileSize);
            for (Leaf leaf : leaves) leaf.draw(canvas, paint);

            if (!gameStarted) {
                drawMenu(canvas);
                holder.unlockCanvasAndPost(canvas);
                return;
            }

            for (int i = 0; i < snake.size(); i++) {
                float drawX, drawY;
                if (prevSnake.size() > i && !gameOver) {
                    Point current = snake.get(i);
                    Point prev = prevSnake.get(i);
                    float dx = current.x - prev.x;
                    float dy = current.y - prev.y;
                    if (Math.abs(dx) > 1) dx = (dx > 0) ? -1 : 1;
                    if (Math.abs(dy) > 1) dy = (dy > 0) ? -1 : 1;
                    drawX = prev.x + dx * moveProgress;
                    drawY = prev.y + dy * moveProgress;
                } else {
                    drawX = snake.get(i).x;
                    drawY = snake.get(i).y;
                }
                if (i == 0) drawPixelSnakeHeadSmooth(canvas, drawX, drawY);
                else drawPixelSnakeBodySmooth(canvas, drawX, drawY);
            }

            for (Point f : foods) drawPixelApple(canvas, f.x, f.y);
            if (mutantApple != null) mutantApple.draw(canvas, paint, tileSize);
            for (Bomb bomb : bombs) bomb.draw(canvas, paint, tileSize);
            
            drawPixelPortal(canvas, portal.position1.x, portal.position1.y, Color.rgb(0, 100, 255), Color.rgb(0, 255, 255));
            drawPixelPortal(canvas, portal.position2.x, portal.position2.y, Color.rgb(255, 100, 0), Color.rgb(255, 200, 0));
            
            drawPixelText(canvas, "Score: " + (snake.size() - 1), tileSize, tileSize * 0.5f, tileSize / 10f);

            if (gameOver) drawGameOverScreen(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawPixelText(Canvas canvas, String text, float x, float y, float pSize) {
        paint.setColor(Color.WHITE);
        float currentX = x;
        for (char c : text.toUpperCase().toCharArray()) {
            drawPixelChar(canvas, c, currentX, y, pSize);
            currentX += pSize * 6;
        }
    }

    private void drawPixelChar(Canvas canvas, char c, float x, float y, float pSize) {
        int[][] glyph = null;
        switch (c) {
            case 'S': glyph = new int[][]{{1,1,1},{1,0,0},{1,1,1},{0,0,1},{1,1,1}}; break;
            case 'C': glyph = new int[][]{{1,1,1},{1,0,0},{1,0,0},{1,0,0},{1,1,1}}; break;
            case 'O': glyph = new int[][]{{1,1,1},{1,0,1},{1,0,1},{1,0,1},{1,1,1}}; break;
            case 'R': glyph = new int[][]{{1,1,1},{1,0,1},{1,1,1},{1,1,0},{1,0,1}}; break;
            case 'E': glyph = new int[][]{{1,1,1},{1,0,0},{1,1,1},{1,0,0},{1,1,1}}; break;
            case ':': glyph = new int[][]{{0,0,0},{0,1,0},{0,0,0},{0,1,0},{0,0,0}}; break;
            case ' ': glyph = new int[][]{{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}}; break;
            case '0': glyph = new int[][]{{1,1,1},{1,0,1},{1,0,1},{1,0,1},{1,1,1}}; break;
            case '1': glyph = new int[][]{{0,1,0},{0,1,0},{0,1,0},{0,1,0},{0,1,0}}; break;
            case '2': glyph = new int[][]{{1,1,1},{0,0,1},{1,1,1},{1,0,0},{1,1,1}}; break;
            case '3': glyph = new int[][]{{1,1,1},{0,0,1},{1,1,1},{0,0,1},{1,1,1}}; break;
            case '4': glyph = new int[][]{{1,0,1},{1,0,1},{1,1,1},{0,0,1},{0,0,1}}; break;
            case '5': glyph = new int[][]{{1,1,1},{1,0,0},{1,1,1},{0,0,1},{1,1,1}}; break;
            case '6': glyph = new int[][]{{1,1,1},{1,0,0},{1,1,1},{1,0,1},{1,1,1}}; break;
            case '7': glyph = new int[][]{{1,1,1},{0,0,1},{0,0,1},{0,0,1},{0,0,1}}; break;
            case '8': glyph = new int[][]{{1,1,1},{1,0,1},{1,1,1},{1,0,1},{1,1,1}}; break;
            case '9': glyph = new int[][]{{1,1,1},{1,0,1},{1,1,1},{0,0,1},{1,1,1}}; break;
        }
        if (glyph != null) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 3; j++) {
                    if (glyph[i][j] == 1) canvas.drawRect(x + j * pSize, y + i * pSize, x + (j + 1) * pSize, y + (i + 1) * pSize, paint);
                }
            }
        }
    }

    private void drawMenu(Canvas canvas) {
        paint.setColor(Color.GREEN);
        paint.setTextSize(canvas.getHeight() / 6f);
        canvas.drawText("SNAKE GAME", canvas.getWidth() / 4f, canvas.getHeight() / 2f, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(canvas.getHeight() / 12f);
        canvas.drawText("Tap To Start", canvas.getWidth() / 3f, canvas.getHeight() / 1.5f, paint);
    }

    private void drawGameOverScreen(Canvas canvas) {
        paint.setColor(Color.argb(200, 0, 0, 0));
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        paint.setColor(Color.RED);
        paint.setTextSize(canvas.getHeight() / 6f);
        canvas.drawText("GAME OVER", canvas.getWidth() / 5f, canvas.getHeight() / 4f, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(canvas.getHeight() / 15f);
        canvas.drawText("Your Score: " + (snake.size() - 1), canvas.getWidth() / 3f, canvas.getHeight() / 4f + 100, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawText("LEADERBOARD", canvas.getWidth() / 3f, canvas.getHeight() / 2f, paint);
        List<Integer> highScores = getHighScores();
        paint.setColor(Color.WHITE);
        paint.setTextSize(canvas.getHeight() / 20f);
        for (int i = 0; i < highScores.size(); i++) canvas.drawText((i + 1) + ". " + highScores.get(i), canvas.getWidth() / 3f, canvas.getHeight() / 2f + (i + 1) * 60, paint);
        paint.setColor(Color.GREEN);
        paint.setTextSize(canvas.getHeight() / 15f);
        canvas.drawText("Tap to Restart", canvas.getWidth() / 3f, canvas.getHeight() - 100, paint);
    }

    private void drawPixelApple(Canvas canvas, int x, int y) {
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
        float pSize = (float) tileSize / 8;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (apple[i][j] != 0) {
                    switch (apple[i][j]) {
                        case 1: paint.setColor(Color.RED); break;
                        case 3: paint.setColor(Color.GREEN); break;
                        case 4: paint.setColor(Color.rgb(101, 67, 33)); break; 
                    }
                    canvas.drawRect(x * tileSize + j * pSize, y * tileSize + i * pSize, x * tileSize + (j + 1) * pSize, y * tileSize + (i + 1) * pSize, paint);
                }
            }
        }
    }

    private void drawPixelPortal(Canvas canvas, int x, int y, int coreColor, int ringColor) {
        float pSize = (float) tileSize / 8;
        float startX = x * tileSize;
        float startY = y * tileSize;
        long time = System.currentTimeMillis();
        float pulse = (float) (Math.sin(time / 150.0) * 0.1 + 0.9);
        paint.setColor(coreColor);
        paint.setAlpha(100);
        canvas.drawCircle(startX + tileSize / 2f, startY + tileSize / 2f, (tileSize / 1.5f) * pulse, paint);
        paint.setAlpha(255);
        int[][] ring = {{0,0,1,1,1,1,0,0},{0,1,0,0,0,0,1,0},{1,0,0,0,0,0,0,1},{1,0,0,0,0,0,0,1},{1,0,0,0,0,0,0,1},{1,0,0,0,0,0,0,1},{0,1,0,0,0,0,1,0},{0,0,1,1,1,1,0,0}};
        paint.setColor(ringColor);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (ring[i][j] == 1) canvas.drawRect(startX + j * pSize, startY + i * pSize, startX + (j + 1) * pSize, startY + (i + 1) * pSize, paint);
            }
        }
        paint.setColor(coreColor);
        float coreOffset = pSize * 2 * (1 - pulse);
        canvas.drawRect(startX + 2 * pSize + coreOffset, startY + 2 * pSize + coreOffset, startX + 6 * pSize - coreOffset, startY + 6 * pSize - coreOffset, paint);
        paint.setColor(Color.WHITE);
        canvas.drawRect(startX + 3.5f * pSize, startY + 3.5f * pSize, startX + 4.5f * pSize, startY + 4.5f * pSize, paint);
    }

    private void drawPixelSnakeHeadSmooth(Canvas canvas, float x, float y) {
        float pSize = (float) tileSize / 8;
        float headX = x * tileSize;
        float headY = y * tileSize;
        paint.setColor(Color.rgb(0, 200, 0));
        canvas.drawRect(headX, headY, headX + tileSize, headY + tileSize, paint);
        paint.setColor(Color.rgb(0, 150, 0));
        canvas.drawRect(headX, headY, headX + tileSize, headY + pSize, paint);
        canvas.drawRect(headX, headY + tileSize - pSize, headX + tileSize, headY + tileSize, paint);
        canvas.drawRect(headX, headY, headX + pSize, headY + tileSize, paint);
        canvas.drawRect(headX + tileSize - pSize, headY, headX + tileSize, headY + tileSize, paint);
        float mouthOpenness = (float) Math.sin(moveProgress * Math.PI) * 0.8f;
        paint.setColor(Color.WHITE);
        float eyeOffset = 2 * pSize;
        float eyeSize = 2 * pSize;
        Paint mouthPaint = new Paint();
        mouthPaint.setColor(Color.rgb(0, 100, 0));
        switch (direction) {
            case "RIGHT":
                if (mouthOpenness > 0.1f) canvas.drawRect(headX + 5 * pSize, headY + (4 - 2 * mouthOpenness) * pSize, headX + tileSize, headY + (4 + 2 * mouthOpenness) * pSize, mouthPaint);
                canvas.drawRect(headX + 4 * pSize, headY + eyeOffset, headX + 4 * pSize + eyeSize, headY + eyeOffset + eyeSize, paint);
                canvas.drawRect(headX + 4 * pSize, headY + tileSize - eyeOffset - eyeSize, headX + 4 * pSize + eyeSize, headY + tileSize - eyeOffset - eyeSize, paint);
                canvas.drawRect(headX + 5 * pSize, headY + eyeOffset + pSize/2, headX + 5 * pSize + pSize, headY + eyeOffset + pSize/2 + pSize, pupilPaint);
                canvas.drawRect(headX + 5 * pSize, headY + tileSize - eyeOffset - eyeSize + pSize/2, headX + 5 * pSize + pSize, headY + tileSize - eyeOffset - eyeSize + pSize/2 + pSize, pupilPaint);
                canvas.drawRect(headX + tileSize - pSize, headY + 3.5f * pSize, headX + tileSize + pSize + (mouthOpenness * pSize * 2), headY + 4.5f * pSize, tonguePaint);
                break;
            case "LEFT":
                if (mouthOpenness > 0.1f) canvas.drawRect(headX, headY + (4 - 2 * mouthOpenness) * pSize, headX + 3 * pSize, headY + (4 + 2 * mouthOpenness) * pSize, mouthPaint);
                canvas.drawRect(headX + 2 * pSize, headY + eyeOffset, headX + 2 * pSize + eyeSize, headY + eyeOffset + eyeSize, paint);
                canvas.drawRect(headX + 2 * pSize, headY + tileSize - eyeOffset - eyeSize, headX + 2 * pSize + eyeSize, headY + tileSize - eyeOffset - eyeSize, paint);
                canvas.drawRect(headX + 2 * pSize, headY + eyeOffset + pSize/2, headX + 2 * pSize + pSize, headY + eyeOffset + pSize/2 + pSize, pupilPaint);
                canvas.drawRect(headX + 2 * pSize, headY + tileSize - eyeOffset - eyeSize + pSize/2, headX + 2 * pSize + pSize, headY + tileSize - eyeOffset - eyeSize + pSize/2 + pSize, pupilPaint);
                canvas.drawRect(headX - pSize - (mouthOpenness * pSize * 2), headY + 3.5f * pSize, headX, headY + 4.5f * pSize, tonguePaint);
                break;
            case "UP":
                if (mouthOpenness > 0.1f) canvas.drawRect(headX + (4 - 2 * mouthOpenness) * pSize, headY, headX + (4 + 2 * mouthOpenness) * pSize, headY + 3 * pSize, mouthPaint);
                canvas.drawRect(headX + eyeOffset, headY + 2 * pSize, headX + eyeOffset + eyeSize, headY + 2 * pSize + eyeSize, paint);
                canvas.drawRect(headX + tileSize - eyeOffset - eyeSize, headY + 2 * pSize, headX + tileSize - eyeOffset - eyeSize + eyeSize, headY + 2 * pSize + eyeSize, paint);
                canvas.drawRect(headX + eyeOffset + pSize/2, headY + 2 * pSize, headX + eyeOffset + pSize/2 + pSize, headY + 2 * pSize + pSize, pupilPaint);
                canvas.drawRect(headX + tileSize - eyeOffset - eyeSize + pSize/2, headY + 2 * pSize, headX + tileSize - eyeOffset - eyeSize + pSize/2 + pSize, headY + 2 * pSize + pSize, pupilPaint);
                canvas.drawRect(headX + 3.5f * pSize, headY - pSize - (mouthOpenness * pSize * 2), headX + 4.5f * pSize, headY, tonguePaint);
                break;
            case "DOWN":
                if (mouthOpenness > 0.1f) canvas.drawRect(headX + (4 - 2 * mouthOpenness) * pSize, headY + 5 * pSize, headX + (4 + 2 * mouthOpenness) * pSize, headY + tileSize, mouthPaint);
                canvas.drawRect(headX + eyeOffset, headY + 4 * pSize, headX + eyeOffset + eyeSize, headY + 4 * pSize + eyeSize, paint);
                canvas.drawRect(headX + tileSize - eyeOffset - eyeSize, headY + 4 * pSize, headX + tileSize - eyeOffset - eyeSize + eyeSize, headY + 4 * pSize + eyeSize, paint);
                canvas.drawRect(headX + eyeOffset + pSize/2, headY + 5 * pSize, headX + eyeOffset + pSize/2 + pSize, headY + 5 * pSize + pSize, pupilPaint);
                canvas.drawRect(headX + tileSize - eyeOffset - eyeSize + pSize/2, headY + 5 * pSize, headX + tileSize - eyeOffset - eyeSize + pSize/2 + pSize, headY + 5 * pSize + pSize, pupilPaint);
                canvas.drawRect(headX + 3.5f * pSize, headY + tileSize, headX + 4.5f * pSize, headY + tileSize + pSize + (mouthOpenness * pSize * 2), tonguePaint);
                break;
        }
    }

    private void drawPixelSnakeBodySmooth(Canvas canvas, float x, float y) {
        float bX = x * tileSize;
        float bY = y * tileSize;
        float pSize = tileSize / 8f;
        paint.setColor(Color.rgb(0, 180, 0));
        canvas.drawRect(bX + 2, bY + 2, bX + tileSize - 2, bY + tileSize - 2, paint);
        paint.setColor(Color.rgb(0, 140, 0));
        canvas.drawRect(bX + 3 * pSize, bY + 3 * pSize, bX + 5 * pSize, bY + 5 * pSize, paint);
        canvas.drawRect(bX + pSize, bY + pSize, bX + 2 * pSize, bY + 2 * pSize, paint);
        canvas.drawRect(bX + 6 * pSize, bY + 6 * pSize, bX + 7 * pSize, bY + 7 * pSize, paint);
    }

    private void createBackgroundBitmap(int width, int height) {
        backgroundBitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
        Canvas bgCanvas = new Canvas(backgroundBitmap);
        Paint bgPaint = new Paint();
        for (int x = 0; x < numTilesX; x++) {
            for (int y = 0; y < numTilesY; y++) {
                if ((x + y) % 2 == 0) bgPaint.setColor(Color.rgb(5, 20, 5));
                else bgPaint.setColor(Color.rgb(8, 25, 8));
                bgCanvas.drawRect(x * tileSize, y * tileSize, (x + 1) * tileSize, (y + 1) * tileSize, bgPaint);
            }
        }
    }

    public void resume() {
        playing = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        playing = false;
        try { thread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!gameStarted) {
            gameStarted = true;
            return true;
        }
        if (gameOver) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) initGame();
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                float diffX = event.getX() - startX;
                float diffY = event.getY() - startY;
                if (Math.abs(diffX) > 50 || Math.abs(diffY) > 50) {
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (diffX > 0 && !direction.equals("LEFT")) nextDirection = "RIGHT";
                        else if (diffX < 0 && !direction.equals("RIGHT")) nextDirection = "LEFT";
                    } else {
                        if (diffY > 0 && !direction.equals("UP")) nextDirection = "DOWN";
                        else if (diffY < 0 && !direction.equals("DOWN")) nextDirection = "UP";
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        startX = event.getX();
                        startY = event.getY();
                    }
                }
                break;
        }
        return true;
    }
}
