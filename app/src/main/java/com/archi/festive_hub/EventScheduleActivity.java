
package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventScheduleActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText etSearch;
    private LinearLayout eventsContainer;
    private TextView tvNoEvents;
    private TextView tvEventCount;
    private TextView tvScheduleTitle;
    private TextView tvScheduleSubtitle;
    private TextView tvScheduleListHeading;

    private Button btnMyScheduleMode;

    private TextView tabAll;
    private TextView tabUpcoming;
    private TextView tabOngoing;
    private TextView tabCompleted;

    private final List<EventItem> allEvents = new ArrayList<>();

    private String selectedFilter = "All";
    private String searchQuery = "";

    // false = all available events
    // true = events saved by the current user
    private boolean myScheduleMode = false;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_schedule);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        ImageButton btnBack = findViewById(R.id.btnScheduleBack);

        etSearch = findViewById(R.id.etScheduleSearch);
        eventsContainer = findViewById(R.id.scheduleEventsContainer);
        tvNoEvents = findViewById(R.id.tvScheduleNoEvents);
        tvEventCount = findViewById(R.id.tvScheduleEventCount);

        tabAll = findViewById(R.id.tabScheduleAll);
        tabUpcoming = findViewById(R.id.tabScheduleUpcoming);
        tabOngoing = findViewById(R.id.tabScheduleOngoing);
        tabCompleted = findViewById(R.id.tabScheduleCompleted);

        btnMyScheduleMode = findViewById(R.id.btnMyScheduleMode);
        tvScheduleTitle = findViewById(R.id.tvScheduleTitle);
        tvScheduleSubtitle = findViewById(R.id.tvScheduleSubtitle);
        tvScheduleListHeading = findViewById(R.id.tvScheduleListHeading);

        btnBack.setOnClickListener(v -> finish());

        tabAll.setOnClickListener(v -> selectFilter("All"));
        tabUpcoming.setOnClickListener(v -> selectFilter("Upcoming"));
        tabOngoing.setOnClickListener(v -> selectFilter("Ongoing"));
        tabCompleted.setOnClickListener(v -> selectFilter("Completed"));

        btnMyScheduleMode.setOnClickListener(v -> {
            myScheduleMode = !myScheduleMode;

            updateScreenMode();
            loadEvents();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                searchQuery = s.toString()
                        .trim().toLowerCase(Locale.ROOT);

                renderEvents();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        selectFilter("All");
        updateScreenMode();
        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && eventsContainer != null) {
            loadEvents();
        }
    }

    private void updateScreenMode() {
        if (myScheduleMode) {
            tvScheduleTitle.setText("My Schedule");
            tvScheduleSubtitle.setText("Your saved events");
            tvScheduleListHeading.setText("My Saved Events");
            btnMyScheduleMode.setText("All Events");
        } else {
            tvScheduleTitle.setText("Event Schedule");
            tvScheduleSubtitle.setText("Find your next celebration");
            tvScheduleListHeading.setText("All Events");
            btnMyScheduleMode.setText("My Schedule");
        }

        tvNoEvents.setText(myScheduleMode
                ? "You haven't added any events to your schedule yet."
                : "No events available yet.");
    }

    private void loadEvents() {
        if (isLoading) {
            return;
        }

        if (myScheduleMode && auth.getCurrentUser() == null) {
            allEvents.clear();
            renderEvents();

            Toast.makeText(this,
                    "Please login to view your schedule",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        isLoading = true;

        tvNoEvents.setVisibility(View.GONE);

        if (!myScheduleMode) {
            loadAllEvents();
        } else {
            loadMySchedule();
        }
    }

    private void loadAllEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allEvents.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        EventItem item = createEventItem(doc.getId(), doc);

                        allEvents.add(item);
                    }

                    isLoading = false;
                    renderEvents();
                })
                .addOnFailureListener(e -> {
                    isLoading = false;
                    showLoadError("Unable to load events", e);
                });
    }

    private void loadMySchedule() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("mySchedules")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    allEvents.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String savedEventId = doc.getString("eventId");

                        if (savedEventId == null
                                || savedEventId.trim().isEmpty()) {
                            continue;
                        }

                        EventItem item = createSavedEventItem(
                                savedEventId, doc);

                        allEvents.add(item);
                    }

                    isLoading = false;
                    renderEvents();
                })
                .addOnFailureListener(e -> {
                    isLoading = false;
                    showLoadError(
                            "Unable to load your saved events", e);
                });
    }

    private EventItem createEventItem(
            String id, DocumentSnapshot doc) {

        EventItem item = new EventItem();

        item.id = id;

        item.name = firstNonEmpty(
                doc.getString("eventName"),
                doc.getString("name"),
                "Event");

        item.date = firstNonEmpty(
                doc.getString("eventDate"),
                doc.getString("date"),
                "");

        item.time = firstNonEmpty(
                doc.getString("eventTime"),
                doc.getString("time"),
                "");

        item.location = firstNonEmpty(
                doc.getString("eventLocation"),
                doc.getString("location"),
                "Location not available");

        item.category = firstNonEmpty(
                doc.getString("category"),
                "",
                "");

        item.bannerUrl = firstNonEmpty(
                doc.getString("bannerUrl"),
                "",
                "");

        item.status = getStatus(item.date, item.time);

        return item;
    }

    private EventItem createSavedEventItem(
            String eventId, DocumentSnapshot savedDoc) {

        EventItem item = new EventItem();

        item.id = eventId;

        item.name = firstNonEmpty(
                savedDoc.getString("eventName"),
                "",
                "Event");

        item.date = firstNonEmpty(
                savedDoc.getString("eventDate"),
                "",
                "");

        item.time = firstNonEmpty(
                savedDoc.getString("eventTime"),
                "",
                "");

        item.location = firstNonEmpty(
                savedDoc.getString("eventLocation"),
                "",
                "Location not available");

        item.category = firstNonEmpty(
                savedDoc.getString("category"),
                "",
                "");

        item.bannerUrl = "";

        item.status = getStatus(item.date, item.time);

        // Refresh details from the original event document.
        // If the event has been deleted or cannot be read,
        // the saved copy will still be displayed.
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (eventDoc.exists()) {
                        item.name = firstNonEmpty(
                                eventDoc.getString("eventName"),
                                eventDoc.getString("name"),
                                item.name);

                        item.date = firstNonEmpty(
                                eventDoc.getString("eventDate"),
                                eventDoc.getString("date"),
                                item.date);

                        item.time = firstNonEmpty(
                                eventDoc.getString("eventTime"),
                                eventDoc.getString("time"),
                                item.time);

                        item.location = firstNonEmpty(
                                eventDoc.getString("eventLocation"),
                                eventDoc.getString("location"),
                                item.location);

                        item.category = firstNonEmpty(
                                eventDoc.getString("category"),
                                "",
                                item.category);

                        item.bannerUrl = firstNonEmpty(
                                eventDoc.getString("bannerUrl"),
                                "",
                                "");

                        item.status = getStatus(item.date, item.time);

                        renderEvents();
                    }
                });

        return item;
    }

    private void showLoadError(String message, Exception e) {
        tvNoEvents.setText(message + ".");

        tvNoEvents.setVisibility(View.VISIBLE);

        Toast.makeText(this,
                message + ": " + e.getMessage(),
                Toast.LENGTH_LONG).show();

        android.util.Log.e("EventSchedule", message, e);
    }

    private String firstNonEmpty(
            String first, String second, String fallback) {

        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }

        if (second != null && !second.trim().isEmpty()) {
            return second.trim();
        }

        return fallback;
    }

    private void selectFilter(String filter) {
        selectedFilter = filter;

        setTabStyle(tabAll, filter.equals("All"));
        setTabStyle(tabUpcoming, filter.equals("Upcoming"));
        setTabStyle(tabOngoing, filter.equals("Ongoing"));
        setTabStyle(tabCompleted, filter.equals("Completed"));

        renderEvents();
    }

    private void setTabStyle(TextView tab, boolean selected) {
        tab.setSelected(selected);

        tab.setBackgroundResource(selected
                ? R.drawable.bg_schedule_tab_selected
                : R.drawable.bg_schedule_tab);

        tab.setTextColor(selected
                ? 0xFFFFFFFF
                : 0xFF514840);
    }

    private void renderEvents() {
        if (eventsContainer == null) {
            return;
        }

        eventsContainer.removeAllViews();

        int count = 0;

        for (EventItem item : allEvents) {
            String searchable = (
                    item.name + " "
                            + item.location + " "
                            + item.category + " "
                            + item.date
            ).toLowerCase(Locale.ROOT);

            if (!searchQuery.isEmpty()
                    && !searchable.contains(searchQuery)) {
                continue;
            }

            if (!selectedFilter.equals("All")
                    && !item.status.equals(selectedFilter)) {
                continue;
            }

            View card = LayoutInflater.from(this).inflate(
                    R.layout.item_event_schedule,
                    eventsContainer,
                    false);

            TextView day = card.findViewById(R.id.tvScheduleDay);
            TextView month = card.findViewById(R.id.tvScheduleMonth);
            TextView name = card.findViewById(R.id.tvScheduleEventName);
            TextView category = card.findViewById(R.id.tvScheduleCategory);
            TextView date = card.findViewById(R.id.tvScheduleDate);
            TextView time = card.findViewById(R.id.tvScheduleTime);
            TextView location = card.findViewById(R.id.tvScheduleLocation);
            TextView status = card.findViewById(R.id.tvScheduleStatus);

            android.widget.ImageView image =
                    card.findViewById(R.id.ivScheduleBanner);

            day.setText(getDay(item.date));
            month.setText(getMonth(item.date));
            name.setText(item.name);

            category.setText(item.category.isEmpty()
                    ? "Event" : item.category);

            date.setText(item.date.isEmpty()
                    ? "Date not available" : item.date);

            time.setText(item.time.isEmpty()
                    ? "Time not available" : item.time);

            location.setText(item.location);

            status.setText(item.status);
            setStatusStyle(status, item.status);

            if (!item.bannerUrl.isEmpty()) {
                Glide.with(this)
                        .load(item.bannerUrl)
                        .centerCrop()
                        .placeholder(
                                android.R.drawable.ic_menu_gallery)
                        .error(
                                android.R.drawable.ic_menu_gallery)
                        .into(image);
            } else {
                image.setImageResource(
                        android.R.drawable.ic_menu_gallery);
            }

            card.setOnClickListener(v -> {
                Intent intent = new Intent(
                        EventScheduleActivity.this,
                        EventDetail.class);

                intent.putExtra("eventId", item.id);
                startActivity(intent);
            });

            eventsContainer.addView(card);
            count++;
        }

        tvEventCount.setText(count
                + (count == 1 ? " event" : " events"));

        if (count == 0) {
            String emptyMessage;

            if (myScheduleMode) {
                emptyMessage = allEvents.isEmpty()
                        ? "You haven't added any events to your schedule yet."
                        : "No saved events match this search or filter.";
            } else {
                emptyMessage = allEvents.isEmpty()
                        ? "No events available yet."
                        : "No events match this search or filter.";
            }

            tvNoEvents.setText(emptyMessage);
            tvNoEvents.setVisibility(View.VISIBLE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
        }
    }

    private void setStatusStyle(TextView view, String status) {
        if (status.equals("Ongoing")) {
            view.setBackgroundResource(
                    R.drawable.bg_schedule_status_ongoing);
            view.setTextColor(0xFF237447);

        } else if (status.equals("Completed")) {
            view.setBackgroundResource(
                    R.drawable.bg_schedule_status_completed);
            view.setTextColor(0xFF6D625B);

        } else {
            view.setBackgroundResource(
                    R.drawable.bg_schedule_status_upcoming);
            view.setTextColor(0xFFB95112);
        }
    }

    private String getStatus(String dateString, String timeString) {
        Date eventDate = parseDate(dateString);

        if (eventDate == null) {
            return "Upcoming";
        }

        Calendar eventDay = Calendar.getInstance();
        eventDay.setTime(eventDate);
        clearTime(eventDay);

        Calendar today = Calendar.getInstance();
        clearTime(today);

        if (eventDay.before(today)) {
            return "Completed";
        }

        if (eventDay.after(today)) {
            return "Upcoming";
        }

        int[] range = parseTimeRange(timeString);

        if (range == null) {
            return "Ongoing";
        }

        Calendar now = Calendar.getInstance();

        int currentMinutes =
                now.get(Calendar.HOUR_OF_DAY) * 60
                        + now.get(Calendar.MINUTE);

        if (currentMinutes < range[0]) {
            return "Upcoming";
        }

        if (currentMinutes > range[1]) {
            return "Completed";
        }

        return "Ongoing";
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] patterns = {
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "d/M/yyyy",
                "dd MMM yyyy",
                "d MMM yyyy",
                "dd MMMM yyyy",
                "d MMMM yyyy",
                "EEEE, dd MMMM yyyy",
                "EEEE, d MMMM yyyy",
                "EEEE, dd MMM yyyy",
                "EEEE, d MMM yyyy"
        };

        for (String pattern : patterns) {
            SimpleDateFormat format =
                    new SimpleDateFormat(pattern, Locale.ENGLISH);

            format.setLenient(false);

            try {
                return format.parse(value.trim());
            } catch (ParseException ignored) {
                // Try next supported date format.
            }
        }

        return null;
    }

    private int[] parseTimeRange(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] parts = value.split("\\s*[-–—]\\s*");

        if (parts.length < 2) {
            return null;
        }

        Integer start = parseTime(parts[0].trim());
        Integer end = parseTime(parts[1].trim());

        if (start == null || end == null) {
            return null;
        }

        return new int[]{start, end};
    }

    private Integer parseTime(String value) {
        String[] patterns = {
                "h:mm a", "hh:mm a",
                "H:mm", "HH:mm",
                "h a", "hh a"
        };

        for (String pattern : patterns) {
            SimpleDateFormat format =
                    new SimpleDateFormat(pattern, Locale.ENGLISH);

            format.setLenient(false);

            try {
                Date date = format.parse(
                        value.toUpperCase(Locale.ENGLISH));

                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    return calendar.get(Calendar.HOUR_OF_DAY) * 60
                            + calendar.get(Calendar.MINUTE);
                }
            } catch (ParseException ignored) {
                // Try next time format.
            }
        }

        return null;
    }

    private String getDay(String value) {
        Date date = parseDate(value);

        if (date != null) {
            return new SimpleDateFormat(
                    "dd", Locale.ENGLISH).format(date);
        }

        return "—";
    }

    private String getMonth(String value) {
        Date date = parseDate(value);

        if (date != null) {
            return new SimpleDateFormat(
                    "MMM", Locale.ENGLISH)
                    .format(date)
                    .toUpperCase(Locale.ENGLISH);
        }

        return "DATE";
    }

    private static class EventItem {
        String id;
        String name;
        String date;
        String time;
        String location;
        String category;
        String bannerUrl;
        String status;
    }
}
