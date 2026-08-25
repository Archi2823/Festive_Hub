package com.archi.festive_hub;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class Profile extends AppCompatActivity {

    private static final String ADMIN_EMAIL =
            "upadhyaysisters53@gmail.com";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private LinearLayout registeredEventsContainer;
    private TextView tvNoEvents;

    private ImageButton btnProfileMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack =
                findViewById(R.id.btnBack);

        btnProfileMenu =
                findViewById(R.id.btnProfileMenu);

        registeredEventsContainer =
                findViewById(R.id.registeredEventsContainer);

        tvNoEvents =
                findViewById(R.id.tvNoEvents);

        btnBack.setOnClickListener(v -> finish());

        setupProfileMenu();

        loadRegisteredEvents();
    }

    private void setupProfileMenu() {

        btnProfileMenu.setOnClickListener(v -> {

            if (mAuth.getCurrentUser() == null) {
                return;
            }

            String email =
                    mAuth.getCurrentUser().getEmail();

            if (email == null ||
                    !email.equalsIgnoreCase(ADMIN_EMAIL)) {

                return;
            }

            PopupMenu popupMenu =
                    new PopupMenu(
                            Profile.this,
                            btnProfileMenu
                    );

            popupMenu.getMenu().add(
                    "Admin Panel"
            );

            popupMenu.getMenu().add(
                    "Volunteer Scanner"
            );

            popupMenu.setOnMenuItemClickListener(
                    item -> {

                        String selected =
                                item.getTitle().toString();

                        if (selected.equals(
                                "Admin Panel"
                        )) {

                            Intent intent =
                                    new Intent(
                                            Profile.this,
                                            AdminActivity.class
                                    );

                            startActivity(intent);

                            return true;
                        }

                        if (selected.equals(
                                "Volunteer Scanner"
                        )) {

                            Intent intent =
                                    new Intent(
                                            Profile.this,
                                            VolunteerScanner.class
                                    );

                            startActivity(intent);

                            return true;
                        }

                        return false;
                    }
            );

            popupMenu.show();
        });
    }

    private void loadRegisteredEvents() {

        if (mAuth.getCurrentUser() == null) {

            tvNoEvents.setVisibility(
                    View.VISIBLE
            );

            tvNoEvents.setText(
                    "Please login to view your events."
            );

            return;
        }

        String userId =
                mAuth.getCurrentUser().getUid();

        db.collection("eventRegistrations")
                .whereEqualTo(
                        "userId",
                        userId
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    registeredEventsContainer
                            .removeAllViews();

                    if (querySnapshot.isEmpty()) {

                        tvNoEvents.setVisibility(
                                View.VISIBLE
                        );

                        tvNoEvents.setText(
                                "You haven't joined any events yet."
                        );

                        return;
                    }

                    tvNoEvents.setVisibility(
                            View.GONE
                    );

                    for (
                            QueryDocumentSnapshot document :
                            querySnapshot
                    ) {

                        String eventName =
                                document.getString(
                                        "eventName"
                                );

                        String eventDate =
                                document.getString(
                                        "eventDate"
                                );

                        String eventLocation =
                                document.getString(
                                        "eventLocation"
                                );

                        String status =
                                document.getString(
                                        "status"
                                );

                        String registrationId =
                                document.getId();

                        addEventCard(
                                eventName,
                                eventDate,
                                eventLocation,
                                status,
                                registrationId
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    tvNoEvents.setVisibility(
                            View.VISIBLE
                    );

                    tvNoEvents.setText(
                            "Unable to load registered events."
                    );

                    Toast.makeText(
                            Profile.this,
                            "Unable to load registered events",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void addEventCard(
            String eventName,
            String eventDate,
            String eventLocation,
            String status,
            String registrationId
    ) {

        View eventView =
                getLayoutInflater().inflate(
                        R.layout.item_registered_event,
                        registeredEventsContainer,
                        false
                );

        TextView tvEventName =
                eventView.findViewById(
                        R.id.tvEventName
                );

        TextView tvEventDate =
                eventView.findViewById(
                        R.id.tvEventDate
                );

        TextView tvEventLocation =
                eventView.findViewById(
                        R.id.tvEventLocation
                );

        TextView tvStatus =
                eventView.findViewById(
                        R.id.tvStatus
                );

        Button btnShowQr =
                eventView.findViewById(
                        R.id.btnShowQr
                );

        tvEventName.setText(
                eventName != null
                        ? eventName
                        : "Event"
        );

        tvEventDate.setText(
                eventDate != null
                        ? eventDate
                        : "Date not available"
        );

        tvEventLocation.setText(
                eventLocation != null
                        ? eventLocation
                        : "Location not available"
        );

        tvStatus.setText(
                status != null
                        ? status
                        : "Registered"
        );

        if ("Registered".equals(status)) {

            btnShowQr.setVisibility(
                    View.VISIBLE
            );

            btnShowQr.setOnClickListener(v ->
                    showQrDialog(
                            eventName,
                            registrationId
                    )
            );

        } else {

            btnShowQr.setVisibility(
                    View.GONE
            );
        }

        registeredEventsContainer.addView(
                eventView
        );
    }

    private void showQrDialog(
            String eventName,
            String registrationId
    ) {

        if (registrationId == null ||
                registrationId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Registration ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String qrData =
                "FESTIVE_HUB|" +
                        "EVENT|" +
                        (eventName != null
                                ? eventName
                                : "Event") +
                        "|REGISTRATION|" +
                        registrationId;

        Bitmap qrBitmap =
                generateQrCode(qrData);

        if (qrBitmap == null) {

            Toast.makeText(
                    this,
                    "Unable to generate QR code",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Dialog dialog =
                new Dialog(this);

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                30,
                30,
                30,
                30
        );

        layout.setBackgroundColor(
                Color.WHITE
        );

        TextView title =
                new TextView(this);

        title.setText(
                eventName != null
                        ? eventName
                        : "Event QR"
        );

        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);

        ImageView qrImage =
                new ImageView(this);

        qrImage.setImageBitmap(
                qrBitmap
        );

        qrImage.setAdjustViewBounds(
                true
        );

        LinearLayout.LayoutParams imageParams =
                new LinearLayout.LayoutParams(
                        700,
                        700
                );

        imageParams.gravity =
                Gravity.CENTER;

        imageParams.topMargin = 20;
        imageParams.bottomMargin = 20;

        qrImage.setLayoutParams(
                imageParams
        );

        TextView instruction =
                new TextView(this);

        instruction.setText(
                "Show this QR code to the volunteer for event verification."
        );

        instruction.setTextSize(14);
        instruction.setTextColor(Color.DKGRAY);
        instruction.setGravity(Gravity.CENTER);

        Button closeButton =
                new Button(this);

        closeButton.setText(
                "Close"
        );

        closeButton.setOnClickListener(
                v -> dialog.dismiss()
        );

        layout.addView(title);
        layout.addView(qrImage);
        layout.addView(instruction);
        layout.addView(closeButton);

        dialog.setContentView(layout);

        dialog.show();
    }

    private Bitmap generateQrCode(
            String data
    ) {

        QRCodeWriter writer =
                new QRCodeWriter();

        try {

            BitMatrix bitMatrix =
                    writer.encode(
                            data,
                            BarcodeFormat.QR_CODE,
                            600,
                            600
                    );

            Bitmap bitmap =
                    Bitmap.createBitmap(
                            600,
                            600,
                            Bitmap.Config.RGB_565
                    );

            for (int x = 0; x < 600; x++) {

                for (int y = 0; y < 600; y++) {

                    bitmap.setPixel(
                            x,
                            y,
                            bitMatrix.get(x, y)
                                    ? Color.BLACK
                                    : Color.WHITE
                    );
                }
            }

            return bitmap;

        } catch (WriterException e) {

            return null;
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (registeredEventsContainer != null) {
            loadRegisteredEvents();
        }
    }
}