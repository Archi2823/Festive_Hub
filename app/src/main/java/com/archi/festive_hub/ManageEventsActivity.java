package com.archi.festive_hub;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ManageEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout eventsContainer;
    private TextView tvNoEvents;
    private Button btnAddEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        db = FirebaseFirestore.getInstance();

        eventsContainer = findViewById(R.id.eventsContainer);
        tvNoEvents = findViewById(R.id.tvNoEvents);
        btnAddEvent = findViewById(R.id.btnAddEvent);

        View btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnAddEvent.setOnClickListener(v ->
                showEventDialog(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );
        loadEvents();
    }

    private void loadEvents() {

        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    eventsContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        tvNoEvents.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvNoEvents.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String eventId = document.getId();

                        String eventName =
                                document.getString("name");

                        if (eventName == null) {
                            eventName =
                                    document.getString("eventName");
                        }

                        String eventDate =
                                document.getString("date");

                        String eventTime =
                                document.getString("time");

                        String eventLocation =
                                document.getString("location");

                        String eventCategory =
                                document.getString("category");

                        String eventDescription =
                                document.getString("description");

                        addEventCard(
                                eventId,
                                eventName,
                                eventDate,
                                eventTime,
                                eventLocation,
                                eventCategory,
                                eventDescription
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load events",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void addEventCard(
            String eventId,
            String eventName,
            String eventDate,
            String eventTime,
            String eventLocation,
            String eventCategory,
            String eventDescription
    ) {

        View eventView =
                LayoutInflater.from(this).inflate(
                        R.layout.item_admin_event,
                        eventsContainer,
                        false
                );

        TextView tvEventName =
                eventView.findViewById(
                        R.id.tvAdminEventName
                );

        TextView tvEventDetails =
                eventView.findViewById(
                        R.id.tvAdminEventDetails
                );

        Button btnEdit =
                eventView.findViewById(
                        R.id.btnEditEvent
                );

        Button btnDelete =
                eventView.findViewById(
                        R.id.btnDeleteEvent
                );

        Button btnRegisteredUsers =
                eventView.findViewById(
                        R.id.btnRegisteredUsers
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

        btnEdit.setOnClickListener(v ->
                showEventDialog(
                        eventId,
                        eventName,
                        eventDate,
                        eventTime,
                        eventLocation,
                        eventCategory,
                        eventDescription
                )
        );

        btnDelete.setOnClickListener(v ->
                confirmDelete(
                        eventId,
                        eventName
                )
        );

        btnRegisteredUsers.setOnClickListener(v -> {

            android.content.Intent intent =
                    new android.content.Intent(
                            ManageEventsActivity.this,
                            RegisteredUsersActivity.class
                    );

            intent.putExtra(
                    "eventId",
                    eventId
            );

            intent.putExtra(
                    "eventName",
                    eventName
            );

            startActivity(intent);
        });

        eventsContainer.addView(eventView);
    }

    private void showEventDialog(
            String eventId,
            String eventName,
            String eventDate,
            String eventTime,
            String eventLocation,
            String eventCategory,
            String eventDescription
    ) {

        View dialogView =
                LayoutInflater.from(this).inflate(
                        R.layout.dialog_event_form,
                        null
                );

        EditText etEventName =
                dialogView.findViewById(
                        R.id.etEventName
                );

        EditText etEventDate =
                dialogView.findViewById(
                        R.id.etEventDate
                );

        EditText etEventTime =
                dialogView.findViewById(
                        R.id.etEventTime
                );

        EditText etEventLocation =
                dialogView.findViewById(
                        R.id.etEventLocation
                );

        EditText etEventCategory =
                dialogView.findViewById(
                        R.id.etEventCategory
                );

        EditText etEventDescription =
                dialogView.findViewById(
                        R.id.etEventDescription
                );

        if (eventId != null) {

            etEventName.setText(
                    eventName != null ? eventName : ""
            );

            etEventDate.setText(
                    eventDate != null ? eventDate : ""
            );

            etEventTime.setText(
                    eventTime != null ? eventTime : ""
            );

            etEventLocation.setText(
                    eventLocation != null ? eventLocation : ""
            );

            etEventCategory.setText(
                    eventCategory != null ? eventCategory : ""
            );

            etEventDescription.setText(
                    eventDescription != null
                            ? eventDescription
                            : ""
            );
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                eventId == null
                                        ? "Add Event"
                                        : "Edit Event"
                        )
                        .setView(dialogView)
                        .setPositiveButton(
                                eventId == null
                                        ? "Add"
                                        : "Save",
                                null
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    Button positiveButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    positiveButton.setOnClickListener(
                            v -> {

                                String name =
                                        etEventName
                                                .getText()
                                                .toString()
                                                .trim();

                                String date =
                                        etEventDate
                                                .getText()
                                                .toString()
                                                .trim();

                                String time =
                                        etEventTime
                                                .getText()
                                                .toString()
                                                .trim();

                                String location =
                                        etEventLocation
                                                .getText()
                                                .toString()
                                                .trim();

                                String category =
                                        etEventCategory
                                                .getText()
                                                .toString()
                                                .trim();

                                String description =
                                        etEventDescription
                                                .getText()
                                                .toString()
                                                .trim();

                                if (name.isEmpty()) {
                                    etEventName.setError(
                                            "Enter event name"
                                    );
                                    return;
                                }

                                saveEvent(
                                        dialog,
                                        eventId,
                                        name,
                                        date,
                                        time,
                                        location,
                                        category,
                                        description
                                );
                            }
                    );
                }
        );

        dialog.show();
    }

    private void saveEvent(
            AlertDialog dialog,
            String eventId,
            String name,
            String date,
            String time,
            String location,
            String category,
            String description
    ) {

        java.util.HashMap<String, Object> event =
                new java.util.HashMap<>();

        event.put("name", name);
        event.put("eventName", name);
        event.put("date", date);
        event.put("eventDate", date);
        event.put("time", time);
        event.put("eventTime", time);
        event.put("location", location);
        event.put("eventLocation", location);
        event.put("category", category);
        event.put("description", description);

        if (eventId == null) {

            db.collection("events")
                    .add(event)
                    .addOnSuccessListener(documentReference -> {

                        Toast.makeText(
                                this,
                                "Event added successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();
                        loadEvents();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    this,
                                    "Unable to add event",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );

        } else {

            db.collection("events")
                    .document(eventId)
                    .update(event)
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                this,
                                "Event updated successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();
                        loadEvents();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    this,
                                    "Unable to update event",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        }
    }

    private void confirmDelete(
            String eventId,
            String eventName
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage(
                        "Delete \"" +
                                (eventName != null
                                        ? eventName
                                        : "this event") +
                                "\"?"
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteEvent(eventId)
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void deleteEvent(String eventId) {

        db.collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Event deleted successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadEvents();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to delete event",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}