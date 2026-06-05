package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.PixelFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import com.example.myapplication.object.Portal;
import com.example.myapplication.object.Leaf;
import com.example.myapplication.object.Flower;
import com.example.myapplication.object.MutantApple;
import com.example.myapplication.object.Bomb;
import com.example.myapplication.object.Grass;
import com.example.myapplication.object.Snowflake;
import com.example.myapplication.object.Gift;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import androidx.core.graphics.ColorUtils;

public class GameView extends SurfaceView implements Runnable {

    Thread thread;
    float startX, startY;
    boolean playing = true;
    boolean gameStarted = false;
    boolean gameOver = false;
    private android.graphics.RectF settingsButton;

    private float settingsLeft;
    private float settingsTop;
    private float settingsRight;
    private float settingsBottom;
    private void showSettingsDialog() {

        String[] options = {
                "Swipe",
                "Hand Gesture"
        };

        int checkedItem =
                (controlMode == ControlMode.SWIPE)
                        ? 0 : 1;

        new AlertDialog.Builder(getContext())
                .setTitle("Control Mode")
                .setSingleChoiceItems(
                        options,
                        checkedItem,
                        (dialog, which) -> {

                            if (which == 0) {

                                controlMode =
                                        ControlMode.SWIPE;

                            } else {

                                controlMode =
                                        ControlMode.HAND;
                            }
                        })
                .setPositiveButton(
                        "Save",
                        (dialog, which) -> {

                            SharedPreferences prefs =
                                    getContext()
                                            .getSharedPreferences(
                                                    "SnakeGamePrefs",
                                                    Context.MODE_PRIVATE);

                            prefs.edit()
                                    .putString(
                                            "control_mode",
                                            controlMode.name())
                                    .apply();
                        })
                .setNegativeButton(
                        "Cancel",
                        null)
                .show();
    }

    SurfaceHolder holder;
    Paint paint;
    Paint pupilPaint;
    Paint tonguePaint;
    android.graphics.Bitmap backgroundBitmap;

    int tileSize;
    int numTilesX, numTilesY;

    List<Point> snake = new CopyOnWriteArrayList<>();
    List<Point> prevSnake = new CopyOnWriteArrayList<>();
    float moveProgress = 0f;
    float moveSpeed = 0.12f; // Tăng lại một chút để bù trừ overhead rendering

    String direction = "RIGHT";
    String nextDirection = "RIGHT";
    public enum ControlMode {
        SWIPE,
        HAND
    }


    private ControlMode controlMode =
            ControlMode.SWIPE;
    private float handX = -1, handY = -1;

    public void setControlMode(ControlMode mode) {
        this.controlMode = mode;
    }

    public ControlMode getControlMode() {
        return controlMode;
    }

    public void setHandPosition(float x, float y) {
        this.handX = x;
        this.handY = y;
    }
    public void setHandDirection(String handDirection){

        switch(handDirection){

            case "UP":
                if(!direction.equals("DOWN"))
                    nextDirection = "UP";
                break;

            case "DOWN":
                if(!direction.equals("UP"))
                    nextDirection = "DOWN";
                break;

            case "LEFT":
                if(!direction.equals("RIGHT"))
                    nextDirection = "LEFT";
                break;

            case "RIGHT":
                if(!direction.equals("LEFT"))
                    nextDirection = "RIGHT";
                break;
        }
    }
    List<Point> foods = new CopyOnWriteArrayList<>();
    MutantApple mutantApple;
    List<Bomb> bombs = new CopyOnWriteArrayList<>();
    Portal portal = new Portal(2, 2, 17, 17);

    private SharedPreferences prefs;
    private Random random = new Random();
    private List<Leaf> leaves = new CopyOnWriteArrayList<>();
    private List<Flower> flowers = new CopyOnWriteArrayList<>();
    private List<Grass> grassField = new CopyOnWriteArrayList<>();
    private List<Snowflake> snowflakes = new CopyOnWriteArrayList<>();
    private List<Point> iceLakes = new CopyOnWriteArrayList<>();
    private List<Point> waterTiles = new CopyOnWriteArrayList<>();
    private List<Gift> activeGifts = new CopyOnWriteArrayList<>();
    
    private ToneGenerator toneGenerator;
    private MediaPlayer bgmPlayer;
    
    private float santaX = -200;
    private float santaY = 100;
    private boolean isSantaFlying = false;
    private long invincibilityEndTime = 0;
    
    private float winterProgress = 0f; // 0: Mùa hè, 1: Mùa đông
    private int lastStage = 0; // 0: Summer, 1: Winter
    private boolean isWinter = false;
    private long lastUpdateTime = 0;

    public GameView(Context context) {
        super(context);
        setZOrderOnTop(true);
        holder = getHolder();
        holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT);
        holder = getHolder();
        paint = new Paint();
        paint.setAntiAlias(true);
        pupilPaint = new Paint();
        pupilPaint.setColor(Color.BLACK);
        tonguePaint = new Paint();
        tonguePaint.setColor(Color.RED);
        
        prefs = context.getSharedPreferences("SnakeGamePrefs", Context.MODE_PRIVATE);
        String savedMode =
                prefs.getString(
                        "control_mode",
                        "SWIPE");

        controlMode =
                ControlMode.valueOf(savedMode);
        
        // Khởi tạo trình tạo âm thanh Retro (không cần file nhạc)
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        
        // Khởi tạo Nhạc nền
        bgmPlayer = MediaPlayer.create(context, R.raw.bgm_music);
        if (bgmPlayer != null) {
            bgmPlayer.setLooping(true); // Phát lặp lại
            bgmPlayer.setVolume(0.5f, 0.5f); // Để âm lượng 50% để không át tiếng bíp
        }

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
        waterTiles.clear();
        activeGifts.clear();
        isSantaFlying = false;
        invincibilityEndTime = 0;
        
        if (numTilesX > 0 && numTilesY > 0) {
            spawnFood();
            spawnPortal();
            
            // Khởi tạo lá rơi rải rác khắp màn hình
            leaves.clear();
            for (int i = 0; i < 30; i++) {
                Leaf leaf = new Leaf(getWidth(), getHeight());
                // Cho lá xuất hiện ngẫu nhiên trên màn hình thay vì chỉ ở trên cùng
                leaf.y = random.nextFloat() * getHeight(); 
                leaves.add(leaf);
            }
            
            // Khởi tạo hoa
            flowers.clear();
            for (int i = 0; i < 40; i++) {
                flowers.add(new Flower(getWidth(), getHeight()));
            }
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
        
        float speedFactor = deltaTime / 16.67f;

        // Tự động chuyển đổi mùa luân phiên mỗi 10 điểm
        int score = snake.size() - 1;
        int currentStage = (score / 10) % 2;
        isWinter = (currentStage == 1);
        
        // Dọn dẹp ô nước và hồ băng khi chuyển từ mùa đông sang hè
        if (currentStage == 0 && lastStage == 1) {
            waterTiles.clear();
            iceLakes.clear();
            activeGifts.clear();
        }
        lastStage = currentStage;

        // Cập nhật mùa đông (chuyển đổi trong đúng 5 giây)
        float transitionStep = (float) deltaTime / 5000f; 
        if (isWinter && winterProgress < 1.0f) {
            winterProgress += transitionStep;
            if (winterProgress > 1.0f) winterProgress = 1.0f;
        } else if (!isWinter && winterProgress > 0f) {
            winterProgress -= transitionStep;
            if (winterProgress < 0f) winterProgress = 0f;
        }

        if (winterProgress < 1.0f) {
            for (Leaf leaf : leaves) leaf.update(getWidth(), getHeight());
            for (Flower flower : flowers) flower.update();
        }
        
        if (winterProgress > 0.0f) {
            if (snowflakes.isEmpty()) {
                for (int i = 0; i < 60; i++) snowflakes.add(new Snowflake(getWidth(), getHeight()));
            }
            for (Snowflake snow : snowflakes) snow.update(getWidth(), getHeight());
            
            // Logic Santa Claus
            if (winterProgress > 0.8f && !isSantaFlying && random.nextInt(1000) < 2) {
                isSantaFlying = true;
                santaX = -200;
                santaY = 50 + random.nextFloat() * 150;
            }
            
            if (isSantaFlying) {
                santaX += 5 * speedFactor;
                // Thả quà ngẫu nhiên khi bay qua
                if (random.nextInt(100) < 2) {
                    int gx = (int) (santaX / tileSize);
                    int gy = (int) (santaY / tileSize);
                    if (gx >= 0 && gx < numTilesX && gy >= 0 && gy < numTilesY) {
                        activeGifts.add(new Gift(gx, gy));
                    }
                }
                if (santaX > getWidth() + 200) isSantaFlying = false;
            }
        }

        if (mutantApple != null) {
            mutantApple.update(snake.get(0), numTilesX, numTilesY, snake, portal);
        }

        // Cập nhật Bomb
        List<Bomb> toRemove = new ArrayList<>();
        for (Bomb bomb : bombs) {
            bomb.update();
            if (bomb.exploded) {
                if (!bomb.soundPlayed) {
                    // m thanh bom nổ Retro
                    toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 300);
                    bomb.soundPlayed = true;
                }
                checkBombExplosion(bomb);
            }
            if (bomb.finished) toRemove.add(bomb);
        }
        bombs.removeAll(toRemove);

        if (random.nextInt(1000) < 5 && bombs.size() < 3) spawnBombs();

        // Di chuyển dựa trên thời gian thực (Delta Time) thay vì số khung hình
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

        // Kiểm tra tự đâm vào thân (Bỏ qua nếu đang bất tử)
        if (System.currentTimeMillis() > invincibilityEndTime) {
            for (int i = 1; i < snake.size(); i++) {
                if (newHead.equals(snake.get(i))) {
                    handleGameOver();
                    return;
                }
            }
        }

        snake.add(0, newHead);

        // Kiểm tra va chạm ô nước
        if (System.currentTimeMillis() > invincibilityEndTime) {
            for (Point water : waterTiles) {
                if (newHead.equals(water)) {
                    handleGameOver();
                    return;
                }
            }
        }

        // Kiểm tra ăn quà
        Gift eatenGift = null;
        for (Gift g : activeGifts) {
            if (newHead.equals(g.position)) {
                eatenGift = g;
                break;
            }
        }
        if (eatenGift != null) {
            activeGifts.remove(eatenGift);
            invincibilityEndTime = System.currentTimeMillis() + 4000;
            // Âm thanh Power Up cao vút
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_S, 200);
        }

        Point eatenFood = null;
        for (Point f : foods) {
            if (newHead.equals(f)) { eatenFood = f; break; }
        }

        if (eatenFood != null) {
            // Âm thanh ăn táo "chíp" ngắn gọn
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
            foods.remove(eatenFood);
            if (foods.isEmpty() && mutantApple == null) spawnFood();
            spawnPortal();
            if (isWinter) spawnIceLake();
        } else if (mutantApple != null && newHead.equals(mutantApple.position)) {
            // Âm thanh ăn táo đột biến
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 120);
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

    private void spawnIceLake() {
        List<Point> emptyTiles = getEmptyTiles();
        if (emptyTiles.size() < 4) return;
        
        // Tạo hồ băng kích thước 2x2 ngẫu nhiên
        Point base = emptyTiles.get(random.nextInt(emptyTiles.size()));
        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 0; dy < 2; dy++) {
                Point p = new Point(base.x + dx, base.y + dy);
                if (p.x < numTilesX && p.y < numTilesY) {
                    boolean occupied = false;
                    for (Point s : snake) if (s.equals(p)) occupied = true;
                    if (!occupied) iceLakes.add(p);
                }
            }
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
        for (Point il : iceLakes) if (p.equals(il)) return false;
        for (Point s : snake) if (s.equals(p)) return false;
        return true;
    }

    private void handleGameOver() {
        gameOver = true;
        saveHighScore(snake.size() - 1);
    }

    private void checkBombExplosion(Bomb bomb) {
        if (System.currentTimeMillis() > invincibilityEndTime) {
            if (bomb.isInRange(snake.get(0))) { handleGameOver(); return; }
            for (int i = 1; i < snake.size(); i++) {
                if (bomb.isInRange(snake.get(i))) {
                    if (snake.size() > i) snake.subList(i, snake.size()).clear();
                    break;
                }
            }
        }
        
        // Tạo ô nước trên nền băng
        if (winterProgress > 0.8f) {
            boolean iceBroken = false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Point wp = new Point(bomb.position.x + dx, bomb.position.y + dy);
                    if (wp.x >= 0 && wp.x < numTilesX && wp.y >= 0 && wp.y < numTilesY) {
                        if (!waterTiles.contains(wp)) {
                            waterTiles.add(wp);
                            iceBroken = true;
                        }
                    }
                }
            }
            if (iceBroken) {
                // m thanh băng vỡ "rắc rắc"
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_D, 150);
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
                createBackgroundBitmap(canvas.getWidth(), canvas.getHeight());
                initGame(); // Gọi initGame sau khi đã có tileSize và numTiles

                grassField.clear();
                for (int x = 0; x < numTilesX; x++) {
                    for (int y = 0; y < numTilesY; y++) {
                        grassField.add(new Grass(x * tileSize + random.nextFloat() * tileSize, (y + 1) * tileSize, random));
                    }
                }
            }

            if (backgroundBitmap != null) canvas.drawBitmap(backgroundBitmap, 0, 0, null);
            else canvas.drawColor(Color.BLACK);
            
            // Phủ băng lên nền (Chuyển dần sang xanh băng giá có vân lóa sáng)
            if (winterProgress > 0) {
                // Lớp băng cơ bản (Xanh đậm hơn một chút để đỡ đau mắt)
                paint.setColor(Color.rgb(100, 180, 240)); 
                paint.setAlpha((int) (winterProgress * 255));
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

                // Thêm các vân băng lóa sáng (Ice glints/reflections)
                if (winterProgress > 0.3f) {
                    float glintSize = tileSize / 15f;
                    for (int i = 0; i < 40; i++) {
                        Random r = new Random(i * 777);
                        float tx = r.nextFloat() * getWidth();
                        float ty = r.nextFloat() * getHeight();
                        float length = tileSize * (0.5f + r.nextFloat() * 1.5f);
                        
                        // Tia sáng trắng lấp lánh
                        paint.setColor(Color.WHITE);
                        paint.setAlpha((int) (winterProgress * 180 * r.nextFloat()));
                        paint.setStrokeWidth(glintSize / 2f);
                        canvas.drawLine(tx, ty, tx + length, ty - length * 0.3f, paint);
                        
                        // Đốm sáng nhỏ (Sparkles)
                        if (r.nextFloat() > 0.7f) {
                            canvas.drawRect(tx, ty, tx + glintSize, ty + glintSize, paint);
                        }
                    }
                    paint.setStrokeWidth(1f); // Reset stroke width
                }
                paint.setAlpha(255);
            }

            long currentTime = System.currentTimeMillis();
            
            // Vẽ ô nước (Hố băng do bom nổ)
            if (!waterTiles.isEmpty()) {
                paint.setColor(Color.rgb(0, 50, 150)); // Xanh thẫm màu nước
                for (Point w : waterTiles) {
                    canvas.drawRect(w.x * tileSize, w.y * tileSize, (w.x + 1) * tileSize, (w.y + 1) * tileSize, paint);
                    // Hiệu ứng gợn sóng pixel
                    paint.setColor(Color.rgb(50, 100, 200));
                    canvas.drawRect(w.x * tileSize + 2, w.y * tileSize + 2, w.x * tileSize + 6, w.y * tileSize + 4, paint);
                    paint.setColor(Color.rgb(0, 50, 150));
                }
            }

            // Vẽ hồ băng trơn trượt (Đậm hơn nền băng một chút để phân biệt)
            if (winterProgress > 0.3f) {
                paint.setColor(Color.rgb(80, 160, 220)); 
                paint.setAlpha((int) (winterProgress * 180));
                for (Point il : iceLakes) {
                    float left = il.x * tileSize + 1;
                    float top = il.y * tileSize + 1;
                    float right = (il.x + 1) * tileSize - 1;
                    float bottom = (il.y + 1) * tileSize - 1;
                    canvas.drawRect(left, top, right, bottom, paint);
                    
                    // Vẽ vết nứt băng sắc nét
                    paint.setColor(Color.WHITE);
                    paint.setAlpha(120);
                    canvas.drawLine(left, top, right, bottom, paint);
                    canvas.drawLine(left + tileSize/2f, top, left, bottom, paint);
                    paint.setColor(Color.rgb(80, 160, 220));
                    paint.setAlpha((int) (winterProgress * 180));
                }
                paint.setAlpha(255);
            }

            float gpSize = tileSize / 15f;
            // Cỏ phủ tuyết (Thay vì biến mất, nó chuyển sang trắng)
            for (Grass grass : grassField) {
                grass.draw(canvas, paint, gpSize, currentTime, winterProgress);
            }

            // Vẽ hoa (Chuyển dần sang trắng)
            for (Flower flower : flowers) {
                flower.draw(canvas, paint, tileSize, winterProgress);
            }
            
            // Vẽ lá hoặc tuyết rơi
            if (winterProgress < 0.8f) {
                int originalAlpha = paint.getAlpha();
                paint.setAlpha((int) (255 * (1 - winterProgress)));
                for (Leaf leaf : leaves) leaf.draw(canvas, paint, tileSize);
                paint.setAlpha(originalAlpha);
            }
            
            if (winterProgress > 0.2f) {
                int originalAlpha = paint.getAlpha();
                paint.setAlpha((int) (255 * winterProgress));
                for (Snowflake snow : snowflakes) snow.draw(canvas, paint, tileSize / 20f);
                paint.setAlpha(originalAlpha);
            }
            
            // Vẽ quà
            for (Gift gift : activeGifts) gift.draw(canvas, paint, tileSize);

            if (!gameStarted) {
                drawMenu(canvas);
                holder.unlockCanvasAndPost(canvas);
                return;
            }

            boolean isInvincible = System.currentTimeMillis() < invincibilityEndTime;
            
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
                
                int snakeColor;
                if (isInvincible) {
                    // Hiệu ứng 7 màu cầu vồng khi bất tử (Dùng float để tránh lỗi chia nguyên)
                    snakeColor = Color.HSVToColor(new float[]{(System.currentTimeMillis() / 2.0f + i * 20.0f) % 360, 0.8f, 1.0f});
                } else {
                    snakeColor = (i == 0) ? Color.rgb(0, 200, 0) : Color.rgb(0, 180, 0);
                }

                if (i == 0) drawPixelSnakeHeadSmooth(canvas, drawX, drawY, snakeColor);
                else drawPixelSnakeBodySmooth(canvas, drawX, drawY, snakeColor);
            }

            for (Point f : foods) drawPixelApple(canvas, f.x, f.y);
            if (mutantApple != null) mutantApple.draw(canvas, paint, tileSize);
            for (Bomb bomb : bombs) bomb.draw(canvas, paint, tileSize);
            
            drawPixelPortal(canvas, portal.position1.x, portal.position1.y, Color.rgb(0, 100, 255), Color.rgb(0, 255, 255));
            drawPixelPortal(canvas, portal.position2.x, portal.position2.y, Color.rgb(255, 100, 0), Color.rgb(255, 200, 0));
            
            // Vẽ Santa Claus
            if (isSantaFlying) {
                drawSanta(canvas, santaX, santaY, tileSize);
            }
            
            drawPixelText(canvas, "Score: " + (snake.size() - 1), tileSize, tileSize * 0.5f, tileSize / 10f);
            if (isInvincible) {
                drawPixelText(canvas, "POWER UP!", tileSize, tileSize * 1.5f, tileSize / 15f);
            }

            if (gameOver) drawGameOverScreen(canvas);

            // Vẽ Joystick ảo phản hồi (Visual Feedback)
            if (controlMode == ControlMode.HAND && handX != -1) {
                float panelSize = canvas.getWidth() / 5f;
                float margin = 30;
                float bx = canvas.getWidth() - panelSize - margin;
                float by = margin;

                // Vẽ khung nền mờ
                paint.setColor(Color.WHITE);
                paint.setAlpha(40);
                canvas.drawRect(bx, by, bx + panelSize, by + panelSize, paint);
                
                // Vẽ tâm đỏ
                paint.setColor(Color.RED);
                paint.setAlpha(120);
                canvas.drawCircle(bx + panelSize/2f, by + panelSize/2f, 10, paint);

                // Vẽ vị trí tay (Chấm xanh)
                paint.setColor(Color.GREEN);
                paint.setAlpha(255);
                canvas.drawCircle(bx + handX * panelSize, by + handY * panelSize, 20, paint);
            }

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawSanta(Canvas canvas, float x, float y, int tileSize) {
        float pSize = tileSize / 10f;
        // Ông già Noel đơn giản (Pixel Art)
        // Xe tuần lộc
        paint.setColor(Color.rgb(139, 69, 19));
        canvas.drawRect(x, y + 2 * pSize, x + 6 * pSize, y + 4 * pSize, paint);
        // Ông già Noel (Áo đỏ)
        paint.setColor(Color.RED);
        canvas.drawRect(x + 2 * pSize, y, x + 4 * pSize, y + 2 * pSize, paint);
        // Râu trắng
        paint.setColor(Color.WHITE);
        canvas.drawRect(x + 2 * pSize, y + pSize, x + 3 * pSize, y + 2 * pSize, paint);
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
        paint.setTextSize(canvas.getHeight() / 8f);
        canvas.drawText("SNAKE GAME", canvas.getWidth() / 4.5f, canvas.getHeight() / 2.5f, paint);
        
        paint.setColor(Color.WHITE);
        paint.setTextSize(canvas.getHeight() / 15f);
        // Hiệu ứng chữ nhấp nháy pixel
        if ((System.currentTimeMillis() / 500) % 2 == 0) {
            canvas.drawText("Tap To Start", canvas.getWidth() / 3.2f, canvas.getHeight() / 1.8f, paint);
        }

        float btnSize = canvas.getWidth() / 10f;
        settingsLeft = canvas.getWidth() - btnSize - 40;
        settingsTop = 40;
        settingsRight = settingsLeft + btnSize;
        settingsBottom = settingsTop + btnSize;
        settingsButton = new RectF(settingsLeft, settingsTop, settingsRight, settingsBottom);

        // --- VẼ NÚT SETTINGS PHONG CÁCH PIXEL ---
        float p = btnSize / 10f; // Kích thước 1 pixel của nút

        // 1. Bóng đổ phía dưới (Dark Shadow)
        paint.setColor(Color.BLACK);
        canvas.drawRect(settingsLeft + p, settingsTop + p, settingsRight + p, settingsBottom + p, paint);

        // 2. Thân nút (Main Body)
        paint.setColor(Color.rgb(100, 100, 100)); // Màu xám đậm
        canvas.drawRect(settingsLeft, settingsTop, settingsRight, settingsBottom, paint);

        // 3. Viền sáng phía trên (Top Highlight)
        paint.setColor(Color.rgb(180, 180, 180));
        canvas.drawRect(settingsLeft, settingsTop, settingsRight, settingsTop + p, paint);
        canvas.drawRect(settingsLeft, settingsTop, settingsLeft + p, settingsBottom, paint);

        // 4. Viền tối bên phải/dưới (Bottom/Right Shadow)
        paint.setColor(Color.rgb(60, 60, 60));
        canvas.drawRect(settingsRight - p, settingsTop + p, settingsRight, settingsBottom, paint);
        canvas.drawRect(settingsLeft + p, settingsBottom - p, settingsRight, settingsBottom, paint);

        // 5. Vẽ Icon Bánh răng Pixel ở trung tâm
        drawPixelGear(canvas, settingsLeft + 2 * p, settingsTop + 2 * p, btnSize - 4 * p);
    }

    private void drawPixelGear(Canvas canvas, float x, float y, float size) {
        float p = size / 10f;
        paint.setColor(Color.rgb(220, 220, 220)); // Màu trắng bạc

        // Ma trận bánh răng 10x10
        int[][] gear = {
            {0,0,0,1,1,1,1,0,0,0},
            {0,1,1,1,1,1,1,1,1,0},
            {0,1,1,0,0,0,0,1,1,0},
            {1,1,0,0,1,1,0,0,1,1},
            {1,1,0,1,1,1,1,0,1,1},
            {1,1,0,1,1,1,1,0,1,1},
            {1,1,0,0,1,1,0,0,1,1},
            {0,1,1,0,0,0,0,1,1,0},
            {0,1,1,1,1,1,1,1,1,0},
            {0,0,0,1,1,1,1,0,0,0}
        };

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (gear[i][j] == 1) {
                    canvas.drawRect(x + j * p, y + i * p, x + (j + 1) * p, y + (i + 1) * p, paint);
                }
            }
        }
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

    private void drawPixelSnakeHeadSmooth(Canvas canvas, float x, float y, int headColor) {
        float pSize = (float) tileSize / 8;
        float headX = x * tileSize;
        float headY = y * tileSize;
        
        paint.setColor(headColor);
        canvas.drawRect(headX, headY, headX + tileSize, headY + tileSize, paint);

        // Vẽ viền đậm hơn dựa trên màu đầu
        float[] hsv = new float[3];
        Color.colorToHSV(headColor, hsv);
        hsv[2] *= 0.7f; // Làm tối đi 30% cho viền
        paint.setColor(Color.HSVToColor(hsv));
        
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

    private void drawPixelSnakeBodySmooth(Canvas canvas, float x, float y, int bodyColor) {
        float bX = x * tileSize;
        float bY = y * tileSize;
        float pSize = tileSize / 8f;
        
        paint.setColor(bodyColor);
        canvas.drawRect(bX + 2, bY + 2, bX + tileSize - 2, bY + tileSize - 2, paint);
        
        // Vân vảy đậm màu hơn
        float[] hsv = new float[3];
        Color.colorToHSV(bodyColor, hsv);
        hsv[2] *= 0.8f; 
        paint.setColor(Color.HSVToColor(hsv));

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
        if (bgmPlayer != null) bgmPlayer.start(); // Phát nhạc khi quay lại game
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        playing = false;
        if (bgmPlayer != null && bgmPlayer.isPlaying()) bgmPlayer.pause(); // Tạm dừng nhạc khi ẩn game
        try { thread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!gameStarted) {

            if(event.getAction()
                    == MotionEvent.ACTION_DOWN){

                float x = event.getX();
                float y = event.getY();

                if(settingsButton != null &&
                        settingsButton.contains(x,y)){

                    showSettingsDialog();

                    return true;
                }
            }

            gameStarted = true;
            return true;
        }
        if (gameOver) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isWinter = false;
                winterProgress = 0f;
                iceLakes.clear();
                waterTiles.clear();
                activeGifts.clear();
                initGame();
            }
            return true;
        }
        if (controlMode == ControlMode.HAND) {
            return true;
        }
        
        // Kiểm tra xem đầu rắn có đang ở trên hồ băng không
        if (!snake.isEmpty() && winterProgress > 0.5f) {
            Point head = snake.get(0);
            for (Point il : iceLakes) {
                if (head.equals(il)) {
                    // m thanh trượt trên băng
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 50);
                    return true; 
                }
            }
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
