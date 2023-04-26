package com.example.cs_455_wwp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private TextView ballStats;
    private GameThread gameThread;

    Button syncButton;
    // initialize variables for GPS
    Button locationBtn;
    TextView gpsText;
    FusedLocationProviderClient fusedLocationProviderClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the surface view
        SurfaceView surfaceView = findViewById(R.id.gameView);
        surfaceView.getHolder().addCallback(this);

        this.ballStats = findViewById(R.id.ballStats);

        syncButton = findViewById(R.id.sync);
        // set up GPS variables
        locationBtn = findViewById(R.id.locationBtn);
        gpsText = findViewById(R.id.gpsStats);

        // initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        

        locationBtn.setOnClickListener(v -> {
            // check location permissions
            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // once permission granted
                getLocation();
            } else { // permission denied
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
        });

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
        private final HttpClient httpClient;

        public GameThread() {
            this.gameBall = new Ball();
            lastFrameTime = System.nanoTime();
            // initialize HTTPClient
            this.httpClient = HttpClientBuilder.create().build();
            syncButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getBallPosition();
                }
            });
        }

        public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        }

        public void setRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

        private void getBallPosition() {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.submit(() -> {
                HttpGet getBall = new HttpGet("http://192.168.50.223:8080/getPosition");
                StringBuilder ballString = new StringBuilder();
                try {
                    HttpResponse response = this.httpClient.execute(getBall);
                    InputStream inputStream = response.getEntity().getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        ballString.append(line);
                    }
                    gameBall.setVectors(ballString.toString());
                } catch (IOException e) {
                    System.out.println("IOexemption");
                }
            });

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

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            // initialize location
            Location location = task.getResult();
            if (location != null) {
                try {
                    // initialize geoCoder
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    // initialize address list
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1
                    );

                    // set TextView to GPS location info
                    gpsText.setText(String.format("Latitude: %s\nLongitude: %s\nAddress: %s",
                            addresses.get(0).getLatitude(),
                            addresses.get(0).getLongitude(),
                            addresses.get(0).getAddressLine(0))
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
}