package com.archi.festive_hub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class RegisteredUsersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout usersContainer;
    private TextView tvEventName;
    private TextView tvNoUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_users);

        db = FirebaseFirestore.getInstance();

        usersContainer =
                findViewById(R.id.usersContainer);

        tvEventName =
                findViewById(R.id.tvEventName);

        tvNoUsers =
                findViewById(R.id.tvNoUsers);

        View btnBack =
                findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        String eventId =
                getIntent().getStringExtra("eventId");

        String eventName =
                getIntent().getStringExtra("eventName");

        tvEventName.setText(
                eventName != null
                        ? eventName
                        : "Registered Users"
        );

        if (eventId == null || eventId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Event not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        loadRegisteredUsers(eventId);
    }

    private void loadRegisteredUsers(String eventId) {

        db.collection("eventRegistrations")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    usersContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {

                        tvNoUsers.setVisibility(
                                View.VISIBLE
                        );

                        return;
                    }

                    tvNoUsers.setVisibility(
                            View.GONE
                    );

                    for (QueryDocumentSnapshot document :
                            querySnapshot) {

                        addUserCard(document);
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load registered users",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void addUserCard(
            QueryDocumentSnapshot document
    ) {

        View userView =
                LayoutInflater.from(this).inflate(
                        R.layout.item_registered_user,
                        usersContainer,
                        false
                );

        TextView tvUserName =
                userView.findViewById(
                        R.id.tvRegisteredUserName
                );

        TextView tvUserDetails =
                userView.findViewById(
                        R.id.tvRegisteredUserDetails
                );

        String userName =
                document.getString("userName");

        String email =
                document.getString("email");

        String userId =
                document.getString("userId");

        String status =
                document.getString("status");

        if (userName == null ||
                userName.trim().isEmpty()) {

            userName = "Registered Student";
        }

        tvUserName.setText(userName);

        String details =
                "Email: " +
                        (email != null
                                ? email
                                : "N/A")
                        + "\nStudent ID: " +
                        (userId != null
                                ? userId
                                : "N/A")
                        + "\nStatus: " +
                        (status != null
                                ? status
                                : "Registered");

        tvUserDetails.setText(details);

        usersContainer.addView(userView);
    }
}