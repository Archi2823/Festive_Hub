package com.archi.festive_hub;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout eventsContainer;
    private TextView tvNoEvents;
    private Button btnAddEvent;

    private ImageView currentBannerPreview;
    private TextView currentBannerStatus;
    private String existingBannerUrl = "";

    private final List<String> volunteerEmails = new ArrayList<>();
    private final List<String> volunteerNames = new ArrayList<>();

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

        loadVolunteers();
        loadEvents();
    }

    private void loadVolunteers() {
        db.collection("volunteers")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    volunteerEmails.clear();
                    volunteerNames.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String email = document.getString("email");

                        if (email == null) {
                            email = document.getString("volunteerEmail");
                        }

                        String name = document.getString("name");

                        if (name == null) {
                            name = document.getString("volunteerName");
                        }

                        if (name == null) {
                            name = email;
                        }

                        if (email != null && !email.trim().isEmpty()) {
                            volunteerEmails.add(email);
                            volunteerNames.add(
                                    name != null && !name.trim().isEmpty()
                                            ? name
                                            : email
                            );
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load volunteers",
                                Toast.LENGTH_SHORT
                        ).show()
                );
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

                        if (eventDate == null) {
                            eventDate =
                                    document.getString("eventDate");
                        }

                        String eventTime =
                                document.getString("time");

                        if (eventTime == null) {
                            eventTime =
                                    document.getString("eventTime");
                        }

                        String eventLocation =
                                document.getString("location");

                        if (eventLocation == null) {
                            eventLocation =
                                    document.getString("eventLocation");
                        }

                        String eventCategory =
                                document.getString("category");

                        if (eventCategory == null) {
                            eventCategory =
                                    document.getString("eventCategory");
                        }

                        String eventDescription =
                                document.getString("description");

                        String assignedVolunteerEmail =
                                document.getString(
                                        "assignedVolunteerEmail"
                                );

                        addEventCard(
                                eventId,
                                eventName,
                                eventDate,
                                eventTime,
                                eventLocation,
                                eventCategory,
                                eventDescription,
                                assignedVolunteerEmail
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
            String eventDescription,
            String assignedVolunteerEmail
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

        String volunteerText =
                assignedVolunteerEmail != null
                        && !assignedVolunteerEmail.trim().isEmpty()
                        ? assignedVolunteerEmail
                        : "Not assigned";

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
                                : "N/A")
                        + "\nVolunteer: " +
                        volunteerText;

        tvEventDetails.setText(details);

        btnEdit.setOnClickListener(v ->
                showEventDialog(
                        eventId,
                        eventName,
                        eventDate,
                        eventTime,
                        eventLocation,
                        eventCategory,
                        eventDescription,
                        assignedVolunteerEmail
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
        showEventDialog(
                eventId,
                eventName,
                eventDate,
                eventTime,
                eventLocation,
                eventCategory,
                eventDescription,
                null
        );
    }

    private void showEventDialog(
            String eventId,
            String eventName,
            String eventDate,
            String eventTime,
            String eventLocation,
            String eventCategory,
            String eventDescription,
            String assignedVolunteerEmail
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

        Spinner spinnerVolunteer =
                dialogView.findViewById(
                        R.id.spinnerVolunteer
                );

        EditText etEventBannerUrl = dialogView.findViewById(R.id.etEventBannerUrl);
        currentBannerPreview = dialogView.findViewById(R.id.ivEventBanner);
        currentBannerStatus = dialogView.findViewById(R.id.tvBannerUploadStatus);
        Button btnPreviewBanner = dialogView.findViewById(R.id.btnPreviewBanner);
        existingBannerUrl = "";

        btnPreviewBanner.setOnClickListener(v -> {
            String imageUrl = etEventBannerUrl.getText().toString().trim();
            if (!isValidImageUrl(imageUrl)) {
                etEventBannerUrl.setError("Enter a valid http/https image URL");
                return;
            }
            Glide.with(ManageEventsActivity.this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_delete)
                    .into(currentBannerPreview);
            currentBannerStatus.setText("Image URL preview");
        });

        if (eventId != null) {
            db.collection("events").document(eventId).get().addOnSuccessListener(snapshot -> {
                String savedUrl = snapshot.getString("bannerUrl");
                if (savedUrl != null && !savedUrl.trim().isEmpty()) {
                    existingBannerUrl = savedUrl;
                    etEventBannerUrl.setText(savedUrl);
                    Glide.with(ManageEventsActivity.this)
                            .load(savedUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(currentBannerPreview);
                    currentBannerStatus.setText("Existing event image URL loaded");
                }
            });
        }

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

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        getVolunteerDisplayList()
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerVolunteer.setAdapter(adapter);

        if (assignedVolunteerEmail != null) {

            int selectedPosition =
                    volunteerEmails.indexOf(
                            assignedVolunteerEmail
                    );

            if (selectedPosition >= 0) {
                spinnerVolunteer.setSelection(
                        selectedPosition
                );
            }
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

                                String selectedVolunteerEmail =
                                        null;

                                if (!volunteerEmails.isEmpty()) {

                                    int selectedPosition =
                                            spinnerVolunteer
                                                    .getSelectedItemPosition();

                                    if (selectedPosition >= 0
                                            && selectedPosition
                                            < volunteerEmails.size()) {

                                        selectedVolunteerEmail =
                                                volunteerEmails.get(
                                                        selectedPosition
                                                );
                                    }
                                }

                                saveEvent(
                                        dialog,
                                        eventId,
                                        name,
                                        date,
                                        time,
                                        location,
                                        category,
                                        description,
                                        selectedVolunteerEmail,
                                        etEventBannerUrl.getText().toString().trim()
                                );
                            }
                    );
                }
        );

        dialog.show();
    }

    private List<String> getVolunteerDisplayList() {

        List<String> displayList =
                new ArrayList<>();

        if (volunteerEmails.isEmpty()) {
            displayList.add(
                    "No volunteers available"
            );
            return displayList;
        }

        for (int i = 0;
             i < volunteerEmails.size();
             i++) {

            String name =
                    i < volunteerNames.size()
                            ? volunteerNames.get(i)
                            : volunteerEmails.get(i);

            String email =
                    volunteerEmails.get(i);

            if (name != null
                    && !name.equals(email)) {

                displayList.add(
                        name + " (" + email + ")"
                );

            } else {
                displayList.add(email);
            }
        }

        return displayList;
    }

    private void saveEvent(
            AlertDialog dialog,
            String eventId,
            String name,
            String date,
            String time,
            String location,
            String category,
            String description,
            String assignedVolunteerEmail,
            String bannerUrl
    ) {
        if (!bannerUrl.isEmpty() && !isValidImageUrl(bannerUrl)) {
            Toast.makeText(this, "Enter a valid http/https image URL", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> event = new HashMap<>();
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
        event.put("assignedVolunteerEmail",
                assignedVolunteerEmail == null ? "" : assignedVolunteerEmail.trim());

        // Save only the URL in Firestore. No image file is uploaded to Firebase Storage.
        event.put("bannerUrl", bannerUrl);

        DocumentReference eventRef = eventId == null
                ? db.collection("events").document()
                : db.collection("events").document(eventId);

        eventRef.set(event, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(unused -> finishEventSave(
                        dialog,
                        eventId == null ? "Event added successfully" : "Event updated successfully"
                ))
                .addOnFailureListener(e -> Toast.makeText(
                        ManageEventsActivity.this,
                        "Unable to save event: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private boolean isValidImageUrl(String url) {
        if (url == null || url.trim().isEmpty() || !Patterns.WEB_URL.matcher(url).matches()) {
            return false;
        }
        return url.startsWith("https://") || url.startsWith("http://");
    }

    private void finishEventSave(AlertDialog dialog, String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        existingBannerUrl = "";
        currentBannerPreview = null;
        currentBannerStatus = null;
        dialog.dismiss();
        loadEvents();
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