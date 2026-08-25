package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class VolunteerActivity extends AppCompatActivity {

    private Button btnScanQr;
    private Button btnMyEvents;
    private Button btnVolunteerProfile;
    private Button btnVolunteerLogout;

    private FirebaseAuth mAuth;

    private static final String VOLUNTEER_EMAIL =
            "test@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);

        mAuth = FirebaseAuth.getInstance();

        if (!isVolunteer()) {
            Toast.makeText(
                    this,
                    "Volunteer access only",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        btnScanQr = findViewById(R.id.btnScanQr);
        btnMyEvents = findViewById(R.id.btnMyEvents);
        btnVolunteerProfile = findViewById(R.id.btnVolunteerProfile);
        btnVolunteerLogout = findViewById(R.id.btnVolunteerLogout);

        btnScanQr.setOnClickListener(v -> {
            Intent intent = new Intent(
                    VolunteerActivity.this,
                    VolunteerScanner.class
            );
            startActivity(intent);
        });

        btnMyEvents.setOnClickListener(v -> {
            Intent intent = new Intent(
                    VolunteerActivity.this,
                    VolunteerEventsActivity.class
            );

            startActivity(intent);
        });

        btnVolunteerProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    VolunteerActivity.this,
                    Profile.class
            );
            startActivity(intent);
        });

        btnVolunteerLogout.setOnClickListener(v -> {
            mAuth.signOut();

            Toast.makeText(
                    VolunteerActivity.this,
                    "Logged out successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        });
    }

    private boolean isVolunteer() {

        if (mAuth.getCurrentUser() == null) {
            return false;
        }

        String email = mAuth.getCurrentUser().getEmail();

        return email != null
                && email.equalsIgnoreCase(VOLUNTEER_EMAIL);
    }
}