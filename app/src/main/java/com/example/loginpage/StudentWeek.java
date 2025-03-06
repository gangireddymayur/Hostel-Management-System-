package com.example.loginpage;

import android.app.DatePickerDialog;
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

public class StudentWeek extends AppCompatActivity {

    private String roll;
    private TextView studentName, studentRollNumber, studentBranch;
    private TextView  pending;
    private EditText leaveReason;
    private Button selectLeaveDate, selectReturnDate, requestButton;
    private String leaveDate = "", returnDate = "";
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference studentRef, weekMonthPassRef;
    private boolean requestExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_weekor_month);
        EdgeToEdge.enable(this);
        // Get roll number from Intent
        roll = getIntent().getStringExtra("rollNumber");

        // Firebase references
        studentRef = database.getReference("students").child(roll);
        weekMonthPassRef = database.getReference("weekMonthPass");

        // Initialize views
        studentName = findViewById(R.id.studentName);
        studentRollNumber = findViewById(R.id.studentRollNumber);
        pending=findViewById(R.id.alreadyrequested);
        studentBranch = findViewById(R.id.studentBranch);
        leaveReason = findViewById(R.id.leaveReason);
        selectLeaveDate = findViewById(R.id.selectLeaveDate);
        selectReturnDate = findViewById(R.id.selectReturnDate);
        requestButton = findViewById(R.id.requestButton);

        // Fetch student details
        fetchStudentDetails();

        // Fetch any existing leave requests
        fetchExistingRequests();

        // Date picker for leave date
        selectLeaveDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(StudentWeek.this,
                    (view, year1, monthOfYear, dayOfMonth1) -> {
                        leaveDate = dayOfMonth1 + "-" + (monthOfYear + 1) + "-" + year1;
                        selectLeaveDate.setText("Leave Date: " + leaveDate);
                    }, year, month, dayOfMonth).show();
        });

        // Date picker for return date
        selectReturnDate.setOnClickListener(v -> {
            if (leaveDate.isEmpty()) {
                Toast.makeText(StudentWeek.this, "Please select leave date first.", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(StudentWeek.this,
                    (view, year1, monthOfYear, dayOfMonth1) -> {
                        returnDate = dayOfMonth1 + "-" + (monthOfYear + 1) + "-" + year1;
                        selectReturnDate.setText("Return Date: " + returnDate);
                    }, year, month, dayOfMonth).show();
        });

        // Handle request button click
        requestButton.setOnClickListener(v -> {
            requestButton.setText("Already Requested");
            pending.setVisibility(View.VISIBLE);
            if (leaveDate.isEmpty() || returnDate.isEmpty()) {
                Toast.makeText(StudentWeek.this, "Please select both leave date and return date.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (requestExists) {
                Toast.makeText(StudentWeek.this, "You already have a pending or active leave request.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the current date
            String currentDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-"
                    + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "-"
                    + Calendar.getInstance().get(Calendar.YEAR);

            // Create the leave request object
            WeekMonthPass request = new WeekMonthPass(
                    currentDate,         // Request date
                    leaveDate,           // Leave date
                    returnDate,          // Return date
                    leaveReason.getText().toString(), // Reason for leave
                    "pending",           // Status
                    roll                 // Student ID
            );

            // Push the new request to Firebase under a unique ID
            String uniqueId = weekMonthPassRef.push().getKey();
            if (uniqueId != null) {
                weekMonthPassRef.child(uniqueId).setValue(request);

                // Disable inputs
                pending.setVisibility(View.VISIBLE);
                leaveReason.setEnabled(false);
                selectLeaveDate.setEnabled(false);
                selectReturnDate.setEnabled(false);
                requestButton.setEnabled(false);

                // Show the request details


                Toast.makeText(StudentWeek.this, "Leave request submitted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(StudentWeek.this, "Failed to submit request.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch student details
    private void fetchStudentDetails() {
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String branch = dataSnapshot.child("branch").getValue(String.class);

                    // Update the TextViews with student data
                    studentName.setText("Name: " + name);
                    studentRollNumber.setText("Roll Number: " + roll);
                    studentBranch.setText("Branch: " + branch);
                } else {
                    Toast.makeText(StudentWeek.this, "Student details not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StudentWeek.this, "Failed to fetch student details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch existing requests for the student
    private void fetchExistingRequests() {
        weekMonthPassRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    String studentId = requestSnapshot.child("studentId").getValue(String.class);
                    String status = requestSnapshot.child("status").getValue(String.class);

                    if (roll.equals(studentId) && ("pending".equals(status) || "active".equals(status))) {
                        requestExists = true;

                        // Display existing request details
                        String reason = requestSnapshot.child("reason").getValue(String.class);
                        String leaveDateFromDb = requestSnapshot.child("dateOfLeave").getValue(String.class);
                        String returnDateFromDb = requestSnapshot.child("dateofReturn").getValue(String.class);
                        // Disable inputs
                        pending.setVisibility(View.VISIBLE);
                        leaveReason.setEnabled(false);
                        selectLeaveDate.setEnabled(false);
                        selectReturnDate.setEnabled(false);
                        requestButton.setEnabled(false);
                        requestButton.setText("Already Requested");
                        break;
                    }
                    else
                    {
                        pending.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StudentWeek.this, "Failed to fetch leave requests.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // WeekMonthPass class
    public static class WeekMonthPass {
        public String date;
        public String dateOfLeave;
        public String dateofReturn;
        public String reason;
        public String status;
        public String studentId;

        public WeekMonthPass(String date, String dateOfLeave, String dateofReturn, String reason, String status, String studentId) {
            this.date = date;
            this.dateOfLeave = dateOfLeave;
            this.dateofReturn = dateofReturn;
            this.reason = reason;
            this.status = status;
            this.studentId = studentId;
        }
    }
}
