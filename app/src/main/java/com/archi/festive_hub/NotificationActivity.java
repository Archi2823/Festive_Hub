package com.archi.festive_hub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class NotificationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private LinearLayout notificationContainer;
    private TextView tvNoNotifications;

    private static final String ADMIN_EMAIL =
            "upadhyaysisters53@gmail.com";

    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        notificationContainer =
                findViewById(R.id.notificationContainer);

        tvNoNotifications =
                findViewById(R.id.tvNoNotifications);

        isAdmin = checkAdminAccess();

        Button btnAddNotification =
                findViewById(R.id.btnAddNotification);

        if (isAdmin) {
            btnAddNotification.setVisibility(View.VISIBLE);

            btnAddNotification.setOnClickListener(
                    v -> showNotificationDialog()
            );
        } else {
            btnAddNotification.setVisibility(View.GONE);
        }

        loadNotifications();
    }

    private boolean checkAdminAccess() {

        if (mAuth.getCurrentUser() == null) {
            return false;
        }

        String email =
                mAuth.getCurrentUser().getEmail();

        return email != null
                && email.equalsIgnoreCase(ADMIN_EMAIL);
    }

    private void loadNotifications() {

        db.collection("notifications")
                .get()
                .addOnSuccessListener(snapshot -> {

                    notificationContainer.removeAllViews();

                    if (snapshot.isEmpty()) {

                        tvNoNotifications.setVisibility(
                                View.VISIBLE
                        );

                        return;
                    }

                    tvNoNotifications.setVisibility(
                            View.GONE
                    );

                    for (QueryDocumentSnapshot document :
                            snapshot) {

                        String id =
                                document.getId();

                        String title =
                                document.getString("title");

                        String message =
                                document.getString("message");

                        addNotificationCard(
                                id,
                                title,
                                message
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load notifications",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void addNotificationCard(
            String id,
            String title,
            String message
    ) {

        View view = LayoutInflater.from(this).inflate(
                R.layout.item_admin_notification,
                notificationContainer,
                false
        );

        TextView tvTitle =
                view.findViewById(
                        R.id.tvNotificationTitle
                );

        TextView tvMessage =
                view.findViewById(
                        R.id.tvNotificationMessage
                );

        Button btnEdit =
                view.findViewById(
                        R.id.btnEditNotification
                );

        Button btnDelete =
                view.findViewById(
                        R.id.btnDeleteNotification
                );

        tvTitle.setText(
                title != null
                        ? title
                        : "Notification"
        );

        tvMessage.setText(
                message != null
                        ? message
                        : ""
        );

        if (isAdmin) {

            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);

            btnEdit.setOnClickListener(v ->
                    showNotificationDialog(
                            id,
                            title,
                            message
                    )
            );

            btnDelete.setOnClickListener(v ->
                    confirmDelete(
                            id,
                            title
                    )
            );

        } else {

            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        notificationContainer.addView(view);
    }

    private void showNotificationDialog() {

        showNotificationDialog(
                null,
                "",
                ""
        );
    }

    private void showNotificationDialog(
            String id,
            String oldTitle,
            String oldMessage
    ) {

        if (!isAdmin) {
            Toast.makeText(
                    this,
                    "Admin access required",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        View dialogView =
                LayoutInflater.from(this).inflate(
                        R.layout.dialog_add_notification,
                        null
                );

        EditText etTitle =
                dialogView.findViewById(
                        R.id.etNotificationTitle
                );

        EditText etMessage =
                dialogView.findViewById(
                        R.id.etNotificationMessage
                );

        etTitle.setText(oldTitle);
        etMessage.setText(oldMessage);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                id == null
                                        ? "Add Notification"
                                        : "Edit Notification"
                        )
                        .setView(dialogView)
                        .setPositiveButton(
                                id == null
                                        ? "Send"
                                        : "Save",
                                null
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                ).setOnClickListener(v -> {

                    String title =
                            etTitle.getText()
                                    .toString()
                                    .trim();

                    String message =
                            etMessage.getText()
                                    .toString()
                                    .trim();

                    if (title.isEmpty()) {

                        etTitle.setError(
                                "Enter notification title"
                        );

                        return;
                    }

                    if (message.isEmpty()) {

                        etMessage.setError(
                                "Enter notification message"
                        );

                        return;
                    }

                    saveNotification(
                            id,
                            title,
                            message,
                            dialog
                    );
                })
        );

        dialog.show();
    }

    private void saveNotification(
            String id,
            String title,
            String message,
            AlertDialog dialog
    ) {

        if (!isAdmin) {

            Toast.makeText(
                    this,
                    "Admin access required",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        java.util.HashMap<String, Object> data =
                new java.util.HashMap<>();

        data.put(
                "title",
                title
        );

        data.put(
                "message",
                message
        );

        data.put(
                "timestamp",
                com.google.firebase.firestore.FieldValue
                        .serverTimestamp()
        );

        if (id == null) {

            db.collection("notifications")
                    .add(data)
                    .addOnSuccessListener(
                            documentReference -> {

                                Toast.makeText(
                                        this,
                                        "Notification added",
                                        Toast.LENGTH_SHORT
                                ).show();

                                dialog.dismiss();

                                loadNotifications();
                            }
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    this,
                                    "Unable to add notification",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );

        } else {

            db.collection("notifications")
                    .document(id)
                    .update(data)
                    .addOnSuccessListener(
                            unused -> {

                                Toast.makeText(
                                        this,
                                        "Notification updated",
                                        Toast.LENGTH_SHORT
                                ).show();

                                dialog.dismiss();

                                loadNotifications();
                            }
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    this,
                                    "Unable to update notification",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        }
    }

    private void confirmDelete(
            String id,
            String title
    ) {

        if (!isAdmin) {

            Toast.makeText(
                    this,
                    "Admin access required",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Delete Notification"
                )
                .setMessage(
                        "Delete \"" +
                                (
                                        title != null
                                                ? title
                                                : "this notification"
                                ) +
                                "\"?"
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteNotification(id)
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void deleteNotification(
            String id
    ) {

        if (!isAdmin) {

            Toast.makeText(
                    this,
                    "Admin access required",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        db.collection("notifications")
                .document(id)
                .delete()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Notification deleted",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadNotifications();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to delete notification",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}