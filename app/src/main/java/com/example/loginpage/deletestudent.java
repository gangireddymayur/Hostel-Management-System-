package com.example.loginpage;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class deletestudent extends AppCompatActivity {

    private TextInputEditText searchRollNumber;
    private Button deleteStudentButton;
    private TextView studentName, studentRollNumber, studentPhone, studentBranch;
    private ImageView searchIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deletestudent);

        // Initialize UI elements
        searchRollNumber = findViewById(R.id.searchRollNumber);
        deleteStudentButton = findViewById(R.id.deleteStudentButton);
        studentName = findViewById(R.id.studentName);
        studentRollNumber = findViewById(R.id.studentRollNumber);
        studentPhone = findViewById(R.id.studentPhone);
        studentBranch = findViewById(R.id.studentBranch);
        searchIcon = findViewById(R.id.searchIcon);

        // Set onClickListener for Search Icon
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rollNumber = searchRollNumber.getText().toString().trim();

                if (rollNumber.isEmpty()) {
                    Toast.makeText(deletestudent.this, "Please enter a roll number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Perform the search
                searchStudent(rollNumber);
            }
        });

        // Set onClickListener for Delete button
        deleteStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rollNumber = searchRollNumber.getText().toString().trim();

                if (rollNumber.isEmpty()) {
                    Toast.makeText(deletestudent.this, "Please enter a roll number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Call function to delete the student
                deleteStudent(rollNumber);
                deleteDaypass(rollNumber);
                weekMonthpass(rollNumber);
            }
        });
    }

    private void searchStudent(String rollNumber) {
        // Check if student exists
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("students");
        studentRef.orderByChild("rollNumber").equalTo(rollNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String roll = snapshot.child("rollNumber").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        String branch = snapshot.child("branch").getValue(String.class);

                        // Display the student details
                        studentName.setText("Name: " + name);
                        studentRollNumber.setText("Roll Number: " + roll);
                        studentPhone.setText("Phone: " + phone);
                        studentBranch.setText("Branch: " + branch);

                        // Enable the delete
                        studentName.setVisibility(View.VISIBLE);
                        studentBranch.setVisibility(View.VISIBLE);
                        studentRollNumber.setVisibility(View.VISIBLE);
                        studentPhone.setVisibility(View.VISIBLE);
                        deleteStudentButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    // Clear the details if no student is found
                    clearStudentDetails();
                    Toast.makeText(deletestudent.this, "No student found with that roll number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(deletestudent.this, "Error occurred. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteStudent(String rollNumber) {
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("students");
        studentRef.orderByChild("rollNumber").equalTo(rollNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue(); // Remove the student data
                    }
                    Toast.makeText(deletestudent.this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                    clearStudentDetails(); // Clear student details after deletion
                } else {
                    Toast.makeText(deletestudent.this, "No student found with that roll number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(deletestudent.this, "Error occurred. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteDaypass(String rollNumber) {
        DatabaseReference daypassRef = FirebaseDatabase.getInstance().getReference("daypass");
        daypassRef.orderByChild("studentId").equalTo(rollNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue(); // Remove daypass data
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(deletestudent.this, "Error occurred while deleting daypass data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void weekMonthpass(String rollNumber) {
        DatabaseReference daypassRef = FirebaseDatabase.getInstance().getReference("weekMonthPass");
        daypassRef.orderByChild("studentId").equalTo(rollNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue(); // Remove daypass data
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(deletestudent.this, "Error occurred while deleting daypass data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearStudentDetails() {
        studentName.setText("");
        studentRollNumber.setText("");
        studentPhone.setText("");
        studentBranch.setText("");
        studentName.setVisibility(View.GONE);
        studentBranch.setVisibility(View.GONE);
        studentRollNumber.setVisibility(View.GONE);
        studentPhone.setVisibility(View.GONE);
        deleteStudentButton.setVisibility(View.GONE); // Hide the delete button
    }
}
