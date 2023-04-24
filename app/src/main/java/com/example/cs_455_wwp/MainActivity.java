package com.example.cs_455_wwp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.annotation.NonNull;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.widget.Toast;

import com.example.cs_455_wwp.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private TextView ballStats;
    private GameThread gameThread;
    private boolean isRunning;

    // view binding
    private ActivityMainBinding binding;

    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient googleSignInClient;

    private FirebaseAuth firebaseAuth;

    private static final String TAG = "GOOGLE_SIGN_IN_TAG";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the surface view
        SurfaceView surfaceView = findViewById(R.id.gameView);
        surfaceView.getHolder().addCallback(this);

        this.ballStats = findViewById(R.id.ballStats);

        // binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // configure Google Sign-In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // initialize Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        // Google sign-in button
        binding.googleSignInBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v){
               // begin Google sign-in process
               Intent intent = googleSignInClient.getSignInIntent();
               startActivityForResult(intent, RC_SIGN_IN);
           }
        });

    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        // if user is already logged in,
        if (firebaseUser != null){
            // go to profile activity first
            startActivity(new Intent(this, GameActivity.class));
            finish();
        }
    }

    // handle result of the sign-in request
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // result returned from launching intent from GoogleSignInApi.getSignInIntent()
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google sign-in succeeded, so Firebase authenticate now
                GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                firebaseAuthWithGoogleAccount(account);
            } catch (Exception e) {
                // Google sign-in failed
                Log.d(TAG, "onActivityResult: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogleAccount(GoogleSignInAccount account) {
        // start Firebase Auth with Google Account
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) { // login successful
                        // retrieve logged-in user
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        // retrieve the user's info
                        String uid = firebaseUser.getUid();
                        String email = firebaseUser.getEmail();
                        Log.d(TAG, "onSuccess: User ID is " + uid);
                        Log.d(TAG, "onSuccess: User Email is " + email);

                        // check if they are a new user or an existing user
                        if (authResult.getAdditionalUserInfo().isNewUser()){
                            // new account is created
                            Toast.makeText(MainActivity.this, "Account Created!\n" + email, Toast.LENGTH_SHORT).show();
                        } else {
                            // account is an existing user
                            Toast.makeText(MainActivity.this, "Existing Account!\n" + email, Toast.LENGTH_SHORT).show();
                        }

                        // start profile activity
                        startActivity(new Intent(MainActivity.this, GameActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // login failed
                        Log.d(TAG, "onFailure: Login Failed. " + e.getMessage());
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