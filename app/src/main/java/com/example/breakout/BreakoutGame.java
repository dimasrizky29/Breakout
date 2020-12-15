package com.example.breakout;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class BreakoutGame extends Activity {
BreakoutView breakoutView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        breakoutView = new BreakoutView(this);

        setContentView(breakoutView);

    }

     class BreakoutView extends SurfaceView implements Runnable {

        Thread gameThread = null;
        SurfaceHolder ourHolder;

        volatile boolean playing;
        boolean paused = true;
Canvas canvas;
Paint paint;
long fps;
private  long timeThisFrame;
         int screenX;
         int screenY;
Paddle paddle;
Ball ball;
Brick[] bricks = new Brick[200];
int numBricks = 0;
SoundPool soundPool;
int beep1ID = -1;
int beep2ID = -1;
         int beep3ID = -1;
         int loseLifeID = -1;
        int explodeID =-1;

         int score=0;
         int lives =5;

public BreakoutView(Context context){
    super(context);
    ourHolder =getHolder();
    paint = new Paint();
    // Get a Display object to access screen details
    Display display = getWindowManager().getDefaultDisplay();
// Load the resolution into a Point object
    Point size = new Point();
    display.getSize(size);
    screenX = size.x;
    screenY = size.y;
    paddle=new Paddle(screenX,screenY);
    ball=new Ball(screenX,screenY);
    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

    try{
        AssetManager assetManager = context.getAssets();
        AssetFileDescriptor descriptor;
        descriptor = assetManager.openFd("beep1.ogg");
        beep1ID = soundPool.load(descriptor, 0);
        descriptor = assetManager.openFd("beep2.ogg");
        beep2ID = soundPool.load(descriptor, 0);
        descriptor = assetManager.openFd("beep3.ogg");
        beep3ID = soundPool.load(descriptor, 0);
        descriptor = assetManager.openFd("loseLife.ogg");
        loseLifeID = soundPool.load(descriptor, 0);
        descriptor = assetManager.openFd("explode.ogg");

        Log.e("error", "failed to load sound files");
    }
    catch(IOException e){
        Log.e("error","failed to load sound files");
    }
    createBricksAndRestart();
}

         public void createBricksAndRestart() {
         ball.reset(screenX,screenY);
             paddle.reset(screenX, screenY);
             int brickWidth = screenX / 8;
             int brickHeight = screenY / 10;
// Build a wall of bricks
             numBricks = 0;
             for (int column = 0; column < 8; column++) {
                 for (int row = 0; row < 4; row++) {
                     bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                     numBricks++;
                 }
             }
             if(lives ==0){
                 score=0;
                 lives=4;
             }
}

         @Override
         public void run() {
            while (playing){
                long startFrameTime = System.currentTimeMillis();
                if(!paused){
                    update();
                }
                draw();
                timeThisFrame = System.currentTimeMillis()-startFrameTime;
                if(timeThisFrame >= 1){
                    fps = 1000/timeThisFrame;
                }
            }
         }
         public void update(){
paddle.update(fps);
ball.update(fps);
for(int i=0; i<numBricks;i++){
    if(bricks[i].getVisibility()){
        if(RectF.intersects(bricks[i].getRect(), ball.getRect())){
            bricks[i].setInvisible();
            ball.reverseYVelocity();
            score=score +10;
            soundPool.play(explodeID, 1,1,0,0,1);
        }
    }
}
if(RectF.intersects(paddle.getRect(), ball.getRect())){
    ball.setRandomXVelocity();
    ball.reverseYVelocity();
    ball.clearObstacleY(paddle.getRect().top - 2);
    soundPool.play(beep1ID,1,1,0,0,1);
}
if(ball.getRect().bottom>screenY){
    ball.reverseYVelocity();
    ball.clearObstacleY(screenY-2);

    lives--;
    soundPool.play(loseLifeID,1,1,0,0,1);

    if (lives==0){
        paused = true;
        createBricksAndRestart();
    }
}
if(ball.getRect().top<0){
    ball.reverseYVelocity();
    ball.clearObstacleY(12);
    soundPool.play(beep3ID,1,1,0,0,1);
}

if(ball.getRect().left<0){
    ball.reverseXVelocity();
    ball.clearObstacleX(2);
    soundPool.play(beep3ID,1,1,0,0,1);
}
if(ball.getRect().right>screenX-10){
    ball.reverseXVelocity();
    ball.clearObstacleX(screenX-12);
    soundPool.play(beep3ID,1,1,0,0,1);
}
if(score==numBricks*10){
    paused=true;
    createBricksAndRestart();
}
if(paddle.getRect().right>screenX){
    paddle.setMovementState(paddle.STOPPED);
}
if(paddle.getRect().left<0){
    paddle.setMovementState(paddle.STOPPED);
}
         }
         public void draw(){
            if(ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.argb(255,25,228,182));
                paint.setColor(Color.argb(255,255,255,255));
                canvas.drawRect(paddle.getRect(),paint);
                canvas.drawRect(ball.getRect(), paint);
                paint.setColor(Color.argb(180, 120, 130, 125));

                for (int i = 0; i < numBricks; i++)
                {
                    if (bricks[i].getVisibility())
                    {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                // Choose the brush color for drawing
                paint.setColor(Color.argb(255, 255, 255, 255));

                // Draw the score
                paint.setTextSize(40);
                canvas.drawText("Score: " + score + "   Lives: " + lives, 1800, 50, paint);

                // Has the player cleared the screen?
                if (score == numBricks * 10)
                {
                    paint.setTextSize(90);
                    canvas.drawText("YOU HAVE WON!", 10, screenY / 2, paint);
                }

                // Has the player lost?
                if (lives <= 0)
                {
                    paint.setTextSize(90);
                    canvas.drawText("YOU HAVE LOST!", 10, screenY / 2, paint);
                }



                ourHolder.unlockCanvasAndPost(canvas);
            }
         }
         public void pause(){
        playing =false;
        try {
            gameThread.join();
        }catch (InterruptedException e){
            Log.e("Error:", "joining thread");
        }
         }

         public void resume(){
        playing =true;
        gameThread = new Thread(this);
        gameThread.start();
         }

         @Override
         public boolean onTouchEvent(MotionEvent motionevent) {
            switch (motionevent.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    paused = false;
                    if (motionevent.getX() > screenX / 2)
                    {
                        if(paddle.getRect().right < screenX)
                            paddle.setMovementState(paddle.RIGHT);
                    }
                    else
                    {
                        if(paddle.getRect().left > 0)
                            paddle.setMovementState(paddle.LEFT);
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    paddle.setMovementState(paddle.STOPPED);
                    break;
            }

            return true;
         }
     }
     @Override
     protected void onResume() {
         super.onResume();
         breakoutView.resume();
     }

    @Override
    protected void onPause() {
        super.onPause();
        breakoutView.pause();
    }
}