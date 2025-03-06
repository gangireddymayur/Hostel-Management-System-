package com.example.loginpage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentScreen extends AppCompatActivity {
    TextView head;
    String rollNumber;
    private FirebaseDatabase db;
    private DatabaseReference reference;

    // Define the SharedPreferences constant
    public static final String SHARED_PREFS = "sharedScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_studentscreen);

        // Get rollNumber from intent
        Intent it = getIntent();
        rollNumber = it.getStringExtra("key");

        // Initialize Firebase reference
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("students");

        head = findViewById(R.id.headerTitle);

        // Fetch student details when the activity is created
        loadStudentDetails();

        // ImageView click listeners for different actions
        ImageView dayPassIcon = findViewById(R.id.residentsIcon);
        dayPassIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(StudentScreen.this, StudentDayPass.class);
                it.putExtra("rollNumber", rollNumber);
                startActivity(it);
                Toast.makeText(StudentScreen.this, "Day Pass clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // ImageView for "Week or Month Pass"
        ImageView weekMonthPassIcon = findViewById(R.id.billsIcon);
        weekMonthPassIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(StudentScreen.this, StudentWeek.class);
                it.putExtra("rollNumber", rollNumber);
                startActivity(it);
                Toast.makeText(StudentScreen.this, "Week or Month Pass clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // ImageView for "Profile Info"
        ImageView profileIcon = findViewById(R.id.paymentsIcon);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(StudentScreen.this, Studentprofile.class);
                it.putExtra("rollNumber", rollNumber);
                startActivity(it);
                Toast.makeText(StudentScreen.this, "Profile Info clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // ImageView for "Request History"
        ImageView historyIcon = findViewById(R.id.previousIcon);
        historyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(StudentScreen.this, StudentHistory.class);
                it.putExtra("rollNumber", rollNumber);
                startActivity(it);
                Toast.makeText(StudentScreen.this, "Request History clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudentDetails() {
        // Get SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // Check if student details are already saved in SharedPreferences
        String savedName = sharedPreferences.getString("name", null);

        if (savedName != null) {
            // If data exists in SharedPreferences, use it
            head.setText("Welcome, "+ savedName);
        } else {
            // If no data in SharedPreferences, fetch from Firebase
            fetchStudentDetails();
        }
    }

    private void fetchStudentDetails() {
        // Query the student data using roll number
        reference.child(rollNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve student details from Firebase
                    String name = dataSnapshot.child("name").getValue(String.class);
                    // Save the data to SharedPreferences
                    saveStudentDetails(name);
                    head.setText(name);
                } else {
                    // Handle case when the student data is not found
                    Toast.makeText(StudentScreen.this, "Student not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors
                Toast.makeText(StudentScreen.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveStudentDetails(String name) {
        // Save student details in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.apply();
    }
}
