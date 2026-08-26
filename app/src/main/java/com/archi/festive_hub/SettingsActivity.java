package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private View settingsBackButton;
    private View settingsProfileButton;
    private View settingsNotificationsButton;
    private View settingsLogoutButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        settingsBackButton = findViewById(R.id.settingsBackButton);
        settingsProfileButton = findViewById(R.id.settingsProfileButton);
        settingsNotificationsButton = findViewById(R.id.settingsNotificationsButton);
        settingsLogoutButton = findViewById(R.id.settingsLogoutButton);

        settingsBackButton.setOnClickListener(v -> {
            finish();
        });

        settingsProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    SettingsActivity.this,
                    Profile.class
            );
            startActivity(intent);
        });

        settingsNotificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    SettingsActivity.this,
                    NotificationActivity.class
            );
            startActivity(intent);
        });

        settingsLogoutButton.setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void logoutUser() {

        mAuth.signOut();

        Intent intent = new Intent(
                SettingsActivity.this,
                login.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}