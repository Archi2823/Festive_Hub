package com.archi.festive_hub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etName;
    private TextView tvEmail;
    private Button btnSaveProfile;
    private ImageButton btnBack;
    private ImageView ivEditProfile;

    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etEditName);
        tvEmail = findViewById(R.id.tvEditEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnBack = findViewById(R.id.btnBackEditProfile);
        ivEditProfile = findViewById(R.id.ivEditProfile);

        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode() == RESULT_OK
                                    && result.getData() != null) {

                                Uri uri =
                                        result.getData().getData();

                                if (uri != null) {

                                    selectedImageUri = uri;

                                    try {
                                        getContentResolver()
                                                .takePersistableUriPermission(
                                                        uri,
                                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                );
                                    } catch (Exception ignored) {
                                    }

                                    ivEditProfile.setImageURI(
                                            selectedImageUri
                                    );
                                }
                            }
                        }
                );

        btnBack.setOnClickListener(
                v -> finish()
        );

        ivEditProfile.setOnClickListener(
                v -> openImagePicker()
        );

        loadUserProfile();

        btnSaveProfile.setOnClickListener(
                v -> saveProfile()
        );
    }

    private void openImagePicker() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.setType("image/*");

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        imagePickerLauncher.launch(intent);
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

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        return;
                    }

                    String firestoreName =
                            document.getString("name");

                    String firestoreEmail =
                            document.getString("email");

                    String imageUri =
                            document.getString(
                                    "profileImageUri"
                            );

                    if (firestoreName != null &&
                            !firestoreName.trim().isEmpty()) {

                        etName.setText(
                                firestoreName
                        );

                        etName.setSelection(
                                etName.length()
                        );
                    }

                    if (firestoreEmail != null &&
                            !firestoreEmail.trim().isEmpty()) {

                        tvEmail.setText(
                                firestoreEmail
                        );
                    }

                    if (imageUri != null &&
                            !imageUri.trim().isEmpty()) {

                        try {

                            selectedImageUri =
                                    Uri.parse(imageUri);

                            ivEditProfile.setImageURI(
                                    selectedImageUri
                            );

                        } catch (Exception ignored) {
                        }
                    }
                });
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

                    if (!task.isSuccessful()) {

                        btnSaveProfile.setEnabled(true);
                        btnSaveProfile.setText(
                                "Save Changes"
                        );

                        String message =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Unable to update name";

                        Toast.makeText(
                                EditProfile.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    saveProfileDetails();
                });
    }

    private void saveProfileDetails() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {

            btnSaveProfile.setEnabled(true);
            btnSaveProfile.setText(
                    "Save Changes"
            );

            return;
        }

        String name =
                etName.getText()
                        .toString()
                        .trim();

        String email =
                user.getEmail();

        String imageUri =
                selectedImageUri != null
                        ? selectedImageUri.toString()
                        : "";

        Map<String, Object> userData =
                new HashMap<>();

        userData.put(
                "userId",
                user.getUid()
        );

        userData.put(
                "name",
                name
        );

        userData.put(
                "email",
                email != null ? email : ""
        );

        userData.put(
                "profileImageUri",
                imageUri
        );

        db.collection("users")
                .document(user.getUid())
                .set(
                        userData,
                        SetOptions.merge()
                )
                .addOnSuccessListener(unused -> {

                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText(
                            "Save Changes"
                    );

                    Toast.makeText(
                            EditProfile.this,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText(
                            "Save Changes"
                    );

                    Toast.makeText(
                            EditProfile.this,
                            "Unable to save profile details",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}