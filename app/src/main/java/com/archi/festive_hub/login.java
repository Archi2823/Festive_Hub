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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogle;
    private ImageButton btnBack;
    private TextView txtForgotPassword, txtSignup;

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getData() == null) {
                            btnGoogle.setEnabled(true);
                            btnGoogle.setText("Continue with Google");
                            return;
                        }

                        try {
                            var task = GoogleSignIn
                                    .getSignedInAccountFromIntent(result.getData());

                            var account = task.getResult(ApiException.class);

                            if (account.getIdToken() == null) {
                                btnGoogle.setEnabled(true);
                                btnGoogle.setText("Continue with Google");

                                Toast.makeText(
                                        login.this,
                                        "Google Sign-In failed",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            AuthCredential credential =
                                    GoogleAuthProvider.getCredential(
                                            account.getIdToken(),
                                            null
                                    );

                            firebaseGoogleLogin(credential);

                        } catch (ApiException e) {

                            btnGoogle.setEnabled(true);
                            btnGoogle.setText("Continue with Google");

                            Toast.makeText(
                                    login.this,
                                    "Google Sign-In cancelled or failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);

        btnBack = findViewById(R.id.btnBack);

        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtSignup = findViewById(R.id.txtSignup);

        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN
                )
                        .requestIdToken(
                                getString(R.string.default_web_client_id)
                        )
                        .requestEmail()
                        .build();

        googleSignInClient =
                GoogleSignIn.getClient(
                        this,
                        googleSignInOptions
                );

        btnBack.setOnClickListener(v -> finish());

        btnLogin.setOnClickListener(v -> loginUser());

        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        txtSignup.setOnClickListener(v -> {
            Intent intent = new Intent(
                    login.this,
                    activity_signup.class
            );

            startActivity(intent);
            finish();
        });

        txtForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(
                    login.this,
                    ForgotPassword.class
            );

            startActivity(intent);
        });
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

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

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    btnLogin.setEnabled(true);
                    btnLogin.setText("LOG IN");

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                login.this,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(
                                login.this,
                                MainActivity.class
                        );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);

                    } else {

                        String errorMessage =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Login failed";

                        Toast.makeText(
                                login.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void signInWithGoogle() {

        btnGoogle.setEnabled(false);
        btnGoogle.setText("Connecting...");

        googleSignInClient.signOut()
                .addOnCompleteListener(task -> {

                    Intent signInIntent =
                            googleSignInClient.getSignInIntent();

                    googleSignInLauncher.launch(signInIntent);
                });
    }

    private void firebaseGoogleLogin(AuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    btnGoogle.setEnabled(true);
                    btnGoogle.setText("Continue with Google");

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                login.this,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(
                                login.this,
                                MainActivity.class
                        );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);

                    } else {

                        String errorMessage =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Google Sign-In failed";

                        Toast.makeText(
                                login.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}