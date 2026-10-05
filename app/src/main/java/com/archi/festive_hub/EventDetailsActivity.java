package com.archi.festive_hub;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView tvEventName;
    private TextView tvEventDate;
    private TextView tvEventTime;
    private TextView tvEventLocation;
    private TextView tvEventCategory;
    private TextView tvEventDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        tvEventName = findViewById(R.id.tvEventName);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventTime = findViewById(R.id.tvEventTime);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        tvEventCategory = findViewById(R.id.tvEventCategory);
        tvEventDescription = findViewById(R.id.tvEventDescription);

        View btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        String eventName = getIntent().getStringExtra("eventName");
        String eventDate = getIntent().getStringExtra("eventDate");
        String eventTime = getIntent().getStringExtra("eventTime");
        String eventLocation = getIntent().getStringExtra("eventLocation");
        String eventCategory = getIntent().getStringExtra("eventCategory");
        String eventDescription = getIntent().getStringExtra("eventDescription");

        tvEventName.setText(
                eventName != null && !eventName.isEmpty()
                        ? eventName
                        : "Event"
        );

        tvEventDate.setText(
                eventDate != null && !eventDate.isEmpty()
                        ? eventDate
                        : "N/A"
        );

        tvEventTime.setText(
                eventTime != null && !eventTime.isEmpty()
                        ? eventTime
                        : "N/A"
        );

        tvEventLocation.setText(
                eventLocation != null && !eventLocation.isEmpty()
                        ? eventLocation
                        : "N/A"
        );

        tvEventCategory.setText(
                eventCategory != null && !eventCategory.isEmpty()
                        ? eventCategory
                        : "N/A"
        );

        tvEventDescription.setText(
                eventDescription != null && !eventDescription.isEmpty()
                        ? eventDescription
                        : "No description available"
        );
    }
}