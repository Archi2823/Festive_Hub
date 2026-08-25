package com.archi.festive_hub;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

public class ManageEventsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private LinearLayout eventsContainer;
    private TextView tvNoEvents;
    private Button btnAddEvent;

    private static final String ADMIN_EMAIL =
            "upadhyaysisters53@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_events);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        eventsContainer = findViewById(R.id.eventsContainer);
        tvNoEvents = findViewById(R.id.tvNoEvents);
        btnAddEvent = findViewById(R.id.btnAddEvent);

        ImageButton btnBack =
                findViewById(R.id.btnBack);

        btnBack.setOnClickListener(
                v -> finish()
        );

        if (!isAdmin()) {

            Toast.makeText(
                    this,
                    "Admin access required",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        btnAddEvent.setOnClickListener(v ->
                showAddEditEventDialog(
                        null,
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

    private boolean isAdmin() {

        if (mAuth.getCurrentUser() == null) {
            return false;
        }

        String email =
                mAuth.getCurrentUser().getEmail();

        return email != null
                && ADMIN_EMAIL.equalsIgnoreCase(email);
    }

    private void loadEvents() {

        db.collection("events")
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

                    for (
                            QueryDocumentSnapshot document :
                            querySnapshot
                    ) {

                        String eventId =
                                document.getId();

                        String eventName =
                                document.getString(
                                        "eventName"
                                );

                        String eventDate =
                                document.getString(
                                        "eventDate"
                                );

                        String eventTime =
                                document.getString(
                                        "eventTime"
                                );

                        String eventLocation =
                                document.getString(
                                        "eventLocation"
                                );

                        String category =
                                document.getString(
                                        "category"
                                );

                        String description =
                                document.getString(
                                        "description"
                                );

                        String bannerUrl =
                                document.getString(
                                        "bannerUrl"
                                );

                        addEventCard(
                                eventId,
                                eventName,
                                eventDate,
                                eventTime,
                                eventLocation,
                                category,
                                description,
                                bannerUrl
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
            String category,
            String description,
            String bannerUrl
    ) {

        View eventView =
                LayoutInflater.from(this)
                        .inflate(
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
                        (category != null
                                ? category
                                : "N/A");

        tvEventDetails.setText(details);

        btnEdit.setOnClickListener(v ->
                showAddEditEventDialog(
                        eventId,
                        eventName,
                        eventDate,
                        eventTime,
                        eventLocation,
                        category,
                        description,
                        bannerUrl
                )
        );

        btnDelete.setOnClickListener(v ->
                confirmDelete(
                        eventId,
                        eventName
                )
        );

        eventsContainer.addView(eventView);
    }

    private void showAddEditEventDialog(
            String eventId,
            String eventName,
            String eventDate,
            String eventTime,
            String eventLocation,
            String category,
            String description,
            String existingBannerUrl
    ) {

        View dialogView =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_event_form,
                                null
                        );

        EditText etBannerUrl =
                dialogView.findViewById(
                        R.id.etEventBannerUrl
                );

        Button btnPreviewBanner =
                dialogView.findViewById(
                        R.id.btnPreviewBanner
                );

        ImageView ivEventBanner =
                dialogView.findViewById(
                        R.id.ivEventBanner
                );

        EditText etName =
                dialogView.findViewById(
                        R.id.etEventName
                );

        EditText etDate =
                dialogView.findViewById(
                        R.id.etEventDate
                );

        EditText etTime =
                dialogView.findViewById(
                        R.id.etEventTime
                );

        EditText etLocation =
                dialogView.findViewById(
                        R.id.etEventLocation
                );

        EditText etCategory =
                dialogView.findViewById(
                        R.id.etEventCategory
                );

        EditText etDescription =
                dialogView.findViewById(
                        R.id.etEventDescription
                );

        if (existingBannerUrl != null
                && !existingBannerUrl.isEmpty()) {

            etBannerUrl.setText(
                    existingBannerUrl
            );

            loadBanner(
                    ivEventBanner,
                    existingBannerUrl
            );
        }

        if (eventName != null) {
            etName.setText(eventName);
        }

        if (eventDate != null) {
            etDate.setText(eventDate);
        }

        if (eventTime != null) {
            etTime.setText(eventTime);
        }

        if (eventLocation != null) {
            etLocation.setText(eventLocation);
        }

        if (category != null) {
            etCategory.setText(category);
        }

        if (description != null) {
            etDescription.setText(description);
        }

        btnPreviewBanner.setOnClickListener(v -> {

            String bannerUrl =
                    etBannerUrl.getText()
                            .toString()
                            .trim();

            if (bannerUrl.isEmpty()) {

                Toast.makeText(
                        this,
                        "Please enter an image URL",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            loadBanner(
                    ivEventBanner,
                    bannerUrl
            );
        });

        boolean isEdit =
                eventId != null;

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                isEdit
                                        ? "Edit Event"
                                        : "Add Event"
                        )
                        .setView(dialogView)
                        .setPositiveButton(
                                isEdit
                                        ? "Update"
                                        : "Add",
                                null
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d -> {

            Button positiveButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            positiveButton.setOnClickListener(v -> {

                String name =
                        etName.getText()
                                .toString()
                                .trim();

                String date =
                        etDate.getText()
                                .toString()
                                .trim();

                String time =
                        etTime.getText()
                                .toString()
                                .trim();

                String location =
                        etLocation.getText()
                                .toString()
                                .trim();

                String categoryValue =
                        etCategory.getText()
                                .toString()
                                .trim();

                String descriptionValue =
                        etDescription.getText()
                                .toString()
                                .trim();

                String bannerUrl =
                        etBannerUrl.getText()
                                .toString()
                                .trim();

                if (name.isEmpty()
                        || date.isEmpty()
                        || location.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Event name, date and location are required",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                positiveButton.setEnabled(false);

                saveEvent(
                        eventId,
                        name,
                        date,
                        time,
                        location,
                        categoryValue,
                        descriptionValue,
                        bannerUrl,
                        positiveButton,
                        dialog
                );
            });
        });

        dialog.show();
    }

    private void loadBanner(
            ImageView imageView,
            String bannerUrl
    ) {

        Glide.with(this)
                .load(bannerUrl)
                .placeholder(
                        android.R.drawable.ic_menu_gallery
                )
                .error(
                        android.R.drawable.ic_dialog_alert
                )
                .centerCrop()
                .into(imageView);
    }

    private void saveEvent(
            String eventId,
            String name,
            String date,
            String time,
            String location,
            String category,
            String description,
            String bannerUrl,
            Button positiveButton,
            AlertDialog dialog
    ) {

        if (!isAdmin()) {

            Toast.makeText(
                    this,
                    "Admin access required",
                    Toast.LENGTH_SHORT
            ).show();

            positiveButton.setEnabled(true);

            return;
        }

        String documentId =
                eventId != null
                        ? eventId
                        : db.collection("events")
                        .document()
                        .getId();

        saveEventToFirestore(
                documentId,
                eventId,
                name,
                date,
                time,
                location,
                category,
                description,
                bannerUrl,
                positiveButton,
                dialog
        );
    }

    private void saveEventToFirestore(
            String documentId,
            String eventId,
            String name,
            String date,
            String time,
            String location,
            String category,
            String description,
            String bannerUrl,
            Button positiveButton,
            AlertDialog dialog
    ) {

        Map<String, Object> event =
                new HashMap<>();

        event.put(
                "eventName",
                name
        );

        event.put(
                "eventDate",
                date
        );

        event.put(
                "eventTime",
                time
        );

        event.put(
                "eventLocation",
                location
        );

        event.put(
                "category",
                category
        );

        event.put(
                "description",
                description
        );

        event.put(
                "bannerUrl",
                bannerUrl != null
                        ? bannerUrl
                        : ""
        );

        if (eventId == null) {

            db.collection("events")
                    .document(documentId)
                    .set(event)
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                this,
                                "Event added successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();

                        loadEvents();
                    })
                    .addOnFailureListener(e -> {

                        positiveButton.setEnabled(true);

                        Toast.makeText(
                                this,
                                "Unable to add event: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });

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
                    .addOnFailureListener(e -> {

                        positiveButton.setEnabled(true);

                        Toast.makeText(
                                this,
                                "Unable to update event: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
        }
    }

    private void confirmDelete(
            String eventId,
            String eventName
    ) {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Delete Event"
                )
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

    private void deleteEvent(
            String eventId
    ) {

        if (!isAdmin()) {

            Toast.makeText(
                    this,
                    "Admin access required",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

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
                                "Unable to delete event: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }
}