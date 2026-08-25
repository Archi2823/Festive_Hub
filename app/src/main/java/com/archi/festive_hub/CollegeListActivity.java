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

import java.util.HashMap;
import java.util.Map;

public class CollegeListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout collegesContainer;
    private TextView tvNoColleges;
    private Button btnAddCollege;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_list);

        db = FirebaseFirestore.getInstance();

        collegesContainer = findViewById(R.id.collegesContainer);
        tvNoColleges = findViewById(R.id.tvNoColleges);
        btnAddCollege = findViewById(R.id.btnAddCollege);

        btnAddCollege.setOnClickListener(v ->
                showCollegeDialog(null, null, null, null)
        );

        loadColleges();
    }

    private void loadColleges() {

        db.collection("colleges")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    collegesContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        tvNoColleges.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvNoColleges.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String id = document.getId();

                        String name = document.getString("name");
                        String city = document.getString("city");
                        String contact = document.getString("contact");

                        addCollegeCard(
                                id,
                                name,
                                city,
                                contact
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load colleges",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void addCollegeCard(
            String id,
            String name,
            String city,
            String contact
    ) {

        View view = LayoutInflater.from(this).inflate(
                R.layout.item_admin_college,
                collegesContainer,
                false
        );

        TextView tvCollegeName =
                view.findViewById(R.id.tvCollegeName);

        TextView tvCollegeDetails =
                view.findViewById(R.id.tvCollegeDetails);

        Button btnEdit =
                view.findViewById(R.id.btnEditCollege);

        Button btnDelete =
                view.findViewById(R.id.btnDeleteCollege);

        tvCollegeName.setText(
                name != null ? name : "College"
        );

        String details =
                "City: " +
                        (city != null ? city : "N/A")
                        + "\nContact: " +
                        (contact != null ? contact : "N/A");

        tvCollegeDetails.setText(details);

        btnEdit.setOnClickListener(v ->
                showCollegeDialog(
                        id,
                        name,
                        city,
                        contact
                )
        );

        btnDelete.setOnClickListener(v ->
                confirmDelete(id, name)
        );

        collegesContainer.addView(view);
    }

    private void showCollegeDialog(
            String id,
            String oldName,
            String oldCity,
            String oldContact
    ) {

        View dialogView = LayoutInflater.from(this).inflate(
                R.layout.dialog_college,
                null
        );

        EditText etName =
                dialogView.findViewById(R.id.etCollegeName);

        EditText etCity =
                dialogView.findViewById(R.id.etCollegeCity);

        EditText etContact =
                dialogView.findViewById(R.id.etCollegeContact);

        if (oldName != null) {
            etName.setText(oldName);
        }

        if (oldCity != null) {
            etCity.setText(oldCity);
        }

        if (oldContact != null) {
            etContact.setText(oldContact);
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                id == null
                                        ? "Add College"
                                        : "Edit College"
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

                String city =
                        etCity.getText()
                                .toString()
                                .trim();

                String contact =
                        etContact.getText()
                                .toString()
                                .trim();

                if (name.isEmpty()) {

                    Toast.makeText(
                            this,
                            "College name is required",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                if (id == null) {

                    addCollege(
                            name,
                            city,
                            contact
                    );

                } else {

                    updateCollege(
                            id,
                            name,
                            city,
                            contact
                    );
                }

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void addCollege(
            String name,
            String city,
            String contact
    ) {

        Map<String, Object> data =
                new HashMap<>();

        data.put("name", name);
        data.put("city", city);
        data.put("contact", contact);

        db.collection("colleges")
                .add(data)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            this,
                            "College added",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadColleges();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to add college",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void updateCollege(
            String id,
            String name,
            String city,
            String contact
    ) {

        Map<String, Object> data =
                new HashMap<>();

        data.put("name", name);
        data.put("city", city);
        data.put("contact", contact);

        db.collection("colleges")
                .document(id)
                .update(data)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "College updated",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadColleges();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to update college",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void confirmDelete(
            String id,
            String name
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Delete College")
                .setMessage(
                        "Delete " +
                                (name != null
                                        ? name
                                        : "this college") +
                                "?"
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteCollege(id)
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void deleteCollege(String id) {

        db.collection("colleges")
                .document(id)
                .delete()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "College deleted",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadColleges();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to delete college",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}