package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView categoryAll;
    private TextView categoryFestivals;
    private TextView categoryMusic;
    private TextView categoryFood;
    private TextView categoryCulture;

    private Button btnFilter;
    private View btnVolunteer;

    private ViewPager2 bannerCarousel;
    private LinearLayout bannerDots;
    private LinearLayout eventsContainer;
    private TextView tvNoEvents;
    private EditText etSearchEvents;

    private String selectedCategory = "All";

    private static final String VOLUNTEER_EMAIL =
            "test@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton profile = findViewById(R.id.btnProfile);
        ImageButton settings = findViewById(R.id.btnSettings);

        btnVolunteer = findViewById(R.id.btnVolunteer);

        profile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    Profile.class
            );
            startActivity(intent);
        });

        settings.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SettingsActivity.class
            );
            startActivity(intent);
        });

        setupVolunteerButton();

        categoryAll = findViewById(R.id.categoryAll);
        categoryFestivals = findViewById(R.id.categoryFestivals);
        categoryMusic = findViewById(R.id.categoryMusic);
        categoryFood = findViewById(R.id.categoryFood);
        categoryCulture = findViewById(R.id.categoryCulture);

        btnFilter = findViewById(R.id.btnFilter);

        bannerCarousel = findViewById(R.id.bannerCarousel);
        bannerDots = findViewById(R.id.bannerDots);

        eventsContainer = findViewById(R.id.eventsContainer);
        tvNoEvents = findViewById(R.id.tvNoEvents);
        etSearchEvents = findViewById(R.id.etSearchEvents);

        setupBannerCarousel();

        categoryAll.setSelected(true);

        categoryAll.setOnClickListener(v ->
                selectCategory("All")
        );

        categoryFestivals.setOnClickListener(v ->
                selectCategory("Festivals")
        );

        categoryMusic.setOnClickListener(v ->
                selectCategory("Music")
        );

        categoryFood.setOnClickListener(v ->
                selectCategory("Food")
        );

        categoryCulture.setOnClickListener(v ->
                selectCategory("Culture")
        );

        btnFilter.setOnClickListener(v ->
                showFilterDialog()
        );

        etSearchEvents.setOnEditorActionListener(
                (v, actionId, event) -> {
                    loadEvents();
                    return false;
                }
        );

        loadEvents();
    }

    private void setupVolunteerButton() {

        if (btnVolunteer == null) {
            return;
        }

        FirebaseUser currentUser =
                mAuth.getCurrentUser();

        if (currentUser != null) {

            String email =
                    currentUser.getEmail();

            if (email != null &&
                    email.equalsIgnoreCase(
                            VOLUNTEER_EMAIL
                    )) {

                btnVolunteer.setVisibility(
                        View.VISIBLE
                );

                btnVolunteer.setOnClickListener(v -> {

                    Intent intent =
                            new Intent(
                                    MainActivity.this,
                                    VolunteerActivity.class
                            );

                    startActivity(intent);
                });

            } else {

                btnVolunteer.setVisibility(
                        View.GONE
                );
            }

        } else {

            btnVolunteer.setVisibility(
                    View.GONE
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null) {
            loadEvents();
        }

        if (mAuth != null) {
            setupVolunteerButton();
        }
    }

    private void setupBannerCarousel() {

        EventBannerAdapter adapter =
                new EventBannerAdapter();

        bannerCarousel.setAdapter(adapter);

        createDots(adapter.getItemCount());

        bannerCarousel.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {

                    @Override
                    public void onPageSelected(
                            int position
                    ) {
                        updateDots(position);
                    }
                }
        );
    }

    private void createDots(int count) {

        bannerDots.removeAllViews();

        for (int i = 0; i < count; i++) {

            TextView dot =
                    new TextView(this);

            dot.setText("●");
            dot.setTextSize(10);

            dot.setTextColor(
                    i == 0
                            ? getColor(
                            android.R.color.black
                    )
                            : getColor(
                            android.R.color.darker_gray
                    )
            );

            dot.setPadding(
                    5,
                    0,
                    5,
                    0
            );

            bannerDots.addView(dot);
        }
    }

    private void updateDots(int position) {

        for (int i = 0;
             i < bannerDots.getChildCount();
             i++) {

            TextView dot =
                    (TextView) bannerDots.getChildAt(i);

            dot.setTextColor(
                    i == position
                            ? getColor(
                            android.R.color.black
                    )
                            : getColor(
                            android.R.color.darker_gray
                    )
            );
        }
    }

    private void loadEvents() {

        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    eventsContainer.removeAllViews();

                    String searchText =
                            etSearchEvents.getText()
                                    .toString()
                                    .trim()
                                    .toLowerCase();

                    int eventCount = 0;

                    for (QueryDocumentSnapshot document :
                            querySnapshot) {

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

                        if (eventName == null) {
                            eventName = "Event";
                        }

                        if (eventDate == null) {
                            eventDate =
                                    "Date not available";
                        }

                        if (eventTime == null) {
                            eventTime =
                                    "Time not available";
                        }

                        if (eventLocation == null) {
                            eventLocation =
                                    "Location not available";
                        }

                        if (category == null) {
                            category = "";
                        }

                        boolean categoryMatches =
                                selectedCategory.equals("All")
                                        ||
                                        category.equalsIgnoreCase(
                                                selectedCategory
                                        );

                        boolean searchMatches =
                                searchText.isEmpty()
                                        ||
                                        eventName
                                                .toLowerCase()
                                                .contains(
                                                        searchText
                                                )
                                        ||
                                        eventLocation
                                                .toLowerCase()
                                                .contains(
                                                        searchText
                                                )
                                        ||
                                        category
                                                .toLowerCase()
                                                .contains(
                                                        searchText
                                                );

                        if (!categoryMatches ||
                                !searchMatches) {
                            continue;
                        }

                        addEventCard(
                                eventId,
                                eventName,
                                eventDate,
                                eventTime,
                                eventLocation,
                                category
                        );

                        eventCount++;
                    }

                    if (eventCount == 0) {

                        tvNoEvents.setVisibility(
                                View.VISIBLE
                        );

                    } else {

                        tvNoEvents.setVisibility(
                                View.GONE
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    tvNoEvents.setVisibility(
                            View.VISIBLE
                    );

                    tvNoEvents.setText(
                            "Unable to load events"
                    );

                    Toast.makeText(
                            this,
                            "Unable to load events",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void addEventCard(
            String eventId,
            String eventName,
            String eventDate,
            String eventTime,
            String eventLocation,
            String category
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                14,
                14,
                14,
                14
        );

        card.setBackgroundResource(
                R.drawable.bg_card
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        120
                );

        cardParams.setMargins(
                0,
                0,
                0,
                12
        );

        card.setLayoutParams(cardParams);

        LinearLayout dateBox =
                new LinearLayout(this);

        dateBox.setOrientation(
                LinearLayout.VERTICAL
        );

        dateBox.setGravity(
                android.view.Gravity.CENTER
        );

        dateBox.setBackgroundResource(
                R.drawable.bg_date
        );

        LinearLayout.LayoutParams dateParams =
                new LinearLayout.LayoutParams(
                        75,
                        85
                );

        dateBox.setLayoutParams(dateParams);

        String day =
                getDay(eventDate);

        String month =
                getMonth(eventDate);

        TextView tvDay =
                new TextView(this);

        tvDay.setText(day);

        tvDay.setTextColor(
                getColor(
                        android.R.color.holo_orange_dark
                )
        );

        tvDay.setTextSize(25);

        tvDay.setGravity(
                android.view.Gravity.CENTER
        );

        tvDay.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView tvMonth =
                new TextView(this);

        tvMonth.setText(month);

        tvMonth.setTextColor(
                getColor(
                        android.R.color.darker_gray
                )
        );

        tvMonth.setTextSize(11);

        tvMonth.setGravity(
                android.view.Gravity.CENTER
        );

        tvMonth.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        dateBox.addView(tvDay);
        dateBox.addView(tvMonth);

        LinearLayout info =
                new LinearLayout(this);

        info.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        infoParams.setMargins(
                15,
                0,
                8,
                0
        );

        info.setLayoutParams(infoParams);

        TextView tvName =
                new TextView(this);

        tvName.setText(eventName);

        tvName.setTextColor(
                getColor(
                        android.R.color.black
                )
        );

        tvName.setTextSize(16);

        tvName.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView tvDetails =
                new TextView(this);

        tvDetails.setText(
                category.isEmpty()
                        ? eventTime
                        : eventTime + " • " + category
        );

        tvDetails.setTextColor(
                getColor(
                        android.R.color.darker_gray
                )
        );

        tvDetails.setTextSize(12);

        TextView tvLocation =
                new TextView(this);

        tvLocation.setText(
                "📍 " + eventLocation
        );

        tvLocation.setTextColor(
                getColor(
                        android.R.color.darker_gray
                )
        );

        tvLocation.setTextSize(11);

        info.addView(tvName);

        LinearLayout.LayoutParams detailsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        detailsParams.topMargin = 5;

        info.addView(
                tvDetails,
                detailsParams
        );

        LinearLayout.LayoutParams locationParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        locationParams.topMargin = 5;

        info.addView(
                tvLocation,
                locationParams
        );

        TextView arrow =
                new TextView(this);

        arrow.setText("›");

        arrow.setTextColor(
                getColor(
                        android.R.color.holo_orange_dark
                )
        );

        arrow.setTextSize(28);

        card.addView(dateBox);
        card.addView(info);
        card.addView(arrow);

        card.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            EventDetail.class
                    );

            intent.putExtra(
                    "eventId",
                    eventId
            );

            startActivity(intent);
        });

        eventsContainer.addView(card);
    }

    private String getDay(String date) {

        try {

            String[] parts =
                    date.trim().split(" ");

            if (parts.length >= 1) {

                String first =
                        parts[0]
                                .replaceAll(
                                        "[^0-9]",
                                        ""
                                );

                if (!first.isEmpty()) {
                    return first;
                }
            }

        } catch (Exception ignored) {
        }

        return "--";
    }

    private String getMonth(String date) {

        try {

            String[] parts =
                    date.trim().split(" ");

            if (parts.length >= 2) {

                return parts[1]
                        .substring(
                                0,
                                Math.min(
                                        3,
                                        parts[1].length()
                                )
                        )
                        .toUpperCase();
            }

        } catch (Exception ignored) {
        }

        return "---";
    }

    private void selectCategory(
            String category
    ) {

        selectedCategory = category;

        categoryAll.setSelected(false);
        categoryFestivals.setSelected(false);
        categoryMusic.setSelected(false);
        categoryFood.setSelected(false);
        categoryCulture.setSelected(false);

        switch (category) {

            case "All":
                categoryAll.setSelected(true);
                break;

            case "Festivals":
                categoryFestivals.setSelected(true);
                break;

            case "Music":
                categoryMusic.setSelected(true);
                break;

            case "Food":
                categoryFood.setSelected(true);
                break;

            case "Culture":
                categoryCulture.setSelected(true);
                break;
        }

        loadEvents();
    }

    private void showFilterDialog() {

        String[] filters = {
                "All Events",
                "Today",
                "This Weekend",
                "This Month"
        };

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle(
                "Filter Events"
        );

        builder.setSingleChoiceItems(
                filters,
                0,
                (dialog, which) -> {

                    Toast.makeText(
                            MainActivity.this,
                            filters[which] +
                                    " selected",
                            Toast.LENGTH_SHORT
                    ).show();

                    dialog.dismiss();
                }
        );

        builder.setNegativeButton(
                "Cancel",
                null
        );

        builder.show();
    }
}