package com.archi.festive_hub;
import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventDetail extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Button btnBookEvent;
    private Button btnShowQr;
    private Button btnViewGallery;
    private Button btnShareEvent;
    private Button btnAddToMySchedule;
    private Button btnBookmarkEvent;

    private Switch switchEventReminder;
    private Spinner spinnerReminderTiming;

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

    private boolean isBookmarked = false;
    private boolean isLoadingBookmarkSettings = false;

    private static final String BOOKMARKS_COLLECTION = "eventBookmarks";
    private static final String CHANNEL_ID = "event_reminders";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize existing views
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBookEvent = findViewById(R.id.btnBookEvent);
        btnShowQr = findViewById(R.id.btnShowQr);
        btnViewGallery = findViewById(R.id.btnViewGallery);
        btnShareEvent = findViewById(R.id.btnShareEvent);
        btnAddToMySchedule = findViewById(R.id.btnAddToMySchedule);

        // Initialize bookmark and reminder views
        btnBookmarkEvent = findViewById(R.id.btnBookmarkEvent);
        switchEventReminder = findViewById(R.id.switchEventReminder);
        spinnerReminderTiming = findViewById(R.id.spinnerReminderTiming);

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

        // Back
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Add to My Schedule
        if (btnAddToMySchedule != null) {
            btnAddToMySchedule.setOnClickListener(v -> addToMySchedule());
        }

        // Bookmark
        if (btnBookmarkEvent != null) {
            btnBookmarkEvent.setOnClickListener(v -> toggleBookmark());
        }

        // Reminder switch
        if (switchEventReminder != null) {
            switchEventReminder.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (isLoadingBookmarkSettings) {
                            return;
                        }

                        if (isChecked) {
                            if (mAuth.getCurrentUser() == null) {
                                setReminderSwitch(false);
                                Toast.makeText(this,
                                        "Please login first",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            requestNotificationPermissionAndSaveReminder();
                        } else {
                            disableReminder();
                        }
                    });
        }

        // Gallery
        if (btnViewGallery != null) {
            btnViewGallery.setOnClickListener(v -> {
                Intent galleryIntent = new Intent(
                        EventDetail.this,
                        EventGalleryActivity.class);

                galleryIntent.putExtra("eventId", eventId);
                galleryIntent.putExtra("eventName", eventName);
                startActivity(galleryIntent);
            });
        }

        // Share
        if (btnShareEvent != null) {
            btnShareEvent.setOnClickListener(v -> shareEvent());
        }

        // Join / cancel registration
        if (btnBookEvent != null) {
            btnBookEvent.setOnClickListener(v -> {
                if (btnBookEvent.getText().toString().startsWith("Join")) {
                    createRegistration();
                } else {
                    deleteRegistration();
                }
            });
        }

        // QR
        if (btnShowQr != null) {
            btnShowQr.setOnClickListener(v -> showMyQrCode());
        }

        createNotificationChannel();
        loadEvent();
        loadBookmarkSettings();
    }

    // ---------------------------------------------------------
    // LOAD EVENT
    // ---------------------------------------------------------

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

                    if (tvEventName != null) {
                        tvEventName.setText(eventName);
                    }

                    if (tvEventDate != null) {
                        tvEventDate.setText("📅  "
                                + (eventDate.isEmpty()
                                ? "Date not available" : eventDate));
                    }

                    if (tvEventTime != null) {
                        tvEventTime.setText("🕐  "
                                + (eventTime.isEmpty()
                                ? "Time not available" : eventTime));
                    }

                    if (tvEventLocation != null) {
                        tvEventLocation.setText("📍  "
                                + (eventLocation.isEmpty()
                                ? "Location not available" : eventLocation));
                    }

                    if (tvEventCategory != null) {
                        tvEventCategory.setText(
                                category.isEmpty() ? "Festivals" : category);
                    }

                    if (tvEventDescription != null) {
                        tvEventDescription.setText(
                                description.isEmpty()
                                        ? "No description available."
                                        : description);
                    }

                    if (eventBannerImage != null) {
                        String bannerUrl = snapshot.getString("bannerUrl");

                        if (bannerUrl != null && !bannerUrl.trim().isEmpty()) {
                            Glide.with(this)
                                    .load(bannerUrl)
                                    .centerCrop()
                                    .placeholder(
                                            android.R.drawable.ic_menu_gallery)
                                    .error(
                                            android.R.drawable.ic_menu_gallery)
                                    .into(eventBannerImage);
                        } else {
                            eventBannerImage.setImageResource(
                                    android.R.drawable.ic_menu_gallery);
                        }
                    }

                    checkRegistration();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Unable to load event: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private String readString(
            DocumentSnapshot snapshot,
            String primaryField,
            String fallbackField) {

        String value = snapshot.getString(primaryField);

        if ((value == null || value.trim().isEmpty())
                && fallbackField != null) {
            value = snapshot.getString(fallbackField);
        }

        return value == null ? "" : value.trim();
    }

    // ---------------------------------------------------------
    // BOOKMARK
    // ---------------------------------------------------------

    private String getBookmarkDocumentId() {
        if (mAuth.getCurrentUser() == null || eventId == null) {
            return null;
        }

        return mAuth.getCurrentUser().getUid() + "_" + eventId;
    }

    private void loadBookmarkSettings() {
        String documentId = getBookmarkDocumentId();

        if (documentId == null) {
            updateBookmarkButton(false);
            return;
        }

        isLoadingBookmarkSettings = true;

        db.collection(BOOKMARKS_COLLECTION)
                .document(documentId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    boolean bookmarked = snapshot.exists()
                            && Boolean.TRUE.equals(
                            snapshot.getBoolean("isBookmarked"));

                    boolean reminderEnabled = snapshot.exists()
                            && Boolean.TRUE.equals(
                            snapshot.getBoolean("reminderEnabled"));

                    Long savedMinutes = snapshot.getLong(
                            "reminderMinutesBefore");

                    updateBookmarkButton(bookmarked);

                    if (spinnerReminderTiming != null) {
                        spinnerReminderTiming.setSelection(
                                savedMinutes != null && savedMinutes == 60L
                                        ? 1 : 0);
                    }

                    if (switchEventReminder != null) {
                        switchEventReminder.setChecked(reminderEnabled);
                    }

                    isLoadingBookmarkSettings = false;
                })
                .addOnFailureListener(e -> {
                    isLoadingBookmarkSettings = false;
                    Toast.makeText(this,
                            "Unable to load bookmark settings",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBookmarkButton(boolean bookmarked) {
        isBookmarked = bookmarked;

        if (btnBookmarkEvent != null) {
            btnBookmarkEvent.setText(
                    bookmarked ? "♥ Bookmarked" : "♡ Bookmark Event");
        }
    }

    private void toggleBookmark() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String documentId = getBookmarkDocumentId();

        if (documentId == null) {
            return;
        }

        boolean newStatus = !isBookmarked;
        btnBookmarkEvent.setEnabled(false);

        db.collection(BOOKMARKS_COLLECTION)
                .document(documentId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Object> data = new HashMap<>();

                    data.put("userId", mAuth.getCurrentUser().getUid());
                    data.put("eventId", eventId);
                    data.put("eventName", eventName);
                    data.put("eventDate", eventDate);
                    data.put("eventTime", eventTime);
                    data.put("eventLocation", eventLocation);
                    data.put("category", category);
                    data.put("isBookmarked", newStatus);
                    data.put("updatedAt", System.currentTimeMillis());

                    Long savedMinutes = snapshot.getLong(
                            "reminderMinutesBefore");

                    data.put("reminderMinutesBefore",
                            savedMinutes != null ? savedMinutes : 1440L);

                    data.put("reminderEnabled",
                            snapshot.exists()
                                    && Boolean.TRUE.equals(
                                    snapshot.getBoolean("reminderEnabled")));

                    db.collection(BOOKMARKS_COLLECTION)
                            .document(documentId)
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener(unused -> {
                                updateBookmarkButton(newStatus);
                                btnBookmarkEvent.setEnabled(true);

                                Toast.makeText(this,
                                        newStatus
                                                ? "Event bookmarked successfully!"
                                                : "Bookmark removed",
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                btnBookmarkEvent.setEnabled(true);
                                Toast.makeText(this,
                                        "Unable to save bookmark: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnBookmarkEvent.setEnabled(true);
                    Toast.makeText(this,
                            "Unable to update bookmark: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // ---------------------------------------------------------
    // REMINDER SETTINGS AND PERMISSION
    // ---------------------------------------------------------

    private long getSelectedReminderMinutes() {
        if (spinnerReminderTiming != null
                && spinnerReminderTiming.getSelectedItemPosition() == 1) {
            return 60L;
        }

        return 1440L;
    }

    private void setReminderSwitch(boolean enabled) {
        if (switchEventReminder != null) {
            isLoadingBookmarkSettings = true;
            switchEventReminder.setChecked(enabled);
            isLoadingBookmarkSettings = false;
        }
    }

    private void requestNotificationPermissionAndSaveReminder() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST);
            return;
        }

        saveReminderSettings();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveReminderSettings();
            } else {
                setReminderSwitch(false);
                Toast.makeText(this,
                        "Allow notification permission to use reminders",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveReminderSettings() {
        String documentId = getBookmarkDocumentId();

        if (documentId == null) {
            setReminderSwitch(false);
            return;
        }

        long minutesBefore = getSelectedReminderMinutes();

        // Validate event date/time before saving an active reminder.
        Calendar eventCalendar = parseEventDateTime();

        if (eventCalendar == null) {
            setReminderSwitch(false);
            Toast.makeText(this,
                    "Event date/time format is not supported. "
                            + "Please check the event details.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        long triggerAt = eventCalendar.getTimeInMillis()
                - minutesBefore * 60L * 1000L;

        if (triggerAt <= System.currentTimeMillis()) {
            setReminderSwitch(false);
            Toast.makeText(this,
                    "Selected reminder time has already passed",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", mAuth.getCurrentUser().getUid());
        data.put("eventId", eventId);
        data.put("eventName", eventName);
        data.put("eventDate", eventDate);
        data.put("eventTime", eventTime);
        data.put("eventLocation", eventLocation);
        data.put("category", category);
        data.put("isBookmarked", isBookmarked);
        data.put("reminderEnabled", true);
        data.put("reminderMinutesBefore", minutesBefore);
        data.put("updatedAt", System.currentTimeMillis());

        db.collection(BOOKMARKS_COLLECTION)
                .document(documentId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    if (scheduleEventReminder(minutesBefore)) {
                        Toast.makeText(this,
                                minutesBefore == 60L
                                        ? "Reminder set for 1 hour before"
                                        : "Reminder set for 24 hours before",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        setReminderSwitch(false);
                        Toast.makeText(this,
                                "Could not schedule notification",
                                Toast.LENGTH_LONG).show();

                        db.collection(BOOKMARKS_COLLECTION)
                                .document(documentId)
                                .update("reminderEnabled", false);
                    }
                })
                .addOnFailureListener(e -> {
                    setReminderSwitch(false);
                    Toast.makeText(this,
                            "Unable to save reminder: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void disableReminder() {
        String documentId = getBookmarkDocumentId();

        if (documentId == null) {
            return;
        }

        cancelEventReminder();

        Map<String, Object> data = new HashMap<>();
        data.put("userId", mAuth.getCurrentUser().getUid());
        data.put("eventId", eventId);
        data.put("eventName", eventName);
        data.put("eventDate", eventDate);
        data.put("eventTime", eventTime);
        data.put("eventLocation", eventLocation);
        data.put("isBookmarked", isBookmarked);
        data.put("reminderEnabled", false);
        data.put("updatedAt", System.currentTimeMillis());

        db.collection(BOOKMARKS_COLLECTION)
                .document(documentId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "Reminder turned off",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Unable to update reminder: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    // ---------------------------------------------------------
    // ANDROID NOTIFICATION SCHEDULING
    // ---------------------------------------------------------

    private boolean scheduleEventReminder(long minutesBefore) {
        Calendar eventCalendar = parseEventDateTime();

        if (eventCalendar == null) {
            return false;
        }

        long triggerAt = eventCalendar.getTimeInMillis()
                - minutesBefore * 60L * 1000L;

        if (triggerAt <= System.currentTimeMillis()) {
            return false;
        }

        Intent intent = new Intent(this, EventReminderReceiver.class);
        intent.putExtra("eventId", eventId);
        intent.putExtra("eventName", eventName);
        intent.putExtra("eventDate", eventDate);
        intent.putExtra("eventLocation", eventLocation);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                getReminderRequestCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager == null) {
            return false;
        }

        // Inexact alarm avoids requiring special exact-alarm access.
        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent);

        return true;
    }

    private void cancelEventReminder() {
        Intent intent = new Intent(this, EventReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                getReminderRequestCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        pendingIntent.cancel();
    }

    private int getReminderRequestCode() {
        String key = (mAuth.getCurrentUser() != null
                ? mAuth.getCurrentUser().getUid() : "")
                + "_" + eventId;

        return key.hashCode() & 0x7fffffff;
    }

    private Calendar parseEventDateTime() {
        if (eventDate == null || eventDate.trim().isEmpty()
                || eventTime == null || eventTime.trim().isEmpty()) {
            return null;
        }

        String[] datePatterns = {
                "dd/MM/yyyy", "d/M/yyyy",
                "dd-MM-yyyy", "d-M-yyyy",
                "yyyy-MM-dd", "yyyy/MM/dd",
                "dd MMM yyyy", "d MMM yyyy",
                "dd MMMM yyyy", "d MMMM yyyy"
        };

        String[] timePatterns = {
                "h:mm a", "hh:mm a",
                "HH:mm", "H:mm", "h a", "ha"
        };

        Date parsedDate = null;
        Date parsedTime = null;

        for (String pattern : datePatterns) {
            try {
                SimpleDateFormat format =
                        new SimpleDateFormat(pattern, Locale.ENGLISH);
                format.setLenient(false);
                parsedDate = format.parse(eventDate.trim());

                if (parsedDate != null) {
                    break;
                }
            } catch (ParseException ignored) {
            }
        }

        for (String pattern : timePatterns) {
            try {
                SimpleDateFormat format =
                        new SimpleDateFormat(pattern, Locale.ENGLISH);
                format.setLenient(false);
                parsedTime = format.parse(eventTime.trim());

                if (parsedTime != null) {
                    break;
                }
            } catch (ParseException ignored) {
            }
        }

        if (parsedDate == null || parsedTime == null) {
            return null;
        }

        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(parsedDate);

        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(parsedTime);

        dateCalendar.set(Calendar.HOUR_OF_DAY,
                timeCalendar.get(Calendar.HOUR_OF_DAY));
        dateCalendar.set(Calendar.MINUTE,
                timeCalendar.get(Calendar.MINUTE));
        dateCalendar.set(Calendar.SECOND, 0);
        dateCalendar.set(Calendar.MILLISECOND, 0);

        return dateCalendar;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Event Reminders",
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(
                    "Notifications before bookmarked events");

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // ---------------------------------------------------------
    // SHARE EVENT
    // ---------------------------------------------------------

    private void shareEvent() {
        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Event link unavailable",
                    Toast.LENGTH_SHORT).show();
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
            message.append("📍 Location: ")
                    .append(eventLocation).append("\n");
        }

        if (eventLink != null && !eventLink.trim().isEmpty()) {
            message.append("\nView event:\n").append(eventLink);
        } else {
            message.append("\nOpen Festive Hub to view this event.");
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, eventName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message.toString());

        startActivity(Intent.createChooser(shareIntent, "Share event"));
    }

    // ---------------------------------------------------------
    // QR CODE
    // ---------------------------------------------------------

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
                                EventQrActivity.class);

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

    // ---------------------------------------------------------
    // EVENT REGISTRATION
    // ---------------------------------------------------------

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
        if (btnBookEvent != null) {
            btnBookEvent.setText("Joined");
            btnBookEvent.setEnabled(true);
        }
    }

    private void updateButtonToJoin() {
        if (btnBookEvent != null) {
            btnBookEvent.setText("Join Event");
            btnBookEvent.setEnabled(true);
        }
    }

    // ---------------------------------------------------------
    // ADD TO MY SCHEDULE (EXISTING FEATURE)
    // ---------------------------------------------------------

    private void addToMySchedule() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String scheduleId = userId + "_" + eventId;

        Map<String, Object> schedule = new HashMap<>();
        schedule.put("userId", userId);
        schedule.put("eventId", eventId);
        schedule.put("eventName", eventName);
        schedule.put("eventDate", eventDate);
        schedule.put("eventTime", eventTime);
        schedule.put("eventLocation", eventLocation);
        schedule.put("category", category);
        schedule.put("description", description);

        btnAddToMySchedule.setEnabled(false);

        db.collection("mySchedules")
                .document(scheduleId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        btnAddToMySchedule.setText(
                                "Added to My Schedule");
                        btnAddToMySchedule.setEnabled(true);

                        Toast.makeText(this,
                                "This event is already in your schedule",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("mySchedules")
                            .document(scheduleId)
                            .set(schedule)
                            .addOnSuccessListener(unused -> {
                                btnAddToMySchedule.setText(
                                        "Added to My Schedule");
                                btnAddToMySchedule.setEnabled(true);

                                Toast.makeText(this,
                                        "Event added to your schedule!",
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                btnAddToMySchedule.setEnabled(true);
                                Toast.makeText(this,
                                        "Unable to add event to schedule: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnAddToMySchedule.setEnabled(true);

                    new AlertDialog.Builder(EventDetail.this)
                            .setTitle("Schedule Error")
                            .setMessage("Error Type: "
                                    + e.getClass().getSimpleName()
                                    + "\nMessage: "
                                    + (e.getMessage() != null
                                    ? e.getMessage()
                                    : "No additional details available"))
                            .setPositiveButton("OK", null)
                            .show();
                });
    }
}
