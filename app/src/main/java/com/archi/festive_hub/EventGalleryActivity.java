
package com.archi.festive_hub;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventGalleryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    private GridView gridGallery;
    private TextView tvGalleryTitle;
    private TextView tvGalleryCount;
    private TextView tvGalleryEmpty;

    private final List<String> imageUrls = new ArrayList<>();

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_gallery);

        db = FirebaseFirestore.getInstance();

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        tvGalleryTitle = findViewById(R.id.tvGalleryTitle);
        tvGalleryCount = findViewById(R.id.tvGalleryCount);
        tvGalleryEmpty = findViewById(R.id.tvGalleryEmpty);
        gridGallery = findViewById(R.id.gridGallery);

        findViewById(R.id.btnGalleryBack)
                .setOnClickListener(v -> finish());

        tvGalleryTitle.setText(
                eventName == null || eventName.trim().isEmpty()
                        ? "Event Gallery"
                        : eventName + " Gallery"
        );

        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Event not found",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadGallery();
    }

    private void loadGallery() {
        tvGalleryEmpty.setVisibility(View.VISIBLE);
        tvGalleryEmpty.setText("Loading gallery...");
        gridGallery.setVisibility(View.GONE);

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        showEmpty("Event not found.");
                        return;
                    }

                    String savedName = snapshot.getString("name");

                    if (savedName == null) {
                        savedName = snapshot.getString("eventName");
                    }

                    if (savedName != null
                            && !savedName.trim().isEmpty()) {
                        eventName = savedName;
                        tvGalleryTitle.setText(savedName + " Gallery");
                    }

                    imageUrls.clear();

                    Object value = snapshot.get("galleryUrls");

                    if (value instanceof List<?>) {
                        for (Object item : (List<?>) value) {
                            if (item instanceof String) {
                                String url = ((String) item).trim();

                                if (!url.isEmpty()
                                        && !imageUrls.contains(url)) {
                                    imageUrls.add(url);
                                }
                            }
                        }
                    }

                    // Show the event banner if no gallery photos exist.
                    if (imageUrls.isEmpty()) {
                        String bannerUrl = snapshot.getString("bannerUrl");

                        if (bannerUrl != null
                                && !bannerUrl.trim().isEmpty()) {
                            imageUrls.add(bannerUrl.trim());
                        }
                    }

                    if (imageUrls.isEmpty()) {
                        showEmpty(
                                "No photos have been added to this event yet."
                        );
                        return;
                    }

                    tvGalleryCount.setText(
                            imageUrls.size()
                                    + (imageUrls.size() == 1
                                    ? " photo"
                                    : " photos")
                    );

                    tvGalleryEmpty.setVisibility(View.GONE);
                    gridGallery.setVisibility(View.VISIBLE);
                    gridGallery.setAdapter(new GalleryAdapter());
                })
                .addOnFailureListener(e ->
                        showEmpty(
                                "Unable to load gallery. Check your connection and try again."
                        )
                );
    }

    private void showEmpty(String message) {
        gridGallery.setVisibility(View.GONE);
        tvGalleryCount.setText("0 photos");
        tvGalleryEmpty.setText(message);
        tvGalleryEmpty.setVisibility(View.VISIBLE);
    }

    private class GalleryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return imageUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return imageUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {

            LinearLayout itemLayout;
            ImageView imageView;
            TextView caption;

            if (convertView instanceof LinearLayout) {
                itemLayout = (LinearLayout) convertView;
                imageView = (ImageView) itemLayout.getChildAt(0);
                caption = (TextView) itemLayout.getChildAt(1);
            } else {
                itemLayout = new LinearLayout(
                        EventGalleryActivity.this);

                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setGravity(Gravity.CENTER);
                itemLayout.setPadding(
                        dp(6), dp(6), dp(6), dp(10));

                GradientDrawable cardBackground =
                        new GradientDrawable();
                cardBackground.setColor(Color.WHITE);
                cardBackground.setCornerRadius(dp(12));
                cardBackground.setStroke(dp(1), 0xFFECE3DB);

                itemLayout.setBackground(cardBackground);
                itemLayout.setClipToOutline(true);

                itemLayout.setLayoutParams(
                        new GridView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                );

                imageView = new ImageView(
                        EventGalleryActivity.this);

                imageView.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                dp(140)
                        )
                );

                imageView.setScaleType(
                        ImageView.ScaleType.CENTER_CROP);

                imageView.setBackgroundColor(0xFFF2EDE8);
                imageView.setContentDescription("Gallery photo");

                caption = new TextView(
                        EventGalleryActivity.this);

                caption.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                );

                caption.setGravity(Gravity.CENTER);
                caption.setTextColor(0xFF352A25);
                caption.setTextSize(13);
                caption.setTypeface(
                        null, android.graphics.Typeface.BOLD);
                caption.setPadding(
                        dp(4), dp(10), dp(4), dp(2));
                caption.setMaxLines(2);

                itemLayout.addView(imageView);
                itemLayout.addView(caption);
            }

            caption.setText("Photo " + (position + 1));

            String imageUrl = imageUrls.get(position);

            Glide.with(EventGalleryActivity.this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(imageView);

            View.OnClickListener openPhoto =
                    v -> showFullImage(imageUrl);

            imageView.setOnClickListener(openPhoto);
            itemLayout.setOnClickListener(openPhoto);

            return itemLayout;
        }
    }

    private void showFullImage(String url) {
        ImageView imageView = new ImageView(this);

        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        int padding = dp(8);
        imageView.setPadding(
                padding, padding, padding, padding);

        Glide.with(this)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .fitCenter()
                .into(imageView);

        new AlertDialog.Builder(this)
                .setTitle(eventName == null
                        ? "Event Photo" : eventName)
                .setView(imageView)
                .setPositiveButton("Close", null)
                .show();
    }
}
