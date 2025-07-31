package com.bit.ui;

import java.awt.*;
import java.util.Random;

class Shape {
    private static final int[][][] SHAPES = {
            {{1,1,1,1}}, // I
            {{1,1},{1,1}}, // O
            {{0,1,0},{1,1,1}}, // T
            {{1,0},{1,0},{1,1}}, // L
            {{0,1},{0,1},{1,1}}, // J
            {{1,1,0},{0,1,1}}, // S
            {{0,1,1},{1,1,0}}  // Z
    };
    public int[][] getShape() {
        return shape;
    }
    private static final Color[] COLORS = {
            new Color(0, 240, 240), // Cyan
            new Color(240, 240, 0), // Yellow
            new Color(160, 0, 240), // Purple
            new Color(240, 160, 0), // Orange
            new Color(0, 0, 240), // Blue
            new Color(0, 240, 0), // Green
            new Color(240, 0, 0)  // Red
    };

    int[][] shape;
    Color color;
    int x, y;
    private int rotation;
    private int type;
    public Shape() {
        Random rand = new Random();
        type = rand.nextInt(SHAPES.length);
        shape = SHAPES[type];
        color = COLORS[type];
        x = 5 - shape[0].length/2;
        y = -shape.length;
        rotation = 0;
    }
    public Shape(int[][] template) {
        // 根据模板初始化形状
        shape = template;
        color = new Color((int)(Math.random() * 256),
                (int)(Math.random() * 256),
                (int)(Math.random() * 256));
        x = 5 - shape[0].length / 2;
        y = -shape.length;
        rotation = 0;
    }

    public int getBlock(int i, int j) {
        return shape[i][j];
    }

    // 旋转逻辑
    public void rotate() {
        int[][] newShape = new int[shape[0].length][shape.length];
        for(int i = 0; i < shape.length; i++) {
            for(int j = 0; j < shape[0].length; j++) {
                newShape[j][shape.length-1-i] = shape[i][j];
            }
        }
        shape = newShape;
        rotation = (rotation + 1) % 4;
    }

    public void rotateBack() {
        for (int i = 0; i < 3; i++) rotate();
    }
}
