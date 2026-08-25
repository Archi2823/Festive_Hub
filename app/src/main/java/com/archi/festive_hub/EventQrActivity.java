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

    private String registrationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_event_qr
        );

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        ImageButton btnBack =
                findViewById(R.id.btnBackQr);

        qrCode =
                findViewById(R.id.qrCode);

        txtEventName =
                findViewById(
                        R.id.txtQrEventName
                );

        txtRegistrationInfo =
                findViewById(
                        R.id.txtRegistrationInfo
                );

        registrationId =
                getIntent().getStringExtra(
                        "registrationId"
                );

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

        if (registrationId == null ||
                registrationId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Registration not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

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

                            String userId =
                                    documentSnapshot
                                            .getString(
                                                    "userId"
                                            );

                            if (userId == null ||
                                    !userId.equals(
                                            mAuth.getCurrentUser()
                                                    .getUid()
                                    )) {

                                Toast.makeText(
                                        EventQrActivity.this,
                                        "Invalid registration",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                                return;
                            }

                            String eventName =
                                    documentSnapshot
                                            .getString(
                                                    "eventName"
                                            );

                            String eventId =
                                    documentSnapshot
                                            .getString(
                                                    "eventId"
                                            );

                            if (eventName == null) {
                                eventName =
                                        "Event";
                            }

                            if (eventId == null) {
                                eventId =
                                        "";
                            }

                            txtEventName.setText(
                                    eventName
                            );

                            txtRegistrationInfo.setText(
                                    "Show this QR code to the volunteer at the event"
                            );

                            generateQrCode(
                                    registrationId,
                                    eventId
                            );
                        }
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                EventQrActivity.this,
                                "Unable to load registration",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void generateQrCode(
            String registrationId,
            String eventId
    ) {

        String qrData =
                "FESTIVE_HUB|"
                        + "EVENT_ID="
                        + eventId
                        + "|REGISTRATION_ID="
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

            for (int x = 0;
                 x < 700;
                 x++) {

                for (int y = 0;
                     y < 700;
                     y++) {

                    bitmap.setPixel(
                            x,
                            y,
                            bitMatrix.get(x, y)
                                    ? android.graphics.Color.BLACK
                                    : android.graphics.Color.WHITE
                    );
                }
            }

            qrCode.setImageBitmap(bitmap);

        } catch (WriterException e) {

            Toast.makeText(
                    this,
                    "Unable to generate QR code",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}