package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.auth.UserProfileChangeRequest;

public class activity_signup extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private CheckBox checkTerms;
    private Button btnCreateAccount, btnGoogle;
    private ImageButton btnBack;
    private TextView txtLogin;

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
                                        activity_signup.this,
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
                                    activity_signup.this,
                                    "Google Sign-In cancelled or failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        checkTerms = findViewById(R.id.checkTerms);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnBack = findViewById(R.id.btnBack);

        txtLogin = findViewById(R.id.txtLogin);

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

        btnCreateAccount.setOnClickListener(v -> createAccount());

        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    activity_signup.this,
                    login.class
            );

            startActivity(intent);
            finish();
        });
    }

    private void createAccount() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Please enter your full name");
            etName.requestFocus();
            return;
        }

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
            etPassword.setError("Please enter a password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(
                    "Password must be at least 6 characters"
            );
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError(
                    "Please confirm your password"
            );
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(
                    "Passwords do not match"
            );
            etConfirmPassword.requestFocus();
            return;
        }

        if (!checkTerms.isChecked()) {
            Toast.makeText(
                    this,
                    "Please agree to the Terms & Conditions",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnCreateAccount.setEnabled(false);
        btnCreateAccount.setText("Creating Account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    btnCreateAccount.setEnabled(true);
                    btnCreateAccount.setText("CREATE ACCOUNT");

                    if (task.isSuccessful()) {

                        if (mAuth.getCurrentUser() != null) {

                            UserProfileChangeRequest profile =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();

                            mAuth.getCurrentUser()
                                    .updateProfile(profile);
                        }

                        mAuth.signOut();

                        Toast.makeText(
                                activity_signup.this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(
                                activity_signup.this,
                                login.class
                        );

                        startActivity(intent);
                        finish();

                    } else {

                        String errorMessage =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Account creation failed";

                        Toast.makeText(
                                activity_signup.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void signInWithGoogle() {

        if (!checkTerms.isChecked()) {
            Toast.makeText(
                    this,
                    "Please agree to the Terms & Conditions",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

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
                                activity_signup.this,
                                "Google account connected successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(
                                activity_signup.this,
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
                                activity_signup.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}