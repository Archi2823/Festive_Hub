package com.archi.festive_hub;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Profile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private LinearLayout registeredEventsContainer;
    private TextView tvNoEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        registeredEventsContainer =
                findViewById(R.id.registeredEventsContainer);
        tvNoEvents = findViewById(R.id.tvNoEvents);

        btnBack.setOnClickListener(v -> finish());

        loadRegisteredEvents();
    }

    private void loadRegisteredEvents() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("eventRegistrations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    registeredEventsContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {

                        tvNoEvents.setVisibility(View.VISIBLE);

                    } else {

                        tvNoEvents.setVisibility(View.GONE);

                        for (QueryDocumentSnapshot document :
                                querySnapshot) {

                            String eventName =
                                    document.getString("eventName");

                            String eventDate =
                                    document.getString("eventDate");

                            String eventLocation =
                                    document.getString("eventLocation");

                            String status =
                                    document.getString("status");

                            addEventCard(
                                    eventName,
                                    eventDate,
                                    eventLocation,
                                    status
                            );
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load registered events",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void addEventCard(
            String eventName,
            String eventDate,
            String eventLocation,
            String status
    ) {

        View eventView = getLayoutInflater().inflate(
                R.layout.item_registered_event,
                registeredEventsContainer,
                false
        );

        TextView tvEventName =
                eventView.findViewById(R.id.tvEventName);

        TextView tvEventDate =
                eventView.findViewById(R.id.tvEventDate);

        TextView tvEventLocation =
                eventView.findViewById(R.id.tvEventLocation);

        TextView tvStatus =
                eventView.findViewById(R.id.tvStatus);

        tvEventName.setText(
                eventName != null ? eventName : "Event"
        );

        tvEventDate.setText(
                eventDate != null ? eventDate : "Date not available"
        );

        tvEventLocation.setText(
                eventLocation != null
                        ? eventLocation
                        : "Location not available"
        );

        tvStatus.setText(
                status != null ? status : "Registered"
        );

        registeredEventsContainer.addView(eventView);
    }
}