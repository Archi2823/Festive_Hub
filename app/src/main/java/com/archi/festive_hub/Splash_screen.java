package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class Splash_screen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(() -> {

            Intent intent = new Intent(
                    Splash_screen.this,
                    activity_signup.class
            );

            startActivity(intent);
            finish();

        }, SPLASH_DURATION);
    }
}