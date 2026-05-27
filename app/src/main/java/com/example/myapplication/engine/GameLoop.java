package com.example.myapplication.engine;



public class GameLoop {

    public boolean running = true;

    public void sleep() {

        try {

            Thread.sleep(150);

        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }
}