
package com.archi.festive_hub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private Button btnShareEvent;

    private ImageView eventBannerImage;
    private TextView tvEventName;
    private TextView tvEventDate;
    private TextView tvEventTime;
    private TextView tvEventLocation;
    private TextView tvEventCategory;
    private TextView tvEventDescription;

    private String eventId;
    private String eventName = "Event";
    private String eventDate = "";
    private String eventTime = "";
    private String eventLocation = "";
    private String category = "";
    private String description = "";
    private String eventLink = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBookEvent = findViewById(R.id.btnBookEvent);
        btnShowQr = findViewById(R.id.btnShowQr);
        btnViewGallery = findViewById(R.id.btnViewGallery);
        btnShareEvent = findViewById(R.id.btnShareEvent);

        eventBannerImage = findViewById(R.id.eventBannerImage);
        tvEventName = findViewById(R.id.tvEventName);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventTime = findViewById(R.id.tvEventTime);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        tvEventCategory = findViewById(R.id.tvEventCategory);
        tvEventDescription = findViewById(R.id.tvEventDescription);

        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Event not found",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> finish());

        btnViewGallery.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(
                    EventDetail.this,
                    EventGalleryActivity.class
            );
            galleryIntent.putExtra("eventId", eventId);
            galleryIntent.putExtra("eventName", eventName);
            startActivity(galleryIntent);
        });

        btnShareEvent.setOnClickListener(v -> shareEvent());

        btnBookEvent.setOnClickListener(v -> {
            if (btnBookEvent.getText().toString().startsWith("Join")) {
                createRegistration();
            } else {
                deleteRegistration();
            }
        });

        btnShowQr.setOnClickListener(v -> showMyQrCode());

        loadEvent();
    }

    private void loadEvent() {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Event not found",
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    eventName = readString(snapshot, "eventName", "name");
                    eventDate = readString(snapshot, "eventDate", "date");
                    eventTime = readString(snapshot, "eventTime", "time");
                    eventLocation = readString(
                            snapshot, "eventLocation", "location");
                    category = readString(snapshot, "category", null);
                    description = readString(snapshot, "description", null);
                    eventLink = readString(snapshot, "eventLink", null);

                    tvEventName.setText(eventName);
                    tvEventDate.setText("📅  " +
                            (eventDate.isEmpty() ? "Date not available" : eventDate));
                    tvEventTime.setText("🕐  " +
                            (eventTime.isEmpty() ? "Time not available" : eventTime));
                    tvEventLocation.setText("📍  " +
                            (eventLocation.isEmpty()
                                    ? "Location not available" : eventLocation));
                    tvEventCategory.setText(
                            category.isEmpty() ? "Festivals" : category);
                    tvEventDescription.setText(
                            description.isEmpty()
                                    ? "No description available." : description);

                    String bannerUrl = snapshot.getString("bannerUrl");
                    if (bannerUrl != null && !bannerUrl.trim().isEmpty()) {
                        Glide.with(this)
                                .load(bannerUrl)
                                .centerCrop()
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_gallery)
                                .into(eventBannerImage);
                    } else {
                        eventBannerImage.setImageResource(
                                android.R.drawable.ic_menu_gallery);
                    }

                    checkRegistration();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Unable to load event",
                                Toast.LENGTH_SHORT).show());
    }

    private String readString(
            DocumentSnapshot snapshot,
            String primaryField,
            String fallbackField
    ) {
        String value = snapshot.getString(primaryField);

        if ((value == null || value.trim().isEmpty())
                && fallbackField != null) {
            value = snapshot.getString(fallbackField);
        }

        return value == null ? "" : value.trim();
    }

    private void shareEvent() {
        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Event link unavailable",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        /*
         * The eventLink field must contain a real, publicly reachable
         * HTTPS URL for recipients to open it in a browser.
         * A custom app URI alone is not a public web link.
         */
        String shareUrl = eventLink;

        if (shareUrl.isEmpty()) {
            Toast.makeText(
                    this,
                    "This event does not have a shareable HTTPS link yet.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("🎉 ").append(eventName).append("\n\n");

        if (!eventDate.isEmpty()) {
            message.append("📅 Date: ").append(eventDate).append("\n");
        }

        if (!eventTime.isEmpty()) {
            message.append("🕐 Time: ").append(eventTime).append("\n");
        }

        if (!eventLocation.isEmpty()) {
            message.append("📍 Location: ").append(eventLocation).append("\n");
        }

        message.append("\nView event:\n").append(shareUrl);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, eventName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message.toString());

        startActivity(Intent.createChooser(shareIntent, "Share event"));
    }

    private void showMyQrCode() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String registrationId = getRegistrationId();
        if (registrationId == null) {
            return;
        }

        db.collection("eventRegistrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Intent intent = new Intent(
                                EventDetail.this,
                                EventQrActivity.class
                        );
                        intent.putExtra("registrationId", registrationId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this,
                                "Please join the event first",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Unable to check registration",
                                Toast.LENGTH_SHORT).show());
    }

    private String getRegistrationId() {
        if (mAuth.getCurrentUser() == null || eventId == null) {
            return null;
        }

        return mAuth.getCurrentUser().getUid() + "_" + eventId;
    }

    private void checkRegistration() {
        String registrationId = getRegistrationId();

        if (registrationId == null) {
            updateButtonToJoin();
            return;
        }

        db.collection("eventRegistrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        updateButtonToJoined();
                    } else {
                        updateButtonToJoin();
                    }
                })
                .addOnFailureListener(e -> updateButtonToJoin());
    }

    private void createRegistration() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String registrationId = getRegistrationId();
        if (registrationId == null) {
            return;
        }

        Map<String, Object> registration = new HashMap<>();
        registration.put("userId", mAuth.getCurrentUser().getUid());
        registration.put("eventId", eventId);
        registration.put("eventName", eventName);
        registration.put("eventDate", eventDate);
        registration.put("eventTime", eventTime);
        registration.put("eventLocation", eventLocation);
        registration.put("category", category);
        registration.put("description", description);
        registration.put("status", "Registered");
        registration.put("qrToken", generateSecureQrToken());

        db.collection("eventRegistrations")
                .document(registrationId)
                .set(registration)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Successfully joined the event!",
                            Toast.LENGTH_SHORT).show();
                    updateButtonToJoined();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Registration failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private String generateSecureQrToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);

        StringBuilder token = new StringBuilder();
        for (byte b : tokenBytes) {
            token.append(String.format("%02x", b & 0xff));
        }

        return token.toString();
    }

    private void deleteRegistration() {
        String registrationId = getRegistrationId();

        if (registrationId == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cancel Registration")
                .setMessage("Do you want to leave this event?")
                .setPositiveButton("Yes", (dialog, which) ->
                        db.collection("eventRegistrations")
                                .document(registrationId)
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this,
                                            "Registration cancelled",
                                            Toast.LENGTH_SHORT).show();
                                    updateButtonToJoin();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this,
                                                "Unable to cancel registration",
                                                Toast.LENGTH_SHORT).show()))
                .setNegativeButton("No", null)
                .show();
    }

    private void updateButtonToJoined() {
        btnBookEvent.setText("Joined");
        btnBookEvent.setEnabled(true);
    }

    private void updateButtonToJoin() {
        btnBookEvent.setText("Join Event");
        btnBookEvent.setEnabled(true);
    }
}
