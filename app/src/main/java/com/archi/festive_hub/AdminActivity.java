package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private View btnManageEvents;
    private View btnManageStudents;
    private View btnManageVolunteers;
    private View btnCollegeList;
    private View btnNotifications;
    private View btnAdminSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnManageEvents = findViewById(R.id.btnManageEvents);
        btnManageStudents = findViewById(R.id.btnManageStudents);
        btnManageVolunteers = findViewById(R.id.btnManageVolunteers);
        btnCollegeList = findViewById(R.id.btnCollegeList);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnAdminSettings = findViewById(R.id.btnAdminSettings);

        btnManageEvents.setOnClickListener(v ->
                startActivity(new Intent(
                        AdminActivity.this,
                        ManageEventsActivity.class
                ))
        );

        btnManageStudents.setOnClickListener(v ->
                startActivity(new Intent(
                        AdminActivity.this,
                        ManageStudentsActivity.class
                ))
        );

        btnManageVolunteers.setOnClickListener(v ->
                startActivity(new Intent(
                        AdminActivity.this,
                        ManageVolunteersActivity.class
                ))
        );

        btnCollegeList.setOnClickListener(v ->
                startActivity(new Intent(
                        AdminActivity.this,
                        CollegeListActivity.class
                ))
        );

        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(
                        AdminActivity.this,
                        NotificationActivity.class
                ))
        );

        btnAdminSettings.setOnClickListener(v ->
                startActivity(new Intent(
                        AdminActivity.this,
                        SettingsActivity.class
                ))
        );
    }
}