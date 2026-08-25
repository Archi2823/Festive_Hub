package com.archi.festive_hub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class VolunteerEventsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private LinearLayout eventsContainer;
    private TextView tvNoEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_events);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        eventsContainer = findViewById(R.id.eventsContainer);
        tvNoEvents = findViewById(R.id.tvNoEvents);

        loadAssignedEvents();
    }

    private void loadAssignedEvents() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String email = mAuth.getCurrentUser().getEmail();

        if (email == null) {

            Toast.makeText(
                    this,
                    "Unable to identify volunteer",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        db.collection("events")
                .whereEqualTo(
                        "assignedVolunteerEmail",
                        email
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    eventsContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {

                        tvNoEvents.setVisibility(
                                View.VISIBLE
                        );

                        return;
                    }

                    tvNoEvents.setVisibility(
                            View.GONE
                    );

                    for (QueryDocumentSnapshot document :
                            querySnapshot) {

                        String eventName =
                                document.getString("eventName");

                        String eventDate =
                                document.getString("eventDate");

                        String eventTime =
                                document.getString("eventTime");

                        String eventLocation =
                                document.getString("eventLocation");

                        String eventCategory =
                                document.getString("eventCategory");

                        addEventCard(
                                eventName,
                                eventDate,
                                eventTime,
                                eventLocation,
                                eventCategory
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    tvNoEvents.setVisibility(
                            View.VISIBLE
                    );

                    tvNoEvents.setText(
                            "Unable to load assigned events"
                    );

                    Toast.makeText(
                            this,
                            "Unable to load events",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void addEventCard(
            String eventName,
            String eventDate,
            String eventTime,
            String eventLocation,
            String eventCategory
    ) {

        View eventView =
                LayoutInflater.from(this).inflate(
                        R.layout.item_volunteer_event,
                        eventsContainer,
                        false
                );

        TextView tvEventName =
                eventView.findViewById(
                        R.id.tvVolunteerEventName
                );

        TextView tvEventDetails =
                eventView.findViewById(
                        R.id.tvVolunteerEventDetails
                );

        tvEventName.setText(
                eventName != null
                        ? eventName
                        : "Event"
        );

        String details =
                "Date: " +
                        (eventDate != null
                                ? eventDate
                                : "N/A")
                        + "\nTime: " +
                        (eventTime != null
                                ? eventTime
                                : "N/A")
                        + "\nLocation: " +
                        (eventLocation != null
                                ? eventLocation
                                : "N/A")
                        + "\nCategory: " +
                        (eventCategory != null
                                ? eventCategory
                                : "N/A");

        tvEventDetails.setText(details);

        eventsContainer.addView(eventView);
    }
}