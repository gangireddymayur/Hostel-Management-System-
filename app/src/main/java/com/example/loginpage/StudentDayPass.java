package com.example.loginpage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class StudentDayPass extends AppCompatActivity {

    String roll; // This should be dynamically fetched based on the logged-in user
    TextView studentName, studentRollNumber, studentBranch,pending;
    EditText leaveReason;
    Button selectLeaveDate, requestButton;
    String leaveDate = "";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dayPassesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_day_pass);
        Intent intent = getIntent();
        roll = intent.getStringExtra("rollNumber");

        dayPassesRef = database.getReference("daypass"); // Reference to the daypass section

        // Initialize views
        studentName = findViewById(R.id.studentName);
        studentRollNumber = findViewById(R.id.studentRollNumber);
        pending=findViewById(R.id.alreadyrequested);
        studentBranch = findViewById(R.id.studentBranch);
        leaveReason = findViewById(R.id.leaveReason);
        selectLeaveDate = findViewById(R.id.selectLeaveDate);
        requestButton = findViewById(R.id.requestButton);

        // Fetch student data and status from Firebase
        fetchStudentData();

        // Date picker dialog to select leave date
        selectLeaveDate.setOnClickListener(v -> {
            // Get tomorrow's date
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Move to tomorrow's date
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            // Launch a DatePickerDialog
            new android.app.DatePickerDialog(StudentDayPass.this,
                    (view, year1, monthOfYear, dayOfMonth1) -> {
                        // Format the selected date
                        leaveDate = dayOfMonth1 + "-" + (monthOfYear + 1) + "-" + year1; // "dd-MM-yyyy"
                        selectLeaveDate.setText("Leave Date: " + leaveDate);
                    }, year, month, dayOfMonth) {
                @Override
                public void onDateChanged(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    super.onDateChanged(view, year, monthOfYear, dayOfMonth);
                    // Disallow selection of any date earlier than tomorrow
                    if (year < calendar.get(Calendar.YEAR) ||
                            (year == calendar.get(Calendar.YEAR) && monthOfYear < calendar.get(Calendar.MONTH)) ||
                            (year == calendar.get(Calendar.YEAR) && monthOfYear == calendar.get(Calendar.MONTH) && dayOfMonth < calendar.get(Calendar.DAY_OF_MONTH))) {
                        view.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    }
                }
            }.show();
        });

        // Handle request button click
        requestButton.setOnClickListener(v -> {
            pending.setVisibility(View.VISIBLE);
            if (leaveDate.isEmpty()) {
                Toast.makeText(StudentDayPass.this, "Please select a leave date.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the student already made a request
            dayPassesRef.orderByChild("studentId").equalTo(roll).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean hasPendingRequest = false;

                    // Check if there is already a pending request for the student
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String status = snapshot.child("status").getValue(String.class);
                        if ("pending".equals(status)) {
                            hasPendingRequest = true;
                            break; // Stop if a pending request is found
                        }
                    }

                    if (hasPendingRequest) {
                        // If there is a pending request, prevent further requests
                        Toast.makeText(StudentDayPass.this, "You have a pending request already.", Toast.LENGTH_SHORT).show();
                        // Optionally, disable the request button
                        requestButton.setEnabled(false);
                    } else {
                        // Disable the request button temporarily
                        requestButton.setEnabled(false);

                        // Get the current date (for the request date)
                        String currentDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-"
                                + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "-" + Calendar.getInstance().get(Calendar.YEAR);

                        // Create a new day pass request data
                        DayPassRequest request = new DayPassRequest(
                                leaveDate,               // Leave date
                                currentDate,             // Request date
                                leaveReason.getText().toString(),  // Reason for leave
                                "pending",               // Set status to pending
                                roll// Include roll number as part of the request
                        );

                        // Generate a unique ID for the day pass request
                        String uniqueRequestId = dayPassesRef.push().getKey(); // Generate unique ID for new request

                        if (uniqueRequestId != null) {
                            // Add this request to the "daypass" node with the generated unique request ID
                            dayPassesRef.child(uniqueRequestId).setValue(request)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Show confirmation message
                                            Toast.makeText(StudentDayPass.this, "Day pass requested successfully!", Toast.LENGTH_SHORT).show();
                                            // Optionally, disable inputs
                                            leaveReason.setEnabled(false);
                                            selectLeaveDate.setEnabled(false);
                                        } else {
                                            Toast.makeText(StudentDayPass.this, "Failed to request day pass.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors in data fetching
                    Toast.makeText(StudentDayPass.this, "Failed to fetch day pass data.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Fetch student data from Firebase and update UI
    private void fetchStudentData() {
        DatabaseReference studentRef = database.getReference("students").child(roll); // Reference to the specific student

        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Extract the student data from Firebase
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String branch = dataSnapshot.child("branch").getValue(String.class);

                    // Set the fetched data to the TextViews
                    studentName.setText("Name: " + name);
                    studentRollNumber.setText("Roll Number: " + roll);
                    studentBranch.setText("Branch: " + branch);

                    // If the request is already made (check the status from dayPasses)
                    dayPassesRef.orderByChild("studentId").equalTo(roll).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Fetch the first request (assuming only one request is pending)
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String status = snapshot.child("status").getValue(String.class);
                                    String reason = snapshot.child("reason").getValue(String.class);
                                    String leaveDateFromDb = snapshot.child("dateOfLeave").getValue(String.class);

                                    if ("pending".equals(status)) {
                                        pending.setVisibility(View.VISIBLE);
                                        leaveReason.setEnabled(false); // Disable leave reason input field
                                        selectLeaveDate.setEnabled(false); // Disable date selection
                                        requestButton.setEnabled(false); // Disable the request button
                                        requestButton.setText("Already Requested"); // Change the button text
                                    }
                                    else
                                    {
                                        pending.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(StudentDayPass.this, "Failed to fetch day pass data.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(StudentDayPass.this, "Student not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StudentDayPass.this, "Failed to fetch student data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // DayPassRequest class
    public static class DayPassRequest {
        public String date;        // The date the request is made
        public String dateOfLeave; // The date the student wants to take leave
        public String reason;      // Reason for the leave
        public String status;      // Status of the request (pending, accepted, etc.)
        public String studentId;   // The roll number of the student making the request

        public DayPassRequest() {
            // Default constructor required for Firebase
        }

        public DayPassRequest(String dateOfLeave, String date, String reason, String status, String studentId) {
            this.dateOfLeave = dateOfLeave;
            this.date = date;
            this.reason = reason;
            this.status = status;
            this.studentId = studentId;
        }
    }
}