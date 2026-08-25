package com.archi.festive_hub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class VolunteerScanner extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView tvResult;
    private Button btnScan;

    private static final String VOLUNTEER_EMAIL =
            "test@gmail.com";

    private final androidx.activity.result.ActivityResultLauncher<ScanOptions>
            barcodeLauncher =
            registerForActivityResult(
                    new ScanContract(),
                    result -> {

                        if (result.getContents() == null) {

                            tvResult.setText(
                                    "Scan cancelled"
                            );

                            return;
                        }

                        String qrData =
                                result.getContents();

                        verifyQrCode(qrData);
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_volunteer_scanner
        );

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvResult = findViewById(R.id.tvResult);
        btnScan = findViewById(R.id.btnScan);

        /*
         * Check volunteer access
         */
        if (!isVolunteer()) {

            Toast.makeText(
                    this,
                    "Volunteer access only",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        btnScan.setOnClickListener(
                v -> startScanner()
        );
    }

    private boolean isVolunteer() {

        if (mAuth.getCurrentUser() == null) {
            return false;
        }

        String email =
                mAuth.getCurrentUser().getEmail();

        return email != null
                && email.equalsIgnoreCase(
                VOLUNTEER_EMAIL
        );
    }

    private void startScanner() {

        ScanOptions options =
                new ScanOptions();

        options.setPrompt(
                "Scan the user's event QR code"
        );

        options.setBeepEnabled(true);

        options.setOrientationLocked(true);

        options.setDesiredBarcodeFormats(
                ScanOptions.QR_CODE
        );

        barcodeLauncher.launch(options);
    }

    private void verifyQrCode(String qrData) {

        if (qrData == null ||
                !qrData.startsWith(
                        "FESTIVE_HUB|EVENT|"
                )) {

            showInvalid(
                    "Invalid Festive Hub QR code"
            );

            return;
        }

        String[] parts =
                qrData.split("\\|");

        /*
         * Expected QR format:
         *
         * FESTIVE_HUB|EVENT|Celebrate Together|
         * REGISTRATION|USER_UID_EVENT_ID
         */

        if (parts.length < 5 ||
                !"REGISTRATION".equals(parts[3])) {

            showInvalid(
                    "Invalid registration QR code"
            );

            return;
        }

        String eventName =
                parts[2];

        String registrationId =
                parts[4];

        if (registrationId.isEmpty()) {

            showInvalid(
                    "Registration ID missing"
            );

            return;
        }

        tvResult.setText(
                "Checking registration..."
        );

        db.collection("eventRegistrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {

                                showInvalid(
                                        "Registration not found"
                                );

                                return;
                            }

                            String registeredEvent =
                                    documentSnapshot.getString(
                                            "eventName"
                                    );

                            String status =
                                    documentSnapshot.getString(
                                            "status"
                                    );

                            /*
                             * Check event
                             */

                            if (registeredEvent == null ||
                                    !registeredEvent.equals(
                                            eventName
                                    )) {

                                showInvalid(
                                        "Event does not match"
                                );

                                return;
                            }

                            /*
                             * Check registration status
                             */

                            if ("Checked In".equals(status)) {

                                tvResult.setText(
                                        "ALREADY CHECKED IN ✓\n\n"
                                                + "Event: "
                                                + eventName
                                );

                                Toast.makeText(
                                        this,
                                        "This attendee is already checked in",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            if (!"Registered".equals(status)) {

                                showInvalid(
                                        "Registration is not active"
                                );

                                return;
                            }

                            /*
                             * Valid registration
                             */

                            markCheckedIn(
                                    registrationId,
                                    registeredEvent
                            );
                        }
                )
                .addOnFailureListener(
                        e -> showInvalid(
                                "Unable to verify registration"
                        )
                );
    }

    private void markCheckedIn(
            String registrationId,
            String eventName
    ) {

        db.collection("eventRegistrations")
                .document(registrationId)
                .update(
                        "status",
                        "Checked In"
                )
                .addOnSuccessListener(
                        unused -> {

                            tvResult.setText(
                                    "✓ ATTENDANCE VERIFIED\n\n"
                                            + "Event: "
                                            + eventName
                                            + "\n\n"
                                            + "Status: Checked In"
                            );

                            Toast.makeText(
                                    this,
                                    "Attendance verified successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> showInvalid(
                                "Unable to mark attendance"
                        )
                );
    }

    private void showInvalid(
            String message
    ) {

        tvResult.setText(
                "✕ INVALID REGISTRATION\n\n"
                        + message
        );

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}