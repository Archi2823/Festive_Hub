package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextView categoryAll;
    private TextView categoryFestivals;
    private TextView categoryMusic;
    private TextView categoryFood;
    private TextView categoryCulture;

    private Button btnFilter;

    private ViewPager2 bannerCarousel;
    private LinearLayout bannerDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        ImageButton profile = findViewById(R.id.btnProfile);

        LinearLayout cardFestivals = findViewById(R.id.cardFestivals);
        LinearLayout cardEvents = findViewById(R.id.cardEvents);
        LinearLayout eventCard = findViewById(R.id.eventCard);

        categoryAll = findViewById(R.id.categoryAll);
        categoryFestivals = findViewById(R.id.categoryFestivals);
        categoryMusic = findViewById(R.id.categoryMusic);
        categoryFood = findViewById(R.id.categoryFood);
        categoryCulture = findViewById(R.id.categoryCulture);

        btnFilter = findViewById(R.id.btnFilter);

        bannerCarousel = findViewById(R.id.bannerCarousel);
        bannerDots = findViewById(R.id.bannerDots);

        setupBannerCarousel();

        categoryAll.setSelected(true);

        profile.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    Profile.class
            );

            startActivity(intent);
        });

        cardFestivals.setOnClickListener(v ->
                Toast.makeText(
                        MainActivity.this,
                        "Festivals coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );

        cardEvents.setOnClickListener(v ->
                Toast.makeText(
                        MainActivity.this,
                        "Events coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );

        eventCard.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    EventDetail.class
            );
            startActivity(intent);
        });

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
    }

    private void setupBannerCarousel() {

        EventBannerAdapter adapter = new EventBannerAdapter();

        bannerCarousel.setAdapter(adapter);

        createDots(adapter.getItemCount());

        bannerCarousel.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {

                    @Override
                    public void onPageSelected(int position) {
                        updateDots(position);
                    }
                }
        );
    }

    private void createDots(int count) {

        bannerDots.removeAllViews();

        for (int i = 0; i < count; i++) {

            TextView dot = new TextView(this);

            dot.setText("●");
            dot.setTextSize(10);
            dot.setTextColor(
                    i == 0
                            ? getColor(android.R.color.black)
                            : getColor(android.R.color.darker_gray)
            );

            dot.setPadding(5, 0, 5, 0);

            bannerDots.addView(dot);
        }
    }

    private void updateDots(int position) {

        for (int i = 0; i < bannerDots.getChildCount(); i++) {

            TextView dot =
                    (TextView) bannerDots.getChildAt(i);

            dot.setTextColor(
                    i == position
                            ? getColor(android.R.color.black)
                            : getColor(android.R.color.darker_gray)
            );
        }
    }

    private void selectCategory(String category) {

        Toast.makeText(
                MainActivity.this,
                category + " events selected",
                Toast.LENGTH_SHORT
        ).show();

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

        builder.setTitle("Filter Events");

        builder.setSingleChoiceItems(
                filters,
                0,
                (dialog, which) -> {

                    Toast.makeText(
                            MainActivity.this,
                            filters[which] + " selected",
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