package com.example.loginpage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class modifystudent extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifystudent);
        EdgeToEdge.enable(this);// Make sure the XML is named `activity_modify_student.xml`

        // Find the card views by their IDs
        CardView cardAddStudent = findViewById(R.id.cardAddStudent);
        CardView cardDeleteStudent = findViewById(R.id.cardDeleteStudent);
        CardView cardAddMultipleStudents = findViewById(R.id.cardAddMultipleStudents);
        CardView carddeleteMultipleStudents=findViewById(R.id.carddeleteMultipleStudents);

        // Set click listeners for each card
        cardAddStudent.setOnClickListener(view -> {
            // Action for Add Student
            Toast.makeText(this, "Add Student clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, addstudent.class);
            startActivity(intent);
            // Intent example: Navigate to AddStudentActivity

        });

        cardDeleteStudent.setOnClickListener(view -> {
            // Action for Delete Student
            Toast.makeText(this, "Delete Student clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, deletestudent.class);
            startActivity(intent);
            // Intent example: Navigate to DeleteStudentActivity

        });

        cardAddMultipleStudents.setOnClickListener(view -> {
            // Action for Add Multiple Students
            Toast.makeText(this, "Add Multiple Students clicked", Toast.LENGTH_SHORT).show();
            // Intent example: Navigate to AddMultipleStudentsActivity
            Intent intent = new Intent(this, AdminAddMult.class);
            startActivity(intent);
        });

        carddeleteMultipleStudents.setOnClickListener(view -> {
            // Action for Add Multiple Students
            Toast.makeText(this, "Delete Multiple Students clicked", Toast.LENGTH_SHORT).show();
            // Intent example: Navigate to AddMultipleStudentsActivity
            Intent intent = new Intent(this, deletemult.class);
            startActivity(intent);
        });
    }
}