package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EventDetail extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Button btnBookEvent;
    private Button btnShowQr;

    private static final String EVENT_ID = "celebrate_together";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBookEvent = findViewById(R.id.btnBookEvent);
        btnShowQr = findViewById(R.id.btnShowQr);

        btnBack.setOnClickListener(v -> finish());

        checkRegistration();

        btnBookEvent.setOnClickListener(v -> {

            if (btnBookEvent.getText().toString().equals("Join Event")) {
                createRegistration();
            } else {
                deleteRegistration();
            }
        });

        btnShowQr.setOnClickListener(v -> {

            if (mAuth.getCurrentUser() == null) {

                Toast.makeText(
                        EventDetail.this,
                        "Please login first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String registrationId = getRegistrationId();

            if (registrationId == null) {
                return;
            }

            db.collection("eventRegistrations")
                    .document(registrationId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if (documentSnapshot.exists()) {

                            Intent intent = new Intent(
                                    EventDetail.this,
                                    EventQrActivity.class
                            );

                            startActivity(intent);

                        } else {

                            Toast.makeText(
                                    EventDetail.this,
                                    "Please join the event first",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    EventDetail.this,
                                    "Unable to check registration",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        });
    }

    private String getRegistrationId() {

        if (mAuth.getCurrentUser() == null) {
            return null;
        }

        return mAuth.getCurrentUser().getUid()
                + "_"
                + EVENT_ID;
    }

    private void checkRegistration() {

        String registrationId = getRegistrationId();

        if (registrationId == null) {
            btnBookEvent.setText("Join Event");
            return;
        }

        db.collection("eventRegistrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {
                        updateButtonToJoined();
                    } else {
                        updateButtonToJoin();
                    }
                })
                .addOnFailureListener(e ->
                        updateButtonToJoin()
                );
    }

    private void createRegistration() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String registrationId = getRegistrationId();

        Map<String, Object> registration = new HashMap<>();

        registration.put(
                "userId",
                mAuth.getCurrentUser().getUid()
        );

        registration.put(
                "eventId",
                EVENT_ID
        );

        registration.put(
                "eventName",
                "Celebrate Together"
        );

        registration.put(
                "eventDate",
                "15 August 2026"
        );

        registration.put(
                "eventLocation",
                "City Celebration Ground"
        );

        registration.put(
                "status",
                "Registered"
        );

        db.collection("eventRegistrations")
                .document(registrationId)
                .set(registration)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            EventDetail.this,
                            "Successfully joined the event!",
                            Toast.LENGTH_SHORT
                    ).show();

                    updateButtonToJoined();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            EventDetail.this,
                            "Registration failed: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void updateRegistration() {

        String registrationId = getRegistrationId();

        if (registrationId == null) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();

        updates.put(
                "status",
                "Registered"
        );

        db.collection("eventRegistrations")
                .document(registrationId)
                .update(updates)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            EventDetail.this,
                            "Registration updated",
                            Toast.LENGTH_SHORT
                    ).show();

                    updateButtonToJoined();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                EventDetail.this,
                                "Update failed",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void deleteRegistration() {

        String registrationId = getRegistrationId();

        if (registrationId == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cancel Registration")
                .setMessage("Do you want to leave this event?")
                .setPositiveButton(
                        "Yes",
                        (dialog, which) -> {

                            db.collection("eventRegistrations")
                                    .document(registrationId)
                                    .delete()
                                    .addOnSuccessListener(unused -> {

                                        Toast.makeText(
                                                EventDetail.this,
                                                "Registration cancelled",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        updateButtonToJoin();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(
                                                    EventDetail.this,
                                                    "Unable to cancel registration",
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                    );
                        }
                )
                .setNegativeButton(
                        "No",
                        null
                )
                .show();
    }

    private void updateButtonToJoined() {

        btnBookEvent.setText("Joined ✓");
        btnBookEvent.setEnabled(true);
    }

    private void updateButtonToJoin() {

        btnBookEvent.setText("Join Event");
        btnBookEvent.setEnabled(true);
    }
}