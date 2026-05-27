package com.example.myapplication.object;

import android.graphics.Point;

import java.util.ArrayList;

public class Snake {

    public ArrayList<Point> body = new ArrayList<>();

    public String direction = "RIGHT";

    public Snake() {

        body.add(new Point(5,5));
    }

    public void move() {

        Point head = body.get(0);

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

        body.add(0, newHead);

        body.remove(body.size() - 1);
    }

    public Point getHead() {

        return body.get(0);
    }

    public void grow() {

        Point tail = body.get(body.size() - 1);

        body.add(new Point(tail.x, tail.y));
    }
}
