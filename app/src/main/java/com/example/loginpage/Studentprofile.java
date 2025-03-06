package com.example.loginpage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Studentprofile extends AppCompatActivity {

    private TextView nameTextView, phoneTextView, editTextView, rollNumberTextView;
    private EditText yearEditText, passwordEditText, branchEditText, roomNumberEditText;
    private Button logoutButton, saveButton;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private String rollNumber;

    private boolean isEditing = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_studentprofile);

        // Link UI elements to Java code
        nameTextView = findViewById(R.id.nameText);
        yearEditText = findViewById(R.id.yearEditText);
        phoneTextView = findViewById(R.id.phoneText);
        passwordEditText = findViewById(R.id.passwordEditText); // Password field
        rollNumberTextView = findViewById(R.id.rollNumberText);
        logoutButton = findViewById(R.id.logoutButton);
        saveButton = findViewById(R.id.saveButton);
        editTextView = findViewById(R.id.edit); // Edit button
        branchEditText = findViewById(R.id.branchText); // EditText for Branch
        roomNumberEditText = findViewById(R.id.roomNumberText); // EditText for Room Number

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("students");

        Intent intent = getIntent();
        rollNumber = intent.getStringExtra("rollNumber");

        // Load saved student details from SharedPreferences
        loadStudentDetails();

        // Logout button functionality
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(Studentprofile.this)
                    .setTitle("Logout Confirmation")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        clearSharedPreferences();
                        Intent intent1 = new Intent(Studentprofile.this, MainActivity.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent1);
                        Toast.makeText(Studentprofile.this, "Logout successful", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Save button functionality
        saveButton.setOnClickListener(v -> {
            String updatedYear = yearEditText.getText().toString();
            String updatedPassword = passwordEditText.getText().toString(); // Get the password from the EditText
            String updatedBranch = branchEditText.getText().toString();
            String updatedRoomNumber = roomNumberEditText.getText().toString();

            // Save the updated data to Firebase and SharedPreferences
            updateStudentDetails(updatedYear, updatedPassword, updatedBranch, updatedRoomNumber);

            // Disable editing after saving
            toggleEditing(false);
        });

        // Edit button functionality
        editTextView.setOnClickListener(v -> toggleEditing(!isEditing));
    }

    private void toggleEditing(boolean enableEditing) {
        isEditing = enableEditing;

        // Enable or disable EditTexts based on state
        yearEditText.setFocusableInTouchMode(enableEditing);
        yearEditText.setCursorVisible(enableEditing);

        passwordEditText.setFocusableInTouchMode(enableEditing);
        passwordEditText.setCursorVisible(enableEditing);

        branchEditText.setFocusableInTouchMode(enableEditing);
        branchEditText.setCursorVisible(enableEditing);

        roomNumberEditText.setFocusableInTouchMode(enableEditing);
        roomNumberEditText.setCursorVisible(enableEditing);

        // Show or hide the Save button based on editing state
        saveButton.setVisibility(enableEditing ? View.VISIBLE : View.GONE);

        // Update the text of the Edit button
        editTextView.setText(enableEditing ? "Cancel" : "Edit");

        if (enableEditing) {
            // If we are editing, clear the EditText fields
            clearEditTexts();
        } else {
            // If we cancel, restore the saved values
            restoreSavedValues();
        }
    }

    private void clearEditTexts() {
        yearEditText.setText("");
        passwordEditText.setText("");
        branchEditText.setText("");
        roomNumberEditText.setText("");
    }

    private void restoreSavedValues() {
        SharedPreferences sharedPreferences = getSharedPreferences("StudentDetails", MODE_PRIVATE);

        String savedYear = sharedPreferences.getString("year", null);
        String savedBranch = sharedPreferences.getString("branch", null);
        String savedRoom = sharedPreferences.getString("roomNumber", null);
        String savedPassword = sharedPreferences.getString("password", null);

        if (savedYear != null) yearEditText.setText(savedYear);
        if (savedBranch != null) branchEditText.setText(savedBranch);
        if (savedRoom != null) roomNumberEditText.setText(savedRoom);
        if (savedPassword != null) passwordEditText.setText(savedPassword);
    }

    private void loadStudentDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("StudentDetails", MODE_PRIVATE);

        String savedName = sharedPreferences.getString("name", null);
        String savedRoll = sharedPreferences.getString("rollNumber", null);
        String savedBranch = sharedPreferences.getString("branch", null);
        String savedYear = sharedPreferences.getString("year", null);
        String savedRoom = sharedPreferences.getString("roomNumber", null);
        String savedPhone = sharedPreferences.getString("phone", null);
        String savedPassword = sharedPreferences.getString("password", null); // Add saved password

        if (savedName != null && savedRoll != null && savedBranch != null && savedYear != null && savedRoom != null && savedPhone != null && savedPassword != null) {
            nameTextView.setText(savedName);
            rollNumberTextView.setText(savedRoll);
            phoneTextView.setText(savedPhone);

            branchEditText.setText(savedBranch);
            yearEditText.setText(savedYear);
            roomNumberEditText.setText(savedRoom);
            passwordEditText.setText(savedPassword); // Display password
        } else {
            fetchStudentDetails();
        }
    }

    private void fetchStudentDetails() {
        databaseReference.child(rollNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String branch = dataSnapshot.child("branch").getValue(String.class);
                    String year = dataSnapshot.child("year").getValue(String.class);
                    String room = dataSnapshot.child("roomNumber").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String password = dataSnapshot.child("password").getValue(String.class); // Fetch password

                    saveStudentDetails(name, rollNumber, branch, year, room, phone, password); // Save password

                    nameTextView.setText(name);
                    rollNumberTextView.setText(rollNumber);
                    branchEditText.setText(branch);
                    yearEditText.setText(year);
                    roomNumberEditText.setText(room);
                    phoneTextView.setText(phone);
                    passwordEditText.setText(password); // Display password
                } else {
                    Toast.makeText(Studentprofile.this, "Student not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Studentprofile.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStudentDetails(String updatedYear, String updatedPassword, String updatedBranch, String updatedRoomNumber) {
        databaseReference.child(rollNumber).child("year").setValue(updatedYear);
        databaseReference.child(rollNumber).child("password").setValue(updatedPassword); // Update password
        databaseReference.child(rollNumber).child("branch").setValue(updatedBranch);
        databaseReference.child(rollNumber).child("roomNumber").setValue(updatedRoomNumber);

        saveStudentDetails(
                nameTextView.getText().toString(),
                rollNumber,
                updatedBranch,
                updatedYear,
                updatedRoomNumber,
                phoneTextView.getText().toString(),
                updatedPassword // Save password
        );

        Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void saveStudentDetails(String name, String roll, String branch, String year, String room, String phone, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("StudentDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("rollNumber", roll);
        editor.putString("branch", branch);
        editor.putString("year", year);
        editor.putString("roomNumber", room);
        editor.putString("phone", phone);
        editor.putString("password", password); // Save password
        editor.apply();
    }

    private void clearSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("StudentDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        SharedPreferences sharedPreferences1 = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor.clear();
        editor.apply();
        editor1.clear();
        editor1.apply();
    }
}


