package com.archi.festive_hub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EventDetail extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnBookEvent = findViewById(R.id.btnBookEvent);

        btnBack.setOnClickListener(v -> finish());

        btnBookEvent.setOnClickListener(v -> joinEvent());
    }

    private void joinEvent() {

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> registration = new HashMap<>();

        registration.put("userId", userId);
        registration.put("eventName", "Celebrate Together");
        registration.put("eventDate", "15 August 2026");
        registration.put("eventLocation", "City Celebration Ground");
        registration.put("status", "Registered");

        db.collection("eventRegistrations")
                .add(registration)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            EventDetail.this,
                            "Successfully joined the event!",
                            Toast.LENGTH_LONG
                    ).show();

                    Button button = findViewById(R.id.btnBookEvent);
                    button.setText("Joined ✓");
                    button.setEnabled(false);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            EventDetail.this,
                            "Registration failed. Please try again.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}