package com.archi.festive_hub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnBookEvent = findViewById(R.id.btnBookEvent);

        btnBack.setOnClickListener(v -> finish());

        btnBookEvent.setOnClickListener(v ->
                Toast.makeText(
                        EventDetail.this,
                        "Event booking coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }
}