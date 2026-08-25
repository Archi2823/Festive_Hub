package com.archi.festive_hub;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ManageStudentsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout studentsContainer;
    private TextView tvNoStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        db = FirebaseFirestore.getInstance();

        studentsContainer = findViewById(R.id.studentsContainer);
        tvNoStudents = findViewById(R.id.tvNoStudents);

        loadStudents();
    }

    private void loadStudents() {

        db.collection("eventRegistrations")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    studentsContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        tvNoStudents.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvNoStudents.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String registrationId = document.getId();

                        String userId =
                                document.getString("userId");

                        String eventName =
                                document.getString("eventName");

                        String eventDate =
                                document.getString("eventDate");

                        String eventLocation =
                                document.getString("eventLocation");

                        String status =
                                document.getString("status");

                        addStudentCard(
                                registrationId,
                                userId,
                                eventName,
                                eventDate,
                                eventLocation,
                                status
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load students: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void addStudentCard(
            String registrationId,
            String userId,
            String eventName,
            String eventDate,
            String eventLocation,
            String status
    ) {

        View studentView =
                LayoutInflater.from(this).inflate(
                        R.layout.item_admin_student,
                        studentsContainer,
                        false
                );

        TextView tvStudentId =
                studentView.findViewById(R.id.tvStudentId);

        TextView tvStudentEvent =
                studentView.findViewById(R.id.tvStudentEvent);

        TextView tvStudentDate =
                studentView.findViewById(R.id.tvStudentDate);

        TextView tvStudentLocation =
                studentView.findViewById(R.id.tvStudentLocation);

        TextView tvStudentStatus =
                studentView.findViewById(R.id.tvStudentStatus);

        Button btnRemoveStudent =
                studentView.findViewById(R.id.btnRemoveStudent);

        tvStudentId.setText(
                "Student ID: " +
                        (userId != null
                                ? userId
                                : "N/A")
        );

        tvStudentEvent.setText(
                eventName != null
                        ? eventName
                        : "Event"
        );

        tvStudentDate.setText(
                "Date: " +
                        (eventDate != null
                                ? eventDate
                                : "N/A")
        );

        tvStudentLocation.setText(
                "Location: " +
                        (eventLocation != null
                                ? eventLocation
                                : "N/A")
        );

        tvStudentStatus.setText(
                status != null
                        ? status
                        : "Registered"
        );

        btnRemoveStudent.setOnClickListener(v ->
                confirmDelete(
                        registrationId,
                        eventName
                )
        );

        studentsContainer.addView(studentView);
    }

    private void confirmDelete(
            String registrationId,
            String eventName
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Remove Registration")
                .setMessage(
                        "Remove this student's registration for \"" +
                                (eventName != null
                                        ? eventName
                                        : "this event") +
                                "\"?"
                )
                .setPositiveButton(
                        "Remove",
                        (dialog, which) ->
                                deleteRegistration(
                                        registrationId
                                )
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void deleteRegistration(
            String registrationId
    ) {

        db.collection("eventRegistrations")
                .document(registrationId)
                .delete()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Student registration removed",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadStudents();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to remove registration: " +
                                        e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }
}