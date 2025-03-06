package com.example.loginpage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import java.util.Objects;

public class AdminStudentWeek extends AppCompatActivity {

    private RecyclerView requestRecyclerView;
    private RequestAdapter adapter;
    private List<StudentWeekRequest> pendingRequests;
    private List<StudentWeekRequest> filteredRequests;
    private DatabaseReference databaseReference;
    private SearchView searchView;
    private Handler handler;
    private Runnable filterRunnable;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_student_week);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("weekMonthPass");
        EdgeToEdge.enable(this);

        // Initialize UI components
        requestRecyclerView = findViewById(R.id.requestsRecyclerView);
        searchView = findViewById(R.id.searchEditText1);

        pendingRequests = new ArrayList<>();
        filteredRequests = new ArrayList<>();

        // Set up RecyclerView with LinearLayoutManager
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up adapter
        adapter = new RequestAdapter(this, filteredRequests, this);
        requestRecyclerView.setAdapter(adapter);

        // Fetch pending week requests from Firebase
        fetchPendingRequests();

        // SearchView debounce logic
        handler = new Handler();
        filterRunnable = () -> filterRequests(searchView.getQuery().toString());

        // Add search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                handler.removeCallbacks(filterRunnable);
                handler.postDelayed(filterRunnable, 300);
                return false;
            }
        });
    }

    private void fetchPendingRequests() {
        databaseReference.orderByChild("status").equalTo("pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear existing lists to avoid duplicates
                pendingRequests.clear();
                filteredRequests.clear();

                // Find the empty TextView in the layout
                TextView emptyTextView = findViewById(R.id.emptyTextView);

                if (snapshot.exists()) {
                    // Loop through each request in the snapshot
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String id = data.getKey();
                        StudentWeekRequest request = data.getValue(StudentWeekRequest.class);

                        // Only process valid requests
                        if (request != null) {
                            request.setId(id);
                            fetchStudentDetails(request); // Populate student details
                        }
                    }
                    // Hide "No requests" message if data is available
                    emptyTextView.setVisibility(View.GONE);
                } else {
                    // Show "No pending requests" message
                    emptyTextView.setVisibility(View.VISIBLE);
                }

                // Notify adapter of data changes
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminStudentWeek.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStudentDetails(StudentWeekRequest request) {
        DatabaseReference studentsReference = FirebaseDatabase.getInstance().getReference("students");
        if (request.getStudentId() == null) return;

        studentsReference.child(request.getStudentId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String roll = snapshot.child("rollNumber").getValue(String.class);
                    String branch = snapshot.child("branch").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);

                    // Handle null values and set default values
                    request.setName(name != null ? name : "Unknown");
                    request.setRoll(roll != null ? roll : "Unknown");
                    request.setBranch(branch != null ? branch : "Unknown");
                    request.setPhone(phone != null ? phone : "Unknown");

                    pendingRequests.add(request);
                    filteredRequests.add(request);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminStudentWeek.this, "Failed to fetch student details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterRequests(String query) {
        filteredRequests.clear();
        if (query == null || query.isEmpty()) {
            filteredRequests.addAll(pendingRequests);
        } else {
            String queryLower = query.toLowerCase();
            for (StudentWeekRequest request : pendingRequests) {
                if (Objects.equals(request.getName().toLowerCase(), queryLower)
                        || Objects.equals(request.getRoll().toLowerCase(), queryLower)
                        || Objects.equals(request.getBranch().toLowerCase(), queryLower)) {
                    filteredRequests.add(request);
                }
            }
        }
        if (filteredRequests.isEmpty() && !query.isEmpty()) {
            Toast.makeText(AdminStudentWeek.this, "No matching requests found", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }

    private void updateRequestStatus(StudentWeekRequest request, String newStatus) {
        databaseReference.child(request.getId()).child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    request.setStatus(newStatus);
                    filteredRequests.remove(request); // Remove from filtered list after update
                    pendingRequests.remove(request); // Remove from pending requests
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Request updated to " + newStatus, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show());
    }

    public static class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
        private List<StudentWeekRequest> requestList;
        private Context context;
        private AdminStudentWeek adminStudentWeek;

        public RequestAdapter(Context context, List<StudentWeekRequest> requestList, AdminStudentWeek adminStudentWeek) {
            this.context = context;
            this.requestList = requestList;
            this.adminStudentWeek = adminStudentWeek;
        }

        @NonNull
        @Override
        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_week_month, parent, false);
            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
            StudentWeekRequest request = requestList.get(position);
            holder.nameTextView.setText("Name: " + request.getName());
            holder.rollNumberTextView.setText("Roll Number: " + request.getRoll());
            holder.phoneTextView.setText("Phone: " + request.getPhone());
            holder.branchTextView.setText("Branch: " + request.getBranch());
            holder.dateTextView.setText("Request Date: " + request.getDate());
            holder.dateOfLeaveTextView.setText("Leave Date: " + request.getDateOfLeave());
            holder.dateOfReturnTextView.setText("Return Date: " + request.getDateofReturn());
            holder.reasonTextView.setText("Reason: " + request.getReason());
            holder.acceptButton.setOnClickListener(v -> adminStudentWeek.updateRequestStatus(request, "accepted"));
            holder.rejectButton.setOnClickListener(v -> adminStudentWeek.updateRequestStatus(request, "rejected"));
        }

        @Override
        public int getItemCount() {
            return requestList.size();
        }

        public static class RequestViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView, rollNumberTextView, phoneTextView, branchTextView;
            TextView dateTextView, dateOfLeaveTextView, dateOfReturnTextView, reasonTextView;
            Button acceptButton, rejectButton;

            public RequestViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                rollNumberTextView = itemView.findViewById(R.id.rollNumberTextView);
                phoneTextView = itemView.findViewById(R.id.phoneTextView);
                branchTextView = itemView.findViewById(R.id.branchTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                dateOfLeaveTextView = itemView.findViewById(R.id.dateOfLeaveTextView);
                dateOfReturnTextView = itemView.findViewById(R.id.dateOfReturnTextView);
                reasonTextView = itemView.findViewById(R.id.reasonTextView);
                acceptButton = itemView.findViewById(R.id.acceptButton);
                rejectButton = itemView.findViewById(R.id.rejectButton);
            }
        }
    }

    public static class StudentWeekRequest {
        private String id;
        private String date;
        private String dateOfLeave;
        private String dateofReturn;
        private String reason;
        private String status;
        private String studentId;
        private String name;
        private String roll;
        private String branch;
        private String phone;

        public StudentWeekRequest() {}

        public StudentWeekRequest(String date, String dateOfLeave, String dateofReturn, String reason, String status, String studentId) {
            this.date = date;
            this.dateOfLeave = dateOfLeave;
            this.dateofReturn = dateofReturn;
            this.reason = reason;
            this.status = status;
            this.studentId = studentId;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getDateOfLeave() { return dateOfLeave; }
        public void setDateOfLeave(String dateOfLeave) { this.dateOfLeave = dateOfLeave; }
        public String getDateofReturn() { return dateofReturn; }
        public void setDateofReturn(String dateofReturn) { this.dateofReturn = dateofReturn; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRoll() { return roll; }
        public void setRoll(String roll) { this.roll = roll; }
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
