package com.example.cs_455_wwp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private TextView ballStats;
    private GameThread gameThread;
    private boolean isRunning;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the surface view
        SurfaceView surfaceView = findViewById(R.id.gameView);
        surfaceView.getHolder().addCallback(this);

        this.ballStats = findViewById(R.id.ballStats);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Start the game thread when the surface is created
        gameThread = new GameThread();
        gameThread.setSurfaceHolder(holder);
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Handle surface changes if needed
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Stop the game thread when the surface is destroyed
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // Retry
            }
        }
    }

    private class GameThread extends Thread {
        private final Ball gameBall;
        private boolean isRunning;
        private long lastFrameTime;

        public GameThread() {
            this.gameBall = new Ball();
            lastFrameTime = System.nanoTime();
        }

        public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        }

        public void setRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

        @Override
        public void run() {
            while (isRunning) {
                long currentFrameTime = System.nanoTime();
                double deltaTime = (currentFrameTime - lastFrameTime) / 1e9;
                lastFrameTime = currentFrameTime;
                // Update the game state here
                // For example, calculate the new position based on the speed vector

                // Lock the canvas and draw the game objects here
                // For example, use surfaceHolder.lockCanvas() to get a Canvas object

                // Unlock the canvas and show the changes here
                // For example, use surfaceHolder.unlockCanvasAndPost(Canvas) to show the changes
                this.gameBall.updatePosition(deltaTime);
                // Update the speed text view here

                runOnUiThread(() -> ballStats.setText(gameBall.toString()));

                // Sleep for a short period of time to control the frame rate
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
        }
    }
}