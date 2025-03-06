package com.example.loginpage;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class addstudent extends AppCompatActivity {

    private EditText studentName, studentRollNumber, studentRoomNumber, studentYear, studentBranch, studentPhone, parentPhone;
    private Button addStudentButton;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addstudent);
        EdgeToEdge.enable(this);
        // Initialize the EditText fields
        studentName = findViewById(R.id.studentName);
        studentRollNumber = findViewById(R.id.studentRollNumber);
        studentRoomNumber = findViewById(R.id.studentRoomNumber);
        studentYear = findViewById(R.id.studentYear);
        studentBranch = findViewById(R.id.studentBranch);
        studentPhone = findViewById(R.id.studentPhone);
        parentPhone = findViewById(R.id.parentPhone);

        // Initialize the Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("students");

        // Initialize the button and set its click listener
        addStudentButton = findViewById(R.id.addStudentButton);
        addStudentButton.setOnClickListener(v -> addStudent());
    }

    // Method to add student to Firebase
    private void addStudent() {
        // Get values from EditTexts
        String name = studentName.getText().toString().trim();
        String rollNumber = studentRollNumber.getText().toString().trim();
        String roomNumber = studentRoomNumber.getText().toString().trim();
        String year = studentYear.getText().toString().trim();
        String branch = studentBranch.getText().toString().trim();
        String phone = studentPhone.getText().toString().trim();
        String parentPhoneNumber = parentPhone.getText().toString().trim();
        String password="12345";

        // Validate inputs
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(rollNumber) || TextUtils.isEmpty(roomNumber) ||
                TextUtils.isEmpty(year) || TextUtils.isEmpty(branch) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(parentPhoneNumber)) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a student object
        Student student = new Student(name, rollNumber, roomNumber, year, branch, phone, parentPhoneNumber,password);

        // Check if student already exists by roll number
        databaseReference.child(rollNumber).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Toast.makeText(this, "Student with this roll number already exists", Toast.LENGTH_SHORT).show();
                } else {
                    // Upload student data to Firebase
                    databaseReference.child(rollNumber).setValue(student).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to add student", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Error checking data", Toast.LENGTH_SHORT).show();
            }
        });
        clearFields();
    }
    private void clearFields() {
       studentName.setText("");
       studentBranch.setText("");
       studentPhone.setText("");
       parentPhone.setText("");
       studentYear.setText("");
       studentRollNumber.setText("");
       studentRoomNumber.setText("");
    }
}
