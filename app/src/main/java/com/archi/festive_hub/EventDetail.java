package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class EventDetail extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Button btnBookEvent;
    private Button btnShowQr;
    private Button btnViewGallery;

    private String eventId;
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String eventLocation;
    private String category;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_event_detail
        );

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        ImageButton btnBack =
                findViewById(R.id.btnBack);

        btnBookEvent =
                findViewById(R.id.btnBookEvent);

        btnShowQr =
                findViewById(R.id.btnShowQr);
        btnViewGallery = findViewById(R.id.btnViewGallery);

        android.widget.ImageView eventBannerImage = findViewById(R.id.eventBannerImage);
        android.widget.TextView tvEventName = findViewById(R.id.tvEventName);
        android.widget.TextView tvEventDate = findViewById(R.id.tvEventDate);
        android.widget.TextView tvEventTime = findViewById(R.id.tvEventTime);
        android.widget.TextView tvEventLocation = findViewById(R.id.tvEventLocation);
        android.widget.TextView tvEventCategory = findViewById(R.id.tvEventCategory);
        android.widget.TextView tvEventDescription = findViewById(R.id.tvEventDescription);

        eventId =
                getIntent().getStringExtra(
                        "eventId"
                );

        if (eventId == null ||
                eventId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Event not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        btnBack.setOnClickListener(
                v -> finish()
        );

        loadEvent();

        btnViewGallery.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(EventDetail.this, EventGalleryActivity.class);
            galleryIntent.putExtra("eventId", eventId);
            galleryIntent.putExtra("eventName", eventName);
            startActivity(galleryIntent);
        });

        btnBookEvent.setOnClickListener(v -> {

            if (btnBookEvent
                    .getText()
                    .toString()
                    .startsWith("Join")) {

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

            String registrationId =
                    getRegistrationId();

            if (registrationId == null) {
                return;
            }

            db.collection(
                            "eventRegistrations"
                    )
                    .document(registrationId)
                    .get()
                    .addOnSuccessListener(
                            documentSnapshot -> {

                                if (documentSnapshot
                                        .exists()) {

                                    Intent intent =
                                            new Intent(
                                                    EventDetail.this,
                                                    EventQrActivity.class
                                            );

                                    intent.putExtra(
                                            "registrationId",
                                            registrationId
                                    );

                                    startActivity(intent);

                                } else {

                                    Toast.makeText(
                                            EventDetail.this,
                                            "Please join the event first",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    EventDetail.this,
                                    "Unable to check registration",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        });
    }

    private void loadEvent() {

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {

                                Toast.makeText(
                                        this,
                                        "Event not found",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                                return;
                            }

                            eventName =
                                    documentSnapshot.getString(
                                            "eventName"
                                    );

                            eventDate =
                                    documentSnapshot.getString(
                                            "eventDate"
                                    );

                            eventTime =
                                    documentSnapshot.getString(
                                            "eventTime"
                                    );

                            eventLocation =
                                    documentSnapshot.getString(
                                            "eventLocation"
                                    );

                            category =
                                    documentSnapshot.getString(
                                            "category"
                                    );

                            description =
                                    documentSnapshot.getString(
                                            "description"
                                    );

                            String bannerUrl = documentSnapshot.getString("bannerUrl");
                            android.widget.ImageView eventBannerImage = findViewById(R.id.eventBannerImage);
                            if (bannerUrl != null && !bannerUrl.trim().isEmpty()) {
                                com.bumptech.glide.Glide.with(this).load(bannerUrl).centerCrop()
                                        .placeholder(android.R.drawable.ic_menu_gallery)
                                        .error(android.R.drawable.ic_menu_gallery).into(eventBannerImage);
                            } else {
                                eventBannerImage.setImageResource(android.R.drawable.ic_menu_gallery);
                            }
                            ((android.widget.TextView) findViewById(R.id.tvEventName)).setText(eventName != null ? eventName : "Event");
                            ((android.widget.TextView) findViewById(R.id.tvEventDate)).setText("📅  " + (eventDate == null ? "Date not available" : eventDate));
                            ((android.widget.TextView) findViewById(R.id.tvEventTime)).setText("🕐  " + (eventTime == null ? "Time not available" : eventTime));
                            ((android.widget.TextView) findViewById(R.id.tvEventLocation)).setText("📍  " + (eventLocation == null ? "Location not available" : eventLocation));
                            ((android.widget.TextView) findViewById(R.id.tvEventCategory)).setText(category == null || category.isEmpty() ? "Festivals" : category);
                            ((android.widget.TextView) findViewById(R.id.tvEventDescription)).setText(description == null || description.isEmpty() ? "No description available." : description);

                            if (eventName == null) {
                                eventName = "Event";
                            }

                            if (eventDate == null) {
                                eventDate = "";
                            }

                            if (eventTime == null) {
                                eventTime = "";
                            }

                            if (eventLocation == null) {
                                eventLocation = "";
                            }

                            if (category == null) {
                                category = "";
                            }

                            if (description == null) {
                                description = "";
                            }

                            checkRegistration();
                        }
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load event",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private String getRegistrationId() {

        if (mAuth.getCurrentUser() == null ||
                eventId == null) {

            return null;
        }

        return mAuth.getCurrentUser().getUid()
                + "_"
                + eventId;
    }

    private void checkRegistration() {

        String registrationId =
                getRegistrationId();

        if (registrationId == null) {

            updateButtonToJoin();
            return;
        }

        db.collection("eventRegistrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (documentSnapshot.exists()) {

                                updateButtonToJoined();

                            } else {

                                updateButtonToJoin();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> updateButtonToJoin()
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

        String registrationId =
                getRegistrationId();

        if (registrationId == null) {
            return;
        }

        String qrToken =
                generateSecureQrToken();

        Map<String, Object> registration =
                new HashMap<>();

        registration.put(
                "userId",
                mAuth.getCurrentUser().getUid()
        );

        registration.put(
                "eventId",
                eventId
        );

        registration.put(
                "eventName",
                eventName
        );

        registration.put(
                "eventDate",
                eventDate
        );

        registration.put(
                "eventTime",
                eventTime
        );

        registration.put(
                "eventLocation",
                eventLocation
        );

        registration.put(
                "category",
                category
        );

        registration.put(
                "description",
                description
        );

        registration.put(
                "status",
                "Registered"
        );

        registration.put(
                "qrToken",
                qrToken
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
                            "Registration failed: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private String generateSecureQrToken() {

        SecureRandom secureRandom =
                new SecureRandom();

        byte[] tokenBytes =
                new byte[32];

        secureRandom.nextBytes(
                tokenBytes
        );

        StringBuilder token =
                new StringBuilder();

        for (byte b : tokenBytes) {

            token.append(
                    String.format(
                            "%02x",
                            b & 0xff
                    )
            );
        }

        return token.toString();
    }

    private void deleteRegistration() {

        String registrationId =
                getRegistrationId();

        if (registrationId == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Cancel Registration"
                )
                .setMessage(
                        "Do you want to leave this event?"
                )
                .setPositiveButton(
                        "Yes",
                        (dialog, which) -> {

                            db.collection(
                                            "eventRegistrations"
                                    )
                                    .document(
                                            registrationId
                                    )
                                    .delete()
                                    .addOnSuccessListener(
                                            unused -> {

                                                Toast.makeText(
                                                        EventDetail.this,
                                                        "Registration cancelled",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                updateButtonToJoin();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e ->
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

        btnBookEvent.setText(
                "Joined"
        );

        btnBookEvent.setEnabled(true);
    }

    private void updateButtonToJoin() {

        btnBookEvent.setText(
                "Join Event"
        );

        btnBookEvent.setEnabled(true);
    }
}