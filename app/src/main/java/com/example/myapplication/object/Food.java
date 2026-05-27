package com.example.myapplication.object;



import android.graphics.Point;

import java.util.Random;

public class Food {

    public Point position;

    Random random = new Random();

    public Food() {

        respawn();
    }

    public void respawn() {

        position = new Point(
                random.nextInt(20),
                random.nextInt(20)
        );
    }
}