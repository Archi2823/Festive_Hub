package com.archi.festive_hub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextView categoryAll;
    private TextView categoryFestivals;
    private TextView categoryMusic;
    private TextView categoryFood;
    private TextView categoryCulture;
    private Button btnFilter;

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
        categoryAll.setSelected(true);
        profile.setOnClickListener(v ->
                Toast.makeText(
                        MainActivity.this,
                        "Profile coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );

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

        eventCard.setOnClickListener(v ->
                Toast.makeText(
                        MainActivity.this,
                        "Event details coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );

        categoryAll.setOnClickListener(v -> selectCategory("All"));
        categoryFestivals.setOnClickListener(v -> selectCategory("Festivals"));
        categoryMusic.setOnClickListener(v -> selectCategory("Music"));
        categoryFood.setOnClickListener(v -> selectCategory("Food"));
        categoryCulture.setOnClickListener(v -> selectCategory("Culture"));

        btnFilter.setOnClickListener(v -> showFilterDialog());
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