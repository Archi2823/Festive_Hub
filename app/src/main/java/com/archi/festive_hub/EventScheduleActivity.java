
package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
    private EditText etSearch;
    private LinearLayout eventsContainer;
    private TextView tvNoEvents, tvEventCount;

    private TextView tabAll, tabUpcoming, tabOngoing, tabCompleted;

    private final List<EventItem> allEvents = new ArrayList<>();
    private String selectedFilter = "All";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_schedule);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnScheduleBack);
        etSearch = findViewById(R.id.etScheduleSearch);
        eventsContainer = findViewById(R.id.scheduleEventsContainer);
        tvNoEvents = findViewById(R.id.tvScheduleNoEvents);
        tvEventCount = findViewById(R.id.tvScheduleEventCount);

        tabAll = findViewById(R.id.tabScheduleAll);
        tabUpcoming = findViewById(R.id.tabScheduleUpcoming);
        tabOngoing = findViewById(R.id.tabScheduleOngoing);
        tabCompleted = findViewById(R.id.tabScheduleCompleted);

        btnBack.setOnClickListener(v -> finish());

        tabAll.setOnClickListener(v -> selectFilter("All"));
        tabUpcoming.setOnClickListener(v -> selectFilter("Upcoming"));
        tabOngoing.setOnClickListener(v -> selectFilter("Ongoing"));
        tabCompleted.setOnClickListener(v -> selectFilter("Completed"));

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
        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && eventsContainer != null) {
            loadEvents();
        }
    }

    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allEvents.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        EventItem item = new EventItem();

                        item.id = doc.getId();
                        item.name = firstNonEmpty(
                                doc.getString("eventName"),
                                doc.getString("name"), "Event");

                        item.date = firstNonEmpty(
                                doc.getString("eventDate"),
                                doc.getString("date"), "");

                        item.time = firstNonEmpty(
                                doc.getString("eventTime"),
                                doc.getString("time"), "");

                        item.location = firstNonEmpty(
                                doc.getString("eventLocation"),
                                doc.getString("location"),
                                "Location not available");

                        item.category = firstNonEmpty(
                                doc.getString("category"), "", "");

                        item.bannerUrl = firstNonEmpty(
                                doc.getString("bannerUrl"), "", "");

                        item.status = getStatus(item.date, item.time);

                        allEvents.add(item);
                    }

                    renderEvents();
                })
                .addOnFailureListener(e -> {
                    tvNoEvents.setText("Unable to load events.");
                    tvNoEvents.setVisibility(View.VISIBLE);

                    Toast.makeText(this,
                            "Unable to load events",
                            Toast.LENGTH_SHORT).show();
                });
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
        if (eventsContainer == null) return;

        eventsContainer.removeAllViews();
        int count = 0;

        for (EventItem item : allEvents) {

            String searchable = (
                    item.name + " " +
                            item.location + " " +
                            item.category + " " +
                            item.date
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

        tvEventCount.setText(count +
                (count == 1 ? " event" : " events"));

        if (count == 0) {
            tvNoEvents.setText(allEvents.isEmpty()
                    ? "No events available yet."
                    : "No events match this search or filter.");

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

        // Unknown date format: don't incorrectly mark as completed.
        if (eventDate == null) return "Upcoming";

        Calendar eventDay = Calendar.getInstance();
        eventDay.setTime(eventDate);
        clearTime(eventDay);

        Calendar today = Calendar.getInstance();
        clearTime(today);

        if (eventDay.before(today)) return "Completed";
        if (eventDay.after(today)) return "Upcoming";

        int[] range = parseTimeRange(timeString);

        if (range == null) return "Ongoing";

        Calendar now = Calendar.getInstance();

        int currentMinutes =
                now.get(Calendar.HOUR_OF_DAY) * 60
                        + now.get(Calendar.MINUTE);

        if (currentMinutes < range[0]) return "Upcoming";
        if (currentMinutes > range[1]) return "Completed";

        return "Ongoing";
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;

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
            }
        }

        return null;
    }

    private int[] parseTimeRange(String value) {
        if (value == null || value.trim().isEmpty()) return null;

        String[] parts = value.split("\\s*[-–—]\\s*");

        if (parts.length < 2) return null;

        Integer start = parseTime(parts[0].trim());
        Integer end = parseTime(parts[1].trim());

        if (start == null || end == null) return null;

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
