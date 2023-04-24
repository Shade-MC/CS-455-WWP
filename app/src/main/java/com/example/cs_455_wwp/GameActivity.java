package com.example.cs_455_wwp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cs_455_wwp.databinding.ActivityGameBinding;
import com.example.cs_455_wwp.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    // view binding
    private ActivityGameBinding binding;

    private FirebaseAuth firebaseAuth;

    // initialize variables for GPS
    Button locationBtn;
    TextView gpsText;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        // handle logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View v) {
               firebaseAuth.signOut();
               checkUser();
           }
        });

        // set up GPS variables
        locationBtn = findViewById(R.id.locationBtn);
        gpsText = findViewById(R.id.gpsStats);

        // initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // check location permissions
                if (ActivityCompat.checkSelfPermission(GameActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    // once permission granted
                    getLocation();
                } else { // permission denied
                    ActivityCompat.requestPermissions(GameActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }
        });
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
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
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
            }
        });
    }
}