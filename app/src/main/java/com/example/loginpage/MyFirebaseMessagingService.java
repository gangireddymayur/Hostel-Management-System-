package com.example.loginpage;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseService";
    private static final String CHANNEL_ID = "day_pass_channel";

    // This method is called when a new message is received
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "DataSnapshot existsssssssssssssssssssssssssssssssssssssssssssssssssss");
        // Handle the incoming message here, if needed
        // Trigger day pass monitor if needed
        monitorDayPassRequests();
    }

    public void monitorDayPassRequests() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("dayPasses");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "DataSnapshot exists: " + dataSnapshot.getValue());
                } else {
                    Log.d(TAG, "No data exists in the database.");
                }

                // Iterate through dayPasses and check for "pending" status
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("pending".equalsIgnoreCase(status)) {
                        String studentId = snapshot.child("studentId").getValue(String.class);
                        String dateOfLeave = snapshot.child("dateOfLeave").getValue(String.class);
                        String reason = snapshot.child("reason").getValue(String.class);
                        String title = "New Pending Day Pass Request";
                        String message = "Student ID: " + studentId + " requested leave on " + dateOfLeave + ". Reason: " + reason;

                        // Trigger a notification
                        sendNotification(title, message);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error fetching data: " + databaseError.getMessage());
            }
        });
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Day Pass Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new day pass requests");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24) // Replace with your drawable
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // For longer messages
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Show the notification
        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }
}
