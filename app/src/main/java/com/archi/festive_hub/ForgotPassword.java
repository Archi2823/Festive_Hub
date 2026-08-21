package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText etEmail;
    private Button btnResetPassword;
    private ImageButton btnBack;
    private TextView txtLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);
        txtLogin = findViewById(R.id.txtLogin);

        btnBack.setOnClickListener(v -> finish());

        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ForgotPassword.this,
                    login.class
            );
            startActivity(intent);
            finish();
        });

        btnResetPassword.setOnClickListener(v -> sendResetEmail());
    }

    private void sendResetEmail() {

        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("SENDING...");

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {

                    btnResetPassword.setEnabled(true);
                    btnResetPassword.setText("SEND RESET LINK");

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                ForgotPassword.this,
                                "Password reset link sent to your email!",
                                Toast.LENGTH_LONG
                        ).show();

                        Intent intent = new Intent(
                                ForgotPassword.this,
                                login.class
                        );

                        startActivity(intent);
                        finish();

                    } else {

                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unable to send reset email";

                        Toast.makeText(
                                ForgotPassword.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}