package com.archi.festive_hub;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText etName;
    private TextView tvEmail;
    private Button btnSaveProfile;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etEditName);
        tvEmail = findViewById(R.id.tvEditEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnBack = findViewById(R.id.btnBackEditProfile);

        btnBack.setOnClickListener(v -> finish());

        loadUserProfile();

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void loadUserProfile() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String name =
                user.getDisplayName();

        String email =
                user.getEmail();

        if (name != null &&
                !name.trim().isEmpty()) {

            etName.setText(name);

            etName.setSelection(
                    etName.length()
            );
        }

        if (email != null &&
                !email.trim().isEmpty()) {

            tvEmail.setText(email);

        } else {

            tvEmail.setText(
                    "Email not available"
            );
        }
    }

    private void saveProfile() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String name =
                etName.getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(name)) {

            etName.setError(
                    "Please enter your name"
            );

            etName.requestFocus();

            return;
        }

        if (name.length() < 2) {

            etName.setError(
                    "Name must contain at least 2 characters"
            );

            etName.requestFocus();

            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        UserProfileChangeRequest profile =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build();

        user.updateProfile(profile)
                .addOnCompleteListener(task -> {

                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("Save Changes");

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                EditProfile.this,
                                "Name updated successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();

                    } else {

                        String message =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Unable to update name";

                        Toast.makeText(
                                EditProfile.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}