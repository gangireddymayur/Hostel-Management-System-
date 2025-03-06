package com.example.loginpage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

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

public class AdminDayPass extends AppCompatActivity {

    private RecyclerView requestRecyclerView;
    private DayPassAdapter adapter;
    private List<DayPassRequest> pendingRequests;
    private List<DayPassRequest> filteredRequests;

    private DatabaseReference databaseReference;

    private SearchView searchView;

    private Handler handler;
    private Runnable filterRunnable;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_day_pass);
        EdgeToEdge.enable(this);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("daypass");

        // Initialize UI components
        requestRecyclerView = findViewById(R.id.requestsRecyclerView);
        searchView = findViewById(R.id.searchEditText1);  // This is a SearchView now

        pendingRequests = new ArrayList<>();
        filteredRequests = new ArrayList<>();

        // Set up RecyclerView with LinearLayoutManager
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up adapter
        adapter = new DayPassAdapter(this, filteredRequests);
        requestRecyclerView.setAdapter(adapter);

        // Fetch pending requests from Firebase
        fetchPendingRequests();

        // SearchView debounce logic
        handler = new Handler();
        filterRunnable = new Runnable() {
            @Override
            public void run() {
                filterRequests(searchView.getQuery().toString());
            }
        };

        // Add search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Not required for this case, we are filtering as user types
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Delay filtering to avoid too many calls
                handler.removeCallbacks(filterRunnable);
                handler.postDelayed(filterRunnable, 300); // 300 ms delay before filtering
                return false;
            }
        });
    }

    private void filterRequests(String query) {
        filteredRequests.clear();
        if (query.trim().isEmpty()) {
            filteredRequests.addAll(pendingRequests);
        } else {
            String queryLower = query.toLowerCase();
            for (DayPassRequest request : pendingRequests) {
                if (request.getName().toLowerCase().contains(queryLower)
                        || request.getRoll().toLowerCase().contains(queryLower)
                        || request.getBranch().toLowerCase().contains(queryLower)) {
                    filteredRequests.add(request);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchPendingRequests() {
        // Use addValueEventListener for real-time data fetching
        databaseReference.orderByChild("status").equalTo("pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pendingRequests.clear();
                filteredRequests.clear();
                TextView emptyTextView = findViewById(R.id.emptyTextView);

                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String id = data.getKey();
                        DayPassRequest request = data.getValue(DayPassRequest.class);
                        if (request != null) {
                            request.setId(id);
                            fetchStudentDetails(request);
                        }
                    }
                    emptyTextView.setVisibility(View.GONE);
                } else {
                    emptyTextView.setVisibility(View.VISIBLE);
                    Toast.makeText(AdminDayPass.this, "No pending requests available", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDayPass.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRequestStatus(DayPassRequest request, String newStatus) {
        // Update status in real-time
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

    private void fetchStudentDetails(DayPassRequest request) {
        DatabaseReference studentsReference = FirebaseDatabase.getInstance().getReference("students");
        studentsReference.child(request.getStudentId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String roll = snapshot.child("rollNumber").getValue(String.class);
                    String branch = snapshot.child("branch").getValue(String.class);

                    request.setName(name);
                    request.setRoll(roll);
                    request.setBranch(branch);

                    pendingRequests.add(request);
                    filteredRequests.add(request);
                    adapter.notifyItemInserted(filteredRequests.size() - 1); // Notify when a new item is added
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDayPass.this, "Failed to fetch student details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter for DayPassRequest
    private class DayPassAdapter extends RecyclerView.Adapter<DayPassAdapter.ViewHolder> {

        private final List<DayPassRequest> requests;
        private final android.content.Context context;

        public DayPassAdapter(android.content.Context context, List<DayPassRequest> requests) {
            this.context = context;
            this.requests = requests;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(context).inflate(R.layout.list_item_leave, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DayPassRequest request = requests.get(position);

            holder.dateText.setText("Date: " + request.getDate());
            holder.leaveDateText.setText("Leave Date: " + request.getDateOfLeave());
            holder.reasonText.setText("Reason: " + request.getReason());
            holder.nameText.setText("Name: " + request.getName());
            holder.rollText.setText("Roll: " + request.getRoll());
            holder.branchText.setText("Branch: " + request.getBranch());

            holder.acceptButton.setOnClickListener(v -> updateRequestStatus(request, "accepted"));
            holder.rejectButton.setOnClickListener(v -> updateRequestStatus(request, "rejected"));
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView dateText, leaveDateText, reasonText, nameText, rollText, branchText;
            android.widget.Button acceptButton, rejectButton;


            public ViewHolder(android.view.View itemView) {
                super(itemView);
                dateText = itemView.findViewById(R.id.dateTextView);
                leaveDateText = itemView.findViewById(R.id.dateOfLeaveTextView);
                reasonText = itemView.findViewById(R.id.reasonTextView);
                nameText = itemView.findViewById(R.id.nameTextView);
                rollText = itemView.findViewById(R.id.rollNumberTextView);
                branchText = itemView.findViewById(R.id.branchTextView);
                acceptButton = itemView.findViewById(R.id.acceptButton);
                rejectButton = itemView.findViewById(R.id.rejectButton);
            }
        }
    }

    // DayPassRequest Model Class
    public static class DayPassRequest {
        private String id, date, dateOfLeave, reason, status, studentId, name, roll, branch;

        public DayPassRequest() {}

        public DayPassRequest(String date, String dateOfLeave, String reason, String status, String studentId) {
            this.date = date;
            this.dateOfLeave = dateOfLeave;
            this.reason = reason;
            this.status = status;
            this.studentId = studentId;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDate() { return date; }
        public String getDateOfLeave() { return dateOfLeave; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getStudentId() { return studentId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRoll() { return roll; }
        public void setRoll(String roll) { this.roll = roll; }
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
    }
}
