package com.archi.festive_hub;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private EditText currentPassword;
    private EditText newPassword;
    private EditText confirmPassword;
    private Button btnChangePassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        currentPassword = findViewById(R.id.currentPassword);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnBack.setOnClickListener(v -> finish());

        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {

        String current = currentPassword.getText().toString().trim();
        String newPass = newPassword.getText().toString().trim();
        String confirm = confirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(current)) {
            currentPassword.setError("Enter current password");
            currentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPass)) {
            newPassword.setError("Enter new password");
            newPassword.requestFocus();
            return;
        }

        if (newPass.length() < 6) {
            newPassword.setError("Password must be at least 6 characters");
            newPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirm)) {
            confirmPassword.setError("Confirm your new password");
            confirmPassword.requestFocus();
            return;
        }

        if (!newPass.equals(confirm)) {
            confirmPassword.setError("Passwords do not match");
            confirmPassword.requestFocus();
            return;
        }

        if (current.equals(newPass)) {
            newPassword.setError("New password must be different");
            newPassword.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        if (user.getEmail() == null) {
            Toast.makeText(
                    this,
                    "Email account not available",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnChangePassword.setEnabled(false);

        user.reauthenticate(
                EmailAuthProvider.getCredential(
                        user.getEmail(),
                        current
                )
        ).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                user.updatePassword(newPass)
                        .addOnCompleteListener(updateTask -> {

                            btnChangePassword.setEnabled(true);

                            if (updateTask.isSuccessful()) {

                                Toast.makeText(
                                        ChangePassword.this,
                                        "Password changed successfully",
                                        Toast.LENGTH_LONG
                                ).show();

                                currentPassword.setText("");
                                newPassword.setText("");
                                confirmPassword.setText("");

                                finish();

                            } else {

                                Toast.makeText(
                                        ChangePassword.this,
                                        "Unable to change password",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

            } else {

                btnChangePassword.setEnabled(true);

                Toast.makeText(
                        ChangePassword.this,
                        "Current password is incorrect",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}