package com.example.cs_455_wwp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.cs_455_wwp.databinding.ActivityGameBinding;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.ar.sceneform.math.Vector3;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.entity.StringEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.HttpClientBuilder;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Instant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    // view binding
    private ActivityGameBinding binding;

    private FirebaseAuth firebaseAuth;

    // initialize variables for GPS
    Button locationBtn;
    Button mapButton;
    Button hitButton;
    Button syncButton;
    TextView gpsText;
    TextView hitText;
    FusedLocationProviderClient fusedLocationProviderClient;

    TextView scoreTxt;
    TextView teamTxt;

    private TextView ballStats;
    private GameThread gameThread;
    private boolean isRunning;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        // handle logout
        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            checkUser();
        });

        AndroidThreeTen.init(this);

        // set up GPS variables
        locationBtn = findViewById(R.id.locationBtn);
        gpsText = findViewById(R.id.gpsStats);
        hitButton = findViewById(R.id.hit);
        hitText = findViewById(R.id.hitText);
        syncButton = findViewById(R.id.sync);

        // initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationBtn.setOnClickListener(v -> {
            // check location permissions
            if (ActivityCompat.checkSelfPermission(GameActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                // once permission granted
                getLocation();
            } else { // permission denied
                ActivityCompat.requestPermissions(GameActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
        });

        //add button to send the user to the location of the ball
        mapButton = findViewById(R.id.mapButton);

        //add code when map button is pressed
        mapButton.setOnClickListener(v -> {
            String finalPosition = gameThread.gameBall.finalPositionToString();
            sendLocation(finalPosition);
        });

        // Set up the surface view
        SurfaceView surfaceView = findViewById(R.id.gameView);
        surfaceView.getHolder().addCallback(this);

        this.ballStats = findViewById(R.id.ballStats);

        scoreTxt = findViewById(R.id.scoreTxt);

        teamTxt = findViewById(R.id.teamTxt);
    }

    private void checkUser() {
        // get the current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // if the user is not logged in
        if (firebaseUser == null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else { // user is logged in
            // get the user's info
            String email = firebaseUser.getEmail();

            // set the text to the user's email
            binding.emailTxt.setText(email);
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
                    Geocoder geocoder = new Geocoder(GameActivity.this, Locale.getDefault());
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

    private void sendLocation(String coords) {
        //make an intent to send location to a map
        //modified from docs; https://developer.android.com/training/basics/intents/sending
        android.net.Uri sendLocation = android.net.Uri.parse("geo:" + coords + "?z=14");
        // Uri location = Uri.parse("geo:37.422219,-122.08364?z=14"); // z param is zoom level
        android.content.Intent mapIntent = new android.content.Intent(Intent.ACTION_VIEW, sendLocation);

        //send user into maps
        startActivity(mapIntent);
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
        private String playerTeam;

        public GameThread() {
            this.playerTeam = getIntent().getStringExtra("team");
            this.gameBall = new Ball();
            lastFrameTime = System.nanoTime();
            // initialize HTTPClient
            this.httpClient = HttpClientBuilder.create().build();
            syncButton.setOnClickListener(v -> sync());
            getScore();
            hitButton.setOnClickListener(v -> sendLocationPing());
            getBallPosition();
            sendLocationPing();
            sendLocationPing();
            teamTxt.setText("Team " + this.playerTeam);
        }

        public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        }

        public void setRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

        public void sync(){
            getBallPosition();
            getScore();
        }


        private void getBallPosition() {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.submit(() -> {
                HttpGet getBall = new HttpGet("http://10.0.2.2:8080/getPosition");
                try {
                    HttpResponse response = this.httpClient.execute(getBall);
                    String responseString = responseToString(response);
                    assert responseString != null;
                    gameBall.setVectors(responseString);
                } catch (IOException e) {
                    Log.e("HTTP_RESPONSE", e.getMessage());
                }
            });

        }

        private void getScore() {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.submit(() -> {
                HttpGet getScore = new HttpGet("http://10.0.2.2:8080/getScore");
                StringBuilder ballScore = new StringBuilder();
                try {
                    HttpResponse response = this.httpClient.execute(getScore);
                    InputStream inputStream = response.getEntity().getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        ballScore.append(line);
                    }
                    scoreTxt.setText(ballScore);
                } catch (IOException e) {
                    System.out.println("IOexemption");
                }
            });

        }

        private void sendLocationPing(){
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.submit(() -> {
                //TODO: Read users actual location
                Vector3 ballPosition = this.gameBall.getPosition();
                LocationPing currentLocation = new LocationPing(ballPosition.x, ballPosition.y,
                        firebaseAuth.getUid(), Instant.now().toString(), this.playerTeam);
                //Object to convert LocationPoint into a JSON object
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                try {
                    JSONObject body = new JSONObject(ow.writeValueAsString(currentLocation));
                    HttpPost sendPing = new HttpPost("http://10.0.2.2:8080/hitBall");
                    //Set JSON object to body of http POST
                    sendPing.setEntity(new StringEntity(body.toString()));

                    HttpResponse response = httpClient.execute(sendPing);
                    String responseString = responseToString(response);

                    Log.d("BALL_HIT", responseString);
                    hitText.setText(responseString);

                    //Clear text after 2 seconds
                    ExecutorService exe = Executors.newSingleThreadExecutor();
                    exe.submit(() -> {
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            // Do Nothing
                        }
                        hitText.setText(null);
                    });

                } catch (JSONException | java.io.IOException e) {
                    Log.e("PARSE_JSON", e.getMessage());
                }
            });
        }

        private String responseToString(HttpResponse response){
            try {
                InputStream inputStream = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder responseString = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    responseString.append(line);
                }
                return responseString.toString();
            } catch (IOException e) {
                Log.e("HTTP_RESPONSE", e.getMessage());
            }
            return null;
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