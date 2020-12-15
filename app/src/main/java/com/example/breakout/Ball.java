package com.example.breakout;

import android.graphics.RectF;

import java.util.Random;

public class Ball {
    RectF rect;
    float xVelocity;
    float yVelocity;
    float ballWidth = 10;
    float ballHeight = 10;
    public Ball(int screenX,int screenY){
        xVelocity = 200;
        yVelocity = -400;
// Place the ball in the centre of the screen at the bottom
// Make it a 10 pixel x 10 pixel square
        rect = new RectF();
    }

    public void reset(int x, int y) {
        rect.left = x / 2;
        rect.top = y - 20;
        rect.right = x / 2 + ballWidth;
        rect.bottom = y - 20 - ballHeight;
    }

    public void update(long fps) {
        rect.left = rect.left + (xVelocity / fps);
        rect.top = rect.top + (yVelocity / fps);
        rect.right = rect.left + ballWidth;
        rect.bottom = rect.top - ballHeight;
    }

    public RectF getRect() {
    return rect;
    }

    public void reverseYVelocity() {
        yVelocity = -yVelocity;
    }

    public void setRandomXVelocity() {
        Random generator = new Random();
        int answer = generator.nextInt(2);
        if(answer == 0){
            reverseXVelocity();
        }
    }

    public void clearObstacleY(float y) {
        rect.bottom = y;
        rect.top = y - ballHeight;
    }

    public void clearObstacleX(int x) {
        rect.left = x;
        rect.right = x + ballWidth;
    }

    public void reverseXVelocity() {
        xVelocity = - xVelocity;
    }
}
