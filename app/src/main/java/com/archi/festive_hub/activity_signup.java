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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class activity_signup extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private CheckBox checkTerms;
    private Button btnCreateAccount, btnGoogle;
    private ImageButton btnBack;
    private TextView txtLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
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
                                    .getSignedInAccountFromIntent(
                                            result.getData()
                                    );

                            var account =
                                    task.getResult(
                                            ApiException.class
                                    );

                            if (account.getIdToken() == null) {

                                btnGoogle.setEnabled(true);
                                btnGoogle.setText(
                                        "Continue with Google"
                                );

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
                            btnGoogle.setText(
                                    "Continue with Google"
                            );

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
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword =
                findViewById(R.id.etConfirmPassword);

        checkTerms = findViewById(R.id.checkTerms);

        btnCreateAccount =
                findViewById(R.id.btnCreateAccount);

        btnGoogle =
                findViewById(R.id.btnGoogle);

        btnBack =
                findViewById(R.id.btnBack);

        txtLogin =
                findViewById(R.id.txtLogin);

        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN
                )
                        .requestIdToken(
                                getString(
                                        R.string.default_web_client_id
                                )
                        )
                        .requestEmail()
                        .build();

        googleSignInClient =
                GoogleSignIn.getClient(
                        this,
                        googleSignInOptions
                );

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnCreateAccount.setOnClickListener(
                v -> createAccount()
        );

        btnGoogle.setOnClickListener(
                v -> signInWithGoogle()
        );

        txtLogin.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            activity_signup.this,
                            login.class
                    );

            startActivity(intent);
            finish();
        });
    }

    private void createAccount() {

        String name =
                etName.getText()
                        .toString()
                        .trim();

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString();

        String confirmPassword =
                etConfirmPassword.getText()
                        .toString();

        if (TextUtils.isEmpty(name)) {

            etName.setError(
                    "Please enter your full name"
            );

            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {

            etEmail.setError(
                    "Please enter your email"
            );

            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError(
                    "Please enter a valid email"
            );

            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {

            etPassword.setError(
                    "Please enter a password"
            );

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
        btnCreateAccount.setText(
                "Creating Account..."
        );

        mAuth.createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            if (!task.isSuccessful()) {

                                btnCreateAccount.setEnabled(true);
                                btnCreateAccount.setText(
                                        "CREATE ACCOUNT"
                                );

                                String errorMessage =
                                        task.getException() != null
                                                ? task.getException()
                                                .getMessage()
                                                : "Account creation failed";

                                Toast.makeText(
                                        activity_signup.this,
                                        errorMessage,
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            if (mAuth.getCurrentUser() == null) {

                                btnCreateAccount.setEnabled(true);
                                btnCreateAccount.setText(
                                        "CREATE ACCOUNT"
                                );

                                Toast.makeText(
                                        activity_signup.this,
                                        "Account created but user data was not found",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            FirebaseUser user =
                                    mAuth.getCurrentUser();

                            UserProfileChangeRequest profile =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();

                            user.updateProfile(profile)
                                    .addOnCompleteListener(
                                            profileTask ->
                                                    saveUserToFirestore(
                                                            user,
                                                            name,
                                                            email
                                                    )
                                    );
                        }
                );
    }

    private void saveUserToFirestore(
            FirebaseUser user,
            String name,
            String email
    ) {

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
                email
        );

        userData.put(
                "profileImageUri",
                ""
        );

        db.collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnCompleteListener(task -> {

                    btnCreateAccount.setEnabled(true);
                    btnCreateAccount.setText(
                            "CREATE ACCOUNT"
                    );

                    if (!task.isSuccessful()) {

                        String errorMessage =
                                task.getException() != null
                                        ? task.getException()
                                        .getMessage()
                                        : "Unable to save profile details";

                        Toast.makeText(
                                activity_signup.this,
                                "Account created, but profile details could not be saved: "
                                        + errorMessage,
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    mAuth.signOut();

                    Toast.makeText(
                            activity_signup.this,
                            "Account created successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent =
                            new Intent(
                                    activity_signup.this,
                                    login.class
                            );

                    startActivity(intent);
                    finish();
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
                            googleSignInClient
                                    .getSignInIntent();

                    googleSignInLauncher.launch(
                            signInIntent
                    );
                });
    }

    private void firebaseGoogleLogin(
            AuthCredential credential
    ) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(
                        this,
                        task -> {

                            btnGoogle.setEnabled(true);
                            btnGoogle.setText(
                                    "Continue with Google"
                            );

                            if (!task.isSuccessful()) {

                                String errorMessage =
                                        task.getException() != null
                                                ? task.getException()
                                                .getMessage()
                                                : "Google Sign-In failed";

                                Toast.makeText(
                                        activity_signup.this,
                                        errorMessage,
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            FirebaseUser user =
                                    mAuth.getCurrentUser();

                            if (user == null) {

                                Toast.makeText(
                                        activity_signup.this,
                                        "Unable to get Google user",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            saveGoogleUserToFirestore(user);
                        }
                );
    }

    private void saveGoogleUserToFirestore(
            FirebaseUser user
    ) {

        String name =
                user.getDisplayName();

        String email =
                user.getEmail();

        if (name == null ||
                name.trim().isEmpty()) {

            name = "User";
        }

        if (email == null ||
                email.trim().isEmpty()) {

            email = "";
        }

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
                email
        );

        db.collection("users")
                .document(user.getUid())
                .set(
                        userData,
                        com.google.firebase.firestore.SetOptions.merge()
                )
                .addOnCompleteListener(
                        firestoreTask -> {

                            if (!firestoreTask.isSuccessful()) {

                                String errorMessage =
                                        firestoreTask.getException() != null
                                                ? firestoreTask.getException()
                                                .getMessage()
                                                : "Unable to save profile details";

                                Toast.makeText(
                                        activity_signup.this,
                                        "Google login successful, but profile details could not be saved: "
                                                + errorMessage,
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            Toast.makeText(
                                    activity_signup.this,
                                    "Google account connected successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent =
                                    new Intent(
                                            activity_signup.this,
                                            MainActivity.class
                                    );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                        }
                );
    }
}