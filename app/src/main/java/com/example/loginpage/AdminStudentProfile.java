package com.example.loginpage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminStudentProfile extends AppCompatActivity {


    private RecyclerView studentsRecyclerView;
    private SearchView searchView;
    private List<Student> studentList;
    private List<Student> filteredList;
    private DatabaseReference databaseReference;
    private StudentAdapter studentAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_student_profile);
        // Initialize views
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView2);
        searchView = findViewById(R.id.searchView);
        EdgeToEdge.enable(this);
        // Initialize student list and Firebase reference
        studentList = new ArrayList<>();
        filteredList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("students");

        // Set up RecyclerView
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter = new StudentAdapter(this, filteredList); // Use filteredList
        studentsRecyclerView.setAdapter(studentAdapter);



        // Fetch all students from Firebase
        fetchStudents();

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Nothing needed here for now
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter students as text changes
                filterStudents(newText);
                return false;
            }
        });
    }

    // Fetch students from Firebase
    private void fetchStudents() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                studentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Student student = snapshot.getValue(Student.class);
                    if (student != null) {
                        studentList.add(student);
                    }
                }
                filteredList.clear();
                filteredList.addAll(studentList);
                studentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminStudentProfile.this, "Failed to load students", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Filter students based on search query
    private void filterStudents(String query) {
        filteredList.clear();
        if (TextUtils.isEmpty(query)) {
            // If query is empty, show all students
            filteredList.addAll(studentList);
        } else {
            for (Student student : studentList) {
                if (!TextUtils.isEmpty(student.getName()) && student.getName().toLowerCase().contains(query.toLowerCase()) ||
                        !TextUtils.isEmpty(student.getRollNumber()) && student.getRollNumber().contains(query)) {
                    filteredList.add(student);
                }
            }
        }
        studentAdapter.notifyDataSetChanged();
    }

    // Student adapter class
    private static class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

        private final List<Student> students;
        private final AdminStudentProfile context;

        public StudentAdapter(AdminStudentProfile context, List<Student> students) {
            this.context = context;
            this.students = students;
        }

        @Override
        public StudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_student, parent, false);
            return new StudentViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(StudentViewHolder holder, int position) {
            Student student = students.get(position);

            holder.nameTextView.setText(student.getName());
            holder.rollNumberTextView.setText("Roll No: " + student.getRollNumber());
            holder.branchTextView.setText("Branch: " + student.getBranch());

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, StudentDetailActivity.class);
                intent.putExtra("studentId", student.getRollNumber());
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return students.size();
        }

        // ViewHolder for individual student item
        static class StudentViewHolder extends RecyclerView.ViewHolder {

            TextView nameTextView, rollNumberTextView, branchTextView;

            public StudentViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.studentNameTextView);
                rollNumberTextView = itemView.findViewById(R.id.studentRollNumberTextView);
                branchTextView = itemView.findViewById(R.id.studentBranchTextView);
            }
        }
    }
}
