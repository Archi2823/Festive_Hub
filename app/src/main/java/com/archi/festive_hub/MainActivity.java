package com.archi.festive_hub;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        ImageButton profile = findViewById(R.id.btnProfile);
        LinearLayout cardFestivals = findViewById(R.id.cardFestivals);
        LinearLayout cardEvents = findViewById(R.id.cardEvents);
        LinearLayout eventCard = findViewById(R.id.eventCard);

        profile.setOnClickListener(v ->
                Toast.makeText(
                        MainActivity.this,
                        "Profile coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );

        cardFestivals.setOnClickListener(v ->
                Toast.makeText(
                        MainActivity.this,
                        "Festivals coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );

        cardEvents.setOnClickListener(v ->
                Toast.makeText(
                        MainActivity.this,
                        "Events coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );

        eventCard.setOnClickListener(v ->
                Toast.makeText(
                        MainActivity.this,
                        "Event details coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }
}