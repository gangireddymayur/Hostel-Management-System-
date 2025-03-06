package com.example.loginpage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class deletemult extends AppCompatActivity {

    private RecyclerView studentRecyclerView;
    private StudentAdapter studentAdapter;
    private List<Student> studentList = new ArrayList<>();
    private List<Student> filteredList = new ArrayList<>();
    private List<Boolean> selectionList = new ArrayList<>();
    private CheckBox selectAllCheckBox;
    private Spinner sectionFilterSpinner;
    private Button deleteSelectedButton;
    private List<String> availableBranches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deletemult);
        EdgeToEdge.enable(this);
        // Initialize views
        studentRecyclerView = findViewById(R.id.rv_student_list);
        selectAllCheckBox = findViewById(R.id.cb_select_all);
        sectionFilterSpinner = findViewById(R.id.spinner_section_filter);
        deleteSelectedButton = findViewById(R.id.btn_delete_selected);

        // Set up RecyclerView
        studentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch student and branch data from Firebase
        fetchStudentsData();

        // Set up Spinner listener
        sectionFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterByBranch(availableBranches.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle what happens when nothing is selected (e.g., show all students)
                filterByBranch("All Branches");
            }
        });

        // Handle "Select All" checkbox logic
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (int i = 0; i < selectionList.size(); i++) {
                selectionList.set(i, isChecked);
            }
            studentAdapter.notifyDataSetChanged();
        });

        // Handle Delete Button click
        deleteSelectedButton.setOnClickListener(v -> deleteSelectedStudents());
    }

    private void fetchStudentsData() {
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("students");
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                studentList.clear();
                selectionList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String rollNumber = snapshot.child("rollNumber").getValue(String.class);
                    String branch = snapshot.child("branch").getValue(String.class);

                    Student student = new Student(name, rollNumber, branch);
                    studentList.add(student);
                    selectionList.add(false); // Initially unselected
                }
                filteredList.addAll(studentList); // Initially show all students
                studentAdapter = new StudentAdapter(filteredList, selectionList);
                studentRecyclerView.setAdapter(studentAdapter);

                // Fetch and set available branches in Spinner
                fetchBranches();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(deletemult.this, "Failed to load students", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBranches() {
        // Fetch unique branch names from Firebase (assuming the branches are stored under "branch" in each student record)
        availableBranches.clear();
        availableBranches.add("All Branches"); // Add a default option for "All Branches"
        for (Student student : studentList) {
            if (!availableBranches.contains(student.getBranch())) {
                availableBranches.add(student.getBranch());
            }
        }

        // Set the adapter for the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableBranches);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionFilterSpinner.setAdapter(adapter);
    }

    private void filterByBranch(String branch) {
        filteredList.clear();
        if (branch.equals("All Branches")) {
            filteredList.addAll(studentList); // Show all students if "All Branches" is selected
        } else {
            for (Student student : studentList) {
                if (student.getBranch().equals(branch)) {
                    filteredList.add(student);
                }
            }
        }
        studentAdapter.notifyDataSetChanged();
    }

    private void deleteSelectedStudents() {
        List<String> selectedRollNumbers = new ArrayList<>();

        // Collect selected students' roll numbers
        for (int i = 0; i < selectionList.size(); i++) {
            if (selectionList.get(i)) {
                selectedRollNumbers.add(filteredList.get(i).getRollNumber());
            }
        }

        // Delete selected students from Firebase
        if (!selectedRollNumbers.isEmpty()) {
            DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("students");
            for (String rollNumber : selectedRollNumbers) {
                studentRef.orderByChild("rollNumber").equalTo(rollNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().removeValue();
                        }
                        // Optionally remove the student from the daypass database as well
                        deleteStudentDaypassData(rollNumber);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(deletemult.this, "Error occurred during deletion", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Toast.makeText(this, "Selected students deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No students selected for deletion", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteStudentDaypassData(String rollNumber) {
        DatabaseReference daypassRef = FirebaseDatabase.getInstance().getReference("daypass");
        daypassRef.orderByChild("studentId").equalTo(rollNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(deletemult.this, "Failed to delete daypass data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

        private List<Student> studentList;
        private List<Boolean> selectionList;

        public StudentAdapter(List<Student> studentList, List<Boolean> selectionList) {
            this.studentList = studentList;
            this.selectionList = selectionList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_student2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Student student = studentList.get(position);

            // Set student details
            holder.studentName.setText(student.getName());
            holder.studentRollNumber.setText("Roll No: " + student.getRollNumber());
            holder.studentBranch.setText("Branch: " + student.getBranch());

            // Handle checkbox state
            holder.selectStudent.setChecked(selectionList.get(position));
            holder.selectStudent.setOnCheckedChangeListener((buttonView, isChecked) -> selectionList.set(position, isChecked));
        }

        @Override
        public int getItemCount() {
            return studentList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox selectStudent;
            TextView studentName;
            TextView studentRollNumber;
            TextView studentBranch;

            ViewHolder(View itemView) {
                super(itemView);
                selectStudent = itemView.findViewById(R.id.cb_select_student);
                studentName = itemView.findViewById(R.id.studentNameTextView);
                studentRollNumber = itemView.findViewById(R.id.studentRollNumberTextView);
                studentBranch = itemView.findViewById(R.id.studentBranchTextView);
            }
        }
    }

    // Student class
    public static class Student {
        private String name;
        private String rollNumber;
        private String branch;

        public Student(String name, String rollNumber, String branch) {
            this.name = name;
            this.rollNumber = rollNumber;
            this.branch = branch;
        }

        public String getName() {
            return name;
        }

        public String getRollNumber() {
            return rollNumber;
        }

        public String getBranch() {
            return branch;
        }
    }
}
