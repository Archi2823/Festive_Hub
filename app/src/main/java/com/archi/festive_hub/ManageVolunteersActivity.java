package com.archi.festive_hub;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ManageVolunteersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout volunteersContainer;
    private TextView tvNoVolunteers;
    private Button btnAddVolunteer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_volunteers);

        db = FirebaseFirestore.getInstance();

        volunteersContainer =
                findViewById(R.id.volunteersContainer);

        tvNoVolunteers =
                findViewById(R.id.tvNoVolunteers);

        btnAddVolunteer =
                findViewById(R.id.btnAddVolunteer);

        btnAddVolunteer.setOnClickListener(
                v -> showVolunteerDialog(null, null, null, null, null)
        );

        loadVolunteers();
    }

    private void loadVolunteers() {

        db.collection("volunteers")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    volunteersContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        tvNoVolunteers.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvNoVolunteers.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot document :
                            querySnapshot) {

                        String id = document.getId();

                        String name =
                                document.getString("name");

                        String email =
                                document.getString("email");

                        String phone =
                                document.getString("phone");

                        String college =
                                document.getString("college");

                        String status =
                                document.getString("status");

                        addVolunteerCard(
                                id,
                                name,
                                email,
                                phone,
                                college,
                                status
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load volunteers",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void addVolunteerCard(
            String id,
            String name,
            String email,
            String phone,
            String college,
            String status
    ) {

        View view =
                LayoutInflater.from(this).inflate(
                        R.layout.item_admin_volunteer,
                        volunteersContainer,
                        false
                );

        TextView tvName =
                view.findViewById(R.id.tvVolunteerName);

        TextView tvDetails =
                view.findViewById(R.id.tvVolunteerDetails);

        TextView tvStatus =
                view.findViewById(R.id.tvVolunteerStatus);

        Button btnEdit =
                view.findViewById(R.id.btnEditVolunteer);

        Button btnDelete =
                view.findViewById(R.id.btnDeleteVolunteer);

        tvName.setText(
                name != null ? name : "Volunteer"
        );

        String details =
                "Email: " +
                        (email != null ? email : "N/A")
                        + "\nPhone: " +
                        (phone != null ? phone : "N/A")
                        + "\nCollege: " +
                        (college != null ? college : "N/A");

        tvDetails.setText(details);

        tvStatus.setText(
                "Status: " +
                        (status != null ? status : "Active")
        );

        btnEdit.setOnClickListener(v ->
                showVolunteerDialog(
                        id,
                        name,
                        email,
                        phone,
                        college
                )
        );

        btnDelete.setOnClickListener(v ->
                confirmDelete(id, name)
        );

        volunteersContainer.addView(view);
    }

    private void showVolunteerDialog(
            String id,
            String oldName,
            String oldEmail,
            String oldPhone,
            String oldCollege
    ) {

        View dialogView =
                LayoutInflater.from(this).inflate(
                        R.layout.dialog_volunteer,
                        null
                );

        EditText etName =
                dialogView.findViewById(R.id.etVolunteerName);

        EditText etEmail =
                dialogView.findViewById(R.id.etVolunteerEmail);

        EditText etPhone =
                dialogView.findViewById(R.id.etVolunteerPhone);

        EditText etCollege =
                dialogView.findViewById(R.id.etVolunteerCollege);

        if (oldName != null) {
            etName.setText(oldName);
        }

        if (oldEmail != null) {
            etEmail.setText(oldEmail);
        }

        if (oldPhone != null) {
            etPhone.setText(oldPhone);
        }

        if (oldCollege != null) {
            etCollege.setText(oldCollege);
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                id == null
                                        ? "Add Volunteer"
                                        : "Edit Volunteer"
                        )
                        .setView(dialogView)
                        .setPositiveButton(
                                id == null
                                        ? "Add"
                                        : "Update",
                                null
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d -> {

            Button positive =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            positive.setOnClickListener(v -> {

                String name =
                        etName.getText()
                                .toString()
                                .trim();

                String email =
                        etEmail.getText()
                                .toString()
                                .trim();

                String phone =
                        etPhone.getText()
                                .toString()
                                .trim();

                String college =
                        etCollege.getText()
                                .toString()
                                .trim();

                if (name.isEmpty() ||
                        email.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Name and email are required",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                if (id == null) {

                    addVolunteer(
                            name,
                            email,
                            phone,
                            college
                    );

                } else {

                    updateVolunteer(
                            id,
                            name,
                            email,
                            phone,
                            college
                    );
                }

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void addVolunteer(
            String name,
            String email,
            String phone,
            String college
    ) {

        java.util.HashMap<String, Object> data =
                new java.util.HashMap<>();

        data.put("name", name);
        data.put("email", email);
        data.put("phone", phone);
        data.put("college", college);
        data.put("status", "Active");

        db.collection("volunteers")
                .add(data)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            this,
                            "Volunteer added",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadVolunteers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to add volunteer",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void updateVolunteer(
            String id,
            String name,
            String email,
            String phone,
            String college
    ) {

        java.util.HashMap<String, Object> data =
                new java.util.HashMap<>();

        data.put("name", name);
        data.put("email", email);
        data.put("phone", phone);
        data.put("college", college);

        db.collection("volunteers")
                .document(id)
                .update(data)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Volunteer updated",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadVolunteers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to update volunteer",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void confirmDelete(
            String id,
            String name
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Volunteer")
                .setMessage(
                        "Delete " +
                                (name != null
                                        ? name
                                        : "this volunteer") +
                                "?"
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteVolunteer(id)
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void deleteVolunteer(String id) {

        db.collection("volunteers")
                .document(id)
                .delete()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Volunteer deleted",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadVolunteers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to delete volunteer",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}