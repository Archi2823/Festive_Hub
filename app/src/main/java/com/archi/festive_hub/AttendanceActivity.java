package com.archi.festive_hub;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AttendanceActivity extends AppCompatActivity {

    private static final String ADMIN_EMAIL =
            "upadhyaysisters53@gmail.com";

    private static final String VOLUNTEER_EMAIL =
            "test@gmail.com";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private LinearLayout attendanceContainer;
    private TextView tvNoAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_attendance
        );

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView btnBack =
                findViewById(R.id.btnBack);

        attendanceContainer =
                findViewById(
                        R.id.attendanceContainer
                );

        tvNoAttendance =
                findViewById(
                        R.id.tvNoAttendance
                );

        btnBack.setOnClickListener(
                v -> finish()
        );

        if (!isAdmin() && !isVolunteer()) {

            Toast.makeText(
                    this,
                    "Attendance access denied",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }

        loadAttendance();
    }

    private boolean isAdmin() {

        if (mAuth.getCurrentUser() == null) {
            return false;
        }

        String email =
                mAuth.getCurrentUser().getEmail();

        return email != null
                && email.equalsIgnoreCase(
                ADMIN_EMAIL
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

    private void loadAttendance() {

        if (mAuth.getCurrentUser() == null) {
            return;
        }

        attendanceContainer.removeAllViews();

        tvNoAttendance.setVisibility(
                View.GONE
        );

        if (isAdmin()) {

            loadAdminAttendance();

        } else if (isVolunteer()) {

            loadVolunteerAttendance();
        }
    }

    private void loadAdminAttendance() {

        db.collection("attendance")
                .orderBy(
                        "scannedAt",
                        Query.Direction.DESCENDING
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            attendanceContainer
                                    .removeAllViews();

                            if (querySnapshot.isEmpty()) {

                                showNoAttendance(
                                        "No attendance records yet."
                                );

                                return;
                            }

                            tvNoAttendance.setVisibility(
                                    View.GONE
                            );

                            for (
                                    DocumentSnapshot document :
                                    querySnapshot
                            ) {

                                addAttendanceCard(
                                        document
                                );
                            }
                        }
                )
                .addOnFailureListener(e -> {

                    showNoAttendance(
                            "Unable to load attendance."
                    );

                    Toast.makeText(
                            this,
                            "Attendance loading failed: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void loadVolunteerAttendance() {

        String currentUid =
                mAuth.getCurrentUser().getUid();

        db.collection("attendance")
                .whereEqualTo(
                        "scannedByUid",
                        currentUid
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            attendanceContainer
                                    .removeAllViews();

                            if (querySnapshot.isEmpty()) {

                                showNoAttendance(
                                        "You have not scanned any attendance yet."
                                );

                                return;
                            }

                            tvNoAttendance.setVisibility(
                                    View.GONE
                            );

                            for (
                                    DocumentSnapshot document :
                                    querySnapshot
                            ) {

                                addAttendanceCard(
                                        document
                                );
                            }
                        }
                )
                .addOnFailureListener(e -> {

                    showNoAttendance(
                            "Unable to load your attendance."
                    );

                    Toast.makeText(
                            this,
                            "Attendance loading failed: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void showNoAttendance(
            String message
    ) {

        tvNoAttendance.setVisibility(
                View.VISIBLE
        );

        tvNoAttendance.setText(
                message
        );
    }

    private void addAttendanceCard(
            DocumentSnapshot document
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                24,
                20,
                24,
                20
        );

        card.setBackgroundResource(
                R.drawable.bg_admin_card
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                0,
                0,
                16
        );

        card.setLayoutParams(params);

        String userName =
                getValue(
                        document,
                        "userName",
                        "User"
                );

        String userEmail =
                getValue(
                        document,
                        "userEmail",
                        ""
                );

        String eventName =
                getValue(
                        document,
                        "eventName",
                        "Event"
                );

        String status =
                getValue(
                        document,
                        "status",
                        "Present"
                );

        String scannedBy =
                getValue(
                        document,
                        "scannedBy",
                        ""
                );

        TextView tvUser =
                createTextView(
                        "👤 " + userName,
                        18,
                        true
                );

        TextView tvEmail =
                createTextView(
                        userEmail,
                        13,
                        false
                );

        TextView tvEvent =
                createTextView(
                        "🎪 " + eventName,
                        15,
                        true
                );

        TextView tvStatus =
                createTextView(
                        "✓ " + status,
                        14,
                        true
                );

        TextView tvVolunteer =
                createTextView(
                        "Scanned by: " + scannedBy,
                        12,
                        false
                );

        TextView tvTime =
                createTextView(
                        "Scan time: "
                                + formatTimestamp(
                                document
                        ),
                        12,
                        false
                );

        card.addView(tvUser);
        card.addView(tvEmail);
        card.addView(tvEvent);
        card.addView(tvStatus);
        card.addView(tvVolunteer);
        card.addView(tvTime);

        attendanceContainer.addView(
                card
        );
    }

    private TextView createTextView(
            String text,
            int size,
            boolean bold
    ) {

        TextView textView =
                new TextView(this);

        textView.setText(text);

        textView.setTextSize(size);

        textView.setTextColor(
                android.graphics.Color.rgb(
                        40,
                        40,
                        40
                )
        );

        if (bold) {

            textView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        textView.setPadding(
                0,
                5,
                0,
                5
        );

        return textView;
    }

    private String getValue(
            DocumentSnapshot document,
            String field,
            String defaultValue
    ) {

        Object value =
                document.get(field);

        if (value == null) {
            return defaultValue;
        }

        return value.toString();
    }

    private String formatTimestamp(
            DocumentSnapshot document
    ) {

        com.google.firebase.Timestamp timestamp =
                document.getTimestamp(
                        "scannedAt"
                );

        if (timestamp == null) {
            return "Just now";
        }

        java.text.DateFormat format =
                new java.text.SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        java.util.Locale.getDefault()
                );

        return format.format(
                timestamp.toDate()
        );
    }
}