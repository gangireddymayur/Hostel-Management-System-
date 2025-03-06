package com.example.loginpage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Context;

import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

    private ListView listViewStudents;
    Button logout;
    private ArrayList<Student> requestedStudentsList = new ArrayList<>();
    private StudentAdapter studentAdapter;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main3);
        listViewStudents = findViewById(R.id.listViewStudents);
        logout=findViewById(R.id.buttonLogout2);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear credentials and go back to login page
                clearCredentials();
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the home activity
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference("students");

        // Set up custom StudentAdapter for ListView
        studentAdapter = new StudentAdapter(MainActivity2.this, requestedStudentsList);
        listViewStudents.setAdapter(studentAdapter);

        // Fetch data from Firebase and display only approved students

        fetchRequestedStudents();
    }

    private void fetchRequestedStudents() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the list to avoid duplication
                requestedStudentsList.clear();

                // Iterate through all students
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    Student student = studentSnapshot.getValue(Student.class);

                    // Check if the student has requested a gate pass (request = "True")
                    if (student != null && "True".equals(student.getRequest())) {
                        // Add only the requested students to the list
                        requestedStudentsList.add(student);
                    }
                }

                // Notify the adapter to update the ListView
                studentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }


    public static class StudentAdapter extends ArrayAdapter<Student> {

        @SuppressLint("RestrictedApi")
        private MainActivity2 mContext;
        private ArrayList<Student> studentsList;

        public StudentAdapter(@SuppressLint("RestrictedApi") @NonNull MainActivity2 context, ArrayList<Student> list) {
            super(context, 0, list);
            mContext = context;
            studentsList = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Get the student object at the current position
            Student student = studentsList.get(position);

            // Inflate the custom layout
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.custom, parent, false);
            }

            // Get references to the views
            TextView textViewRollNumber = convertView.findViewById(R.id.textViewRollNumber);
            Button buttonApprove = convertView.findViewById(R.id.buttonToggleGatepass);
            Button buttonDisapprove = convertView.findViewById(R.id.buttonToggleGatepass2);

            // Set the student's roll number in the TextView
            textViewRollNumber.setText(student.getRollNumber());

            // Initially set button visibility based on current gatepass status


            // Handle Approve button click
            buttonApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Reference to the student's node in Firebase
                    DatabaseReference studentRef = FirebaseDatabase.getInstance()
                            .getReference("students")
                            .child(student.getRollNumber());

                    // Approve gatepass and update Firebase
                    studentRef.child("gatepass").setValue("approved");
                    studentRef.child("request").setValue("False");  // Optional: change request status
                    student.setGatepass("approved");

                    // Update UI: Show Disapprove button and hide Approve button
                    buttonApprove.setVisibility(View.GONE);
                    buttonDisapprove.setVisibility(View.VISIBLE);
                }
            });

            // Handle Disapprove button click
            buttonDisapprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Reference to the student's node in Firebase
                    DatabaseReference studentRef = FirebaseDatabase.getInstance()
                            .getReference("students")
                            .child(student.getRollNumber());

                    // Disapprove gatepass and update Firebase
                    studentRef.child("gatepass").setValue("disapprove");
                    studentRef.child("request").setValue("False");  // Optional: change request status
                    student.setGatepass("disapprove");

                    // Update UI: Show Approve button and hide Disapprove button
                    buttonApprove.setVisibility(View.VISIBLE);
                    buttonDisapprove.setVisibility(View.GONE);
                }
            });

            return convertView;
        }

    }
    private void clearCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Remove all data
        editor.apply();
    }
}