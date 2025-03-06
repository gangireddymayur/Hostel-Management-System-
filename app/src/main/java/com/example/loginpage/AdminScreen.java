package com.example.loginpage;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminScreen extends AppCompatActivity {
    int pendingCount=0,pendingCount1=0;
    TextView head, pendingRequests;
    Button logout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_screen);

        head = findViewById(R.id.headerTitle); // Header title
        pendingRequests = findViewById(R.id.pendingRequests); // New TextView for pending requests
        logout = findViewById(R.id.logout);

        // Display a dynamic greeting message
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        String adminName = sharedPreferences.getString("username", "Admin");
        head.setText("Welcome, " + "Admin");

        // Check for pending requests
        checkPendingRequests();
        handleDayPassRequests();
        handleWeekMonthPassRequests();

        // Day Pass
        ImageView dayPassIcon = findViewById(R.id.residentsIcon);
        dayPassIcon.setOnClickListener(v -> {
            Intent it = new Intent(AdminScreen.this, AdminDayPass.class);
            startActivity(it);
            Toast.makeText(AdminScreen.this, "Day Pass clicked", Toast.LENGTH_SHORT).show();
        });

        // Week/Month Pass
        ImageView weekMonthPassIcon = findViewById(R.id.billsIcon);
        weekMonthPassIcon.setOnClickListener(v -> {
            Intent it = new Intent(AdminScreen.this, AdminStudentWeek.class);
            startActivity(it);
            Toast.makeText(AdminScreen.this, "Week or Month Pass clicked", Toast.LENGTH_SHORT).show();
        });

        // Student Info
        ImageView profileIcon = findViewById(R.id.paymentsIcon);
        profileIcon.setOnClickListener(v -> {
            Intent it = new Intent(AdminScreen.this, AdminStudentProfile.class);
            startActivity(it);
            Toast.makeText(AdminScreen.this, "Student Info clicked", Toast.LENGTH_SHORT).show();
        });

        // Request History
        ImageView historyIcon = findViewById(R.id.previousIcon);
        historyIcon.setOnClickListener(v -> {
            Intent it = new Intent(AdminScreen.this, modifystudent.class);
            startActivity(it);
            Toast.makeText(AdminScreen.this, "History clicked", Toast.LENGTH_SHORT).show();
        });

        // Logout with confirmation
        logout.setOnClickListener(v -> {
            new AlertDialog.Builder(AdminScreen.this)
                    .setTitle("Logout Confirmation")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        clearSharedPreferences();
                        Intent intent = new Intent(AdminScreen.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Toast.makeText(AdminScreen.this, "Logout successful", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void checkPendingRequests() {
        DatabaseReference dayPassRef = FirebaseDatabase.getInstance().getReference("daypass");
        DatabaseReference dayPassRef2 = FirebaseDatabase.getInstance().getReference("weekMonthPass");

        dayPassRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pendingCount = 0;
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    String status = requestSnapshot.child("status").getValue(String.class);
                    if ("pending".equalsIgnoreCase(status)) {
                        pendingCount++;
                    }
                }
                updatePendingRequestsCount(pendingCount1+pendingCount);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminScreen.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        dayPassRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pendingCount1 = 0;
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    String status = requestSnapshot.child("status").getValue(String.class);
                    if ("pending".equalsIgnoreCase(status)) {
                        pendingCount1++;
                    }
                }
                updatePendingRequestsCount(pendingCount+pendingCount1);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminScreen.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePendingRequestsCount(int count) {
        pendingRequests.setText("Pending Requests: " + count);
    }

    private void handleWeekMonthPassRequests() {
        DatabaseReference weekMonthPassRef = FirebaseDatabase.getInstance().getReference("weekMonthPass");

        weekMonthPassRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    String status = requestSnapshot.child("status").getValue(String.class);
                    String studentId = requestSnapshot.child("studentId").getValue(String.class);
                    String dateOfLeave = requestSnapshot.child("dateOfLeave").getValue(String.class);
                    String dateOfReturn = requestSnapshot.child("dateofReturn").getValue(String.class);

                    if ("pending".equalsIgnoreCase(status) && studentId != null && dateOfLeave != null && dateOfReturn != null) {
                        String notificationKey = "weekMonthPass_" + studentId + "_" + dateOfLeave + "_" + dateOfReturn;
                        sendUniqueNotification(notificationKey, "New Week/Month Pass Request", "Student ID: " + studentId + " requested leave from " + dateOfLeave + " to " + dateOfReturn);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminScreen.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDayPassRequests() {
        DatabaseReference dayPassRef = FirebaseDatabase.getInstance().getReference("daypass");

        dayPassRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    String status = requestSnapshot.child("status").getValue(String.class);
                    String studentId = requestSnapshot.child("studentId").getValue(String.class);
                    String dateOfLeave = requestSnapshot.child("dateOfLeave").getValue(String.class);

                    if ("pending".equalsIgnoreCase(status) && studentId != null && dateOfLeave != null) {
                        String notificationKey = "dayPass_" + studentId + "_" + dateOfLeave;
                        sendUniqueNotification(notificationKey, "New Day Pass Request", "Student ID: " + studentId + " requested leave on " + dateOfLeave);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminScreen.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendUniqueNotification(String notificationKey, String title, String message) {
        SharedPreferences sharedPreferences = getSharedPreferences("AdminNotifications", MODE_PRIVATE);
        boolean isNotified = sharedPreferences.getBoolean(notificationKey, false);

        if (!isNotified) {
            String channelId = "admin_notifications";
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Admin Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.baseline_circle_notifications_24) // Replace with your notification icon
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            // Use a unique ID for each notification
            int notificationId = notificationKey.hashCode();
            notificationManager.notify(notificationId, notificationBuilder.build());

            // Mark the notification as sent
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(notificationKey, true);
            editor.apply();
        }
    }

    private void clearSharedPreferences() {
        SharedPreferences sharedPreferences1 = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor1.clear();
        editor1.apply();
    }
}
