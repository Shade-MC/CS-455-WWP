package com.example.cs_455_wwp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.cs_455_wwp.databinding.ActivityGameBinding;
import com.example.cs_455_wwp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GameActivity extends AppCompatActivity {

    // view binding
    private ActivityGameBinding binding;

    private FirebaseAuth firebaseAuth;

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
}