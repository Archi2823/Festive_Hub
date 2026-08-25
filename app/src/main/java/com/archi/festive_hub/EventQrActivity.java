package com.archi.festive_hub;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class EventQrActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ImageView qrCode;
    private TextView txtEventName;
    private TextView txtRegistrationInfo;

    private static final String EVENT_ID =
            "celebrate_together";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_event_qr
        );

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack =
                findViewById(R.id.btnBackQr);

        qrCode =
                findViewById(R.id.qrCode);

        txtEventName =
                findViewById(R.id.txtQrEventName);

        txtRegistrationInfo =
                findViewById(R.id.txtRegistrationInfo);

        btnBack.setOnClickListener(
                v -> finish()
        );

        generateEventQr();
    }

    private void generateEventQr() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String userId =
                mAuth.getCurrentUser().getUid();

        String registrationId =
                userId + "_" + EVENT_ID;

        db.collection("eventRegistrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {

                                Toast.makeText(
                                        EventQrActivity.this,
                                        "You are not registered for this event",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                                return;
                            }

                            String eventName =
                                    documentSnapshot.getString(
                                            "eventName"
                                    );

                            if (eventName == null ||
                                    eventName.isEmpty()) {

                                eventName =
                                        "Celebrate Together";
                            }

                            String status =
                                    documentSnapshot.getString(
                                            "status"
                                    );

                            /*
                             * Do not show QR if the
                             * registration is cancelled
                             * or otherwise inactive.
                             */

                            if (!"Registered".equals(status)) {

                                Toast.makeText(
                                        EventQrActivity.this,
                                        "Your registration is not active",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                                return;
                            }

                            txtEventName.setText(
                                    eventName
                            );

                            txtRegistrationInfo.setText(
                                    "Show this QR code to the volunteer at the event"
                            );

                            generateQrCode(
                                    registrationId,
                                    eventName
                            );
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                EventQrActivity.this,
                                "Unable to load registration",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void generateQrCode(
            String registrationId,
            String eventName
    ) {

        /*
         * QR FORMAT:
         *
         * FESTIVE_HUB|
         * EVENT|
         * Celebrate Together|
         * REGISTRATION|
         * USER_ID_EVENT_ID
         *
         * The VolunteerScanner reads
         * exactly this format.
         */

        String qrData =
                "FESTIVE_HUB|EVENT|"
                        + eventName
                        + "|REGISTRATION|"
                        + registrationId;

        QRCodeWriter writer =
                new QRCodeWriter();

        try {

            BitMatrix bitMatrix =
                    writer.encode(
                            qrData,
                            BarcodeFormat.QR_CODE,
                            700,
                            700
                    );

            Bitmap bitmap =
                    Bitmap.createBitmap(
                            700,
                            700,
                            Bitmap.Config.RGB_565
                    );

            for (int x = 0; x < 700; x++) {

                for (int y = 0; y < 700; y++) {

                    bitmap.setPixel(
                            x,
                            y,
                            bitMatrix.get(x, y)
                                    ? android.graphics.Color.BLACK
                                    : android.graphics.Color.WHITE
                    );
                }
            }

            qrCode.setImageBitmap(
                    bitmap
            );

        } catch (WriterException e) {

            Toast.makeText(
                    this,
                    "Unable to generate QR code",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}