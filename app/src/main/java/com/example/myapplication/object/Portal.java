package com.example.myapplication.object;

import android.graphics.Point;

public class Portal {

    public Point position1;
    public Point position2;

    public Portal(int x1, int y1, int x2, int y2) {
        this.position1 = new Point(x1, y1);
        this.position2 = new Point(x2, y2);
    }

    public Point teleport(Point position) {
        if (position.equals(position1)) {
            return new Point(position2.x, position2.y);
        } else if (position.equals(position2)) {
            return new Point(position1.x, position1.y);
        }
        return null;
    }
}
