package com.archi.festive_hub;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class EventReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "event_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        String eventId = intent.getStringExtra("eventId");
        String eventName = intent.getStringExtra("eventName");
        String eventDate = intent.getStringExtra("eventDate");
        String eventLocation = intent.getStringExtra("eventLocation");

        if (eventName == null || eventName.trim().isEmpty()) {
            eventName = "Your event";
        }

        createNotificationChannel(context);

        Intent openIntent = new Intent(context, EventDetail.class);
        openIntent.putExtra("eventId", eventId);
        openIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        int requestCode = eventId == null ? 100 : eventId.hashCode();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        StringBuilder message =
                new StringBuilder("Your event is coming up!");

        if (eventDate != null && !eventDate.trim().isEmpty()) {
            message.append("\nDate: ").append(eventDate);
        }

        if (eventLocation != null && !eventLocation.trim().isEmpty()) {
            message.append("\nLocation: ").append(eventLocation);
        }

        Notification notification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(eventName)
                        .setContentText("Your event reminder")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message.toString()))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build();

        NotificationManagerCompat manager =
                NotificationManagerCompat.from(context);

        try {
            manager.notify(requestCode, notification);
        } catch (SecurityException e) {
            android.util.Log.e(
                    "EventReminder",
                    "Notification permission is not granted",
                    e
            );
        }
    }

    private void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Event Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription(
                    "Notifications before bookmarked events"
            );

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}