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

    private Button btnVolunteerScanner;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private LinearLayout registeredEventsContainer;
    private TextView tvNoEvents;

    private static final String VOLUNTEER_EMAIL =
            "upadhyaysisters53@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_profile
        );

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack =
                findViewById(R.id.btnBack);

        registeredEventsContainer =
                findViewById(
                        R.id.registeredEventsContainer
                );

        tvNoEvents =
                findViewById(
                        R.id.tvNoEvents
                );

        btnVolunteerScanner =
                findViewById(
                        R.id.btnVolunteerScanner
                );

        btnBack.setOnClickListener(
                v -> finish()
        );

        /*
         * Check whether current user is the
         * authorized volunteer.
         */
        checkVolunteerAccess();

        /*
         * Load user's registered events.
         */
        loadRegisteredEvents();
    }

    private void checkVolunteerAccess() {

        /*
         * Hide button by default.
         */
        btnVolunteerScanner.setVisibility(
                View.GONE
        );

        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String email =
                mAuth.getCurrentUser().getEmail();

        if (email != null &&
                email.equalsIgnoreCase(
                        VOLUNTEER_EMAIL
                )) {

            /*
             * Authorized volunteer.
             */
            btnVolunteerScanner.setVisibility(
                    View.VISIBLE
            );

            btnVolunteerScanner.setOnClickListener(
                    v -> {

                        Intent intent =
                                new Intent(
                                        Profile.this,
                                        VolunteerScanner.class
                                );

                        startActivity(intent);
                    }
            );
        }
    }

    private void loadRegisteredEvents() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

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
                .addOnSuccessListener(
                        querySnapshot -> {

                            registeredEventsContainer
                                    .removeAllViews();

                            if (querySnapshot.isEmpty()) {

                                tvNoEvents.setVisibility(
                                        View.VISIBLE
                                );

                            } else {

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
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to load registered events",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
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

        btnShowQr.setOnClickListener(
                v -> showQrDialog(
                        eventName,
                        registrationId
                )
        );

        registeredEventsContainer.addView(
                eventView
        );
    }

    private void showQrDialog(
            String eventName,
            String registrationId
    ) {

        String qrData =
                "FESTIVE_HUB|EVENT|"
                        + eventName
                        + "|REGISTRATION|"
                        + registrationId;

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

        title.setTextColor(
                Color.BLACK
        );

        title.setGravity(
                Gravity.CENTER
        );

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

        instruction.setTextColor(
                Color.DKGRAY
        );

        instruction.setGravity(
                Gravity.CENTER
        );

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

        dialog.setContentView(
                layout
        );

        if (dialog.getWindow() != null) {

            dialog.getWindow()
                    .setBackgroundDrawableResource(
                            android.R.color.transparent
                    );
        }

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
}