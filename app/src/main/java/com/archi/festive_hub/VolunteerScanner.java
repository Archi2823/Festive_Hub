package com.archi.festive_hub;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;
import java.util.Map;

public class VolunteerScanner extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String VOLUNTEER_EMAIL =
            "test@gmail.com";

    private final androidx.activity.result.ActivityResultLauncher<ScanOptions>
            barcodeLauncher =
            registerForActivityResult(
                    new ScanContract(),
                    result -> {

                        if (result.getContents() == null) {

                            Toast.makeText(
                                    this,
                                    "Scan cancelled",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        processQrCode(
                                result.getContents()
                        );
                    }
            );

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_volunteer_scanner
        );

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        if (!isVolunteer()) {

            Toast.makeText(
                    this,
                    "Volunteer access only",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }

        startScanner();
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
                "Scan the user's secure event QR code"
        );

        options.setBeepEnabled(true);

        options.setOrientationLocked(false);

        options.setCaptureActivity(
                com.journeyapps.barcodescanner.CaptureActivity.class
        );

        barcodeLauncher.launch(options);
    }

    private void processQrCode(
            String qrData
    ) {

        if (qrData == null
                || qrData.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Invalid QR code",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String prefix =
                "FESTIVE_HUB|SECURE|";

        if (!qrData.startsWith(prefix)) {

            Toast.makeText(
                    this,
                    "Invalid or old Festive Hub QR code",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String secureData =
                qrData.substring(
                        prefix.length()
                ).trim();

        String[] parts =
                secureData.split("\\|", -1);

        if (parts.length != 2) {

            Toast.makeText(
                    this,
                    "Invalid secure QR format",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String registrationId =
                parts[0].trim();

        String qrToken =
                parts[1].trim();

        if (registrationId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Registration ID missing",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (qrToken.isEmpty()) {

            Toast.makeText(
                    this,
                    "Secure token missing",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        checkRegistration(
                registrationId,
                qrToken
        );
    }

    private void checkRegistration(
            String registrationId,
            String qrToken
    ) {

        db.collection("eventRegistrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                Toast.makeText(
                                        this,
                                        "Registration not found",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            String storedQrToken =
                                    getStringValue(
                                            document,
                                            "qrToken"
                                    );

                            if (storedQrToken == null
                                    || storedQrToken.trim().isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "This registration does not have a secure QR",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            if (!storedQrToken.equals(qrToken)) {

                                Toast.makeText(
                                        this,
                                        "Invalid secure QR code",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            String registeredEventName =
                                    getStringValue(
                                            document,
                                            "eventName"
                                    );

                            if (registeredEventName == null) {

                                registeredEventName =
                                        getStringValue(
                                                document,
                                                "name"
                                        );
                            }

                            if (registeredEventName == null
                                    || registeredEventName.trim().isEmpty()) {

                                registeredEventName =
                                        "Event";
                            }

                            String status =
                                    getStringValue(
                                            document,
                                            "status"
                                    );

                            if ("Checked In".equalsIgnoreCase(
                                    status
                            )) {

                                Toast.makeText(
                                        this,
                                        "Already Checked In",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            if (status != null
                                    && !status.equalsIgnoreCase(
                                    "Registered"
                            )) {

                                Toast.makeText(
                                        this,
                                        "Registration status: "
                                                + status,
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            saveAttendance(
                                    document,
                                    registrationId,
                                    registeredEventName
                            );
                        }
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to verify registration",
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void saveAttendance(
            DocumentSnapshot registration,
            String registrationId,
            String eventName
    ) {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Volunteer login required",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String volunteerEmail =
                mAuth.getCurrentUser().getEmail();

        String volunteerUid =
                mAuth.getCurrentUser().getUid();

        String userId =
                getStringValue(
                        registration,
                        "userId"
                );

        String userName =
                getStringValue(
                        registration,
                        "userName"
                );

        String userEmail =
                getStringValue(
                        registration,
                        "userEmail"
                );

        if (userName == null) {

            userName =
                    getStringValue(
                            registration,
                            "name"
                    );
        }

        if (userEmail == null) {

            userEmail =
                    getStringValue(
                            registration,
                            "email"
                    );
        }

        String eventId =
                getStringValue(
                        registration,
                        "eventId"
                );

        Map<String, Object> attendance =
                new HashMap<>();

        attendance.put(
                "registrationId",
                registrationId
        );

        attendance.put(
                "userId",
                userId != null
                        ? userId
                        : ""
        );

        attendance.put(
                "userName",
                userName != null
                        ? userName
                        : "User"
        );

        attendance.put(
                "userEmail",
                userEmail != null
                        ? userEmail
                        : ""
        );

        attendance.put(
                "eventId",
                eventId != null
                        ? eventId
                        : ""
        );

        attendance.put(
                "eventName",
                eventName
        );

        attendance.put(
                "scannedBy",
                volunteerEmail != null
                        ? volunteerEmail
                        : ""
        );

        attendance.put(
                "scannedByUid",
                volunteerUid
        );

        attendance.put(
                "status",
                "Present"
        );

        attendance.put(
                "scannedAt",
                FieldValue.serverTimestamp()
        );

        WriteBatch batch =
                db.batch();

        batch.set(
                db.collection("attendance")
                        .document(registrationId),
                attendance
        );

        batch.update(
                db.collection("eventRegistrations")
                        .document(registrationId),
                "status",
                "Checked In"
        );

        batch.commit()
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "✓ ATTENDANCE VERIFIED",
                                    Toast.LENGTH_LONG
                            ).show();

                            new android.os.Handler()
                                    .postDelayed(
                                            this::startScanner,
                                            1200
                                    );
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Attendance save failed: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show()
                );
    }

    private String getStringValue(
            DocumentSnapshot document,
            String field
    ) {

        Object value =
                document.get(field);

        if (value == null) {
            return null;
        }

        return value.toString();
    }
}