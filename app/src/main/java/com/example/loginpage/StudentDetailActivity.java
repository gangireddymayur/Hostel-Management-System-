package com.example.loginpage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class StudentDetailActivity extends AppCompatActivity {
    TextView name,roll,branch;

    private RecyclerView requestsRecyclerView;
    private List<Request> requestsList;
    private DatabaseReference dayPassRef, weekMonthPassRef;
    private RequestAdapter requestAdapter;
    private String studentId;
    private EditText searchEditText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);
        EdgeToEdge.enable(this);

        // Get student ID (we're assuming this is passed as "studentId")
        studentId = getIntent().getStringExtra("studentId");

        // Initialize the RecyclerView and the adapter
        requestsRecyclerView = findViewById(R.id.requestsListViewStudent);
        requestsList = new ArrayList<>();
        requestAdapter = new RequestAdapter(this, requestsList);

        name = findViewById(R.id.studentNameTextView1);
        roll = findViewById(R.id.studentRollNumberTextView);
        branch = findViewById(R.id.studentBranchTextView);

        // Set LayoutManager for RecyclerView
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set the adapter for the RecyclerView
        requestsRecyclerView.setAdapter(requestAdapter);

        // Initialize Firebase references for both daypass and weekMonthPass
        dayPassRef = FirebaseDatabase.getInstance().getReference("daypass");
        weekMonthPassRef = FirebaseDatabase.getInstance().getReference("weekMonthPass");

        // Get reference to the search EditText
        searchEditText = findViewById(R.id.searchEditText);

        // Set a TextWatcher to filter requests in real-time
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterRequests(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Fetch student data and requests
        fetchStudentData();
        fetchRequests(dayPassRef);  // Real-time data from daypass
        fetchRequests(weekMonthPassRef);  // Real-time data from week/month pass
    }

    private void fetchStudentData() {
        // Get reference to the student node in the Firebase database
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("students").child(studentId);

        // Add a listener to fetch student data
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch and set the data from Firebase
                    String studentName = snapshot.child("name").getValue(String.class);
                    String studentRoll = snapshot.child("rollNumber").getValue(String.class);
                    String studentBranch = snapshot.child("branch").getValue(String.class);

                    // Update UI elements
                    name.setText(studentName != null ? studentName : "Name not available");
                    roll.setText(studentRoll != null ? "Roll No: " + studentRoll : "Roll number not available");
                    branch.setText(studentBranch != null ? "Branch: " + studentBranch : "Branch not available");
                } else {
                    // Handle case where student data is not found
                    Toast.makeText(StudentDetailActivity.this, "Student data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle database errors
                Toast.makeText(StudentDetailActivity.this, "Failed to fetch student data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Fetch requests from Firebase based on the search term
    private void fetchRequests(DatabaseReference ref) {
        // Listen for real-time changes to the requests
        ref.orderByChild("studentId").equalTo(studentId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Request request = snapshot.getValue(Request.class);
                if (request != null) {
                    requestsList.add(request);  // Add new request to the list
                    requestAdapter.notifyItemInserted(requestsList.size() - 1);  // Notify adapter about new item
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                Request updatedRequest = snapshot.getValue(Request.class);
                if (updatedRequest != null) {
                    // Update the corresponding request in the list
                    for (int i = 0; i < requestsList.size(); i++) {
                        if (requestsList.get(i).getId().equals(updatedRequest.getId())) {
                            requestsList.set(i, updatedRequest);
                            requestAdapter.notifyItemChanged(i);  // Notify adapter about the updated item
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                Request removedRequest = snapshot.getValue(Request.class);
                if (removedRequest != null) {
                    // Remove the request from the list
                    for (int i = 0; i < requestsList.size(); i++) {
                        if (requestsList.get(i).getId().equals(removedRequest.getId())) {
                            requestsList.remove(i);
                            requestAdapter.notifyItemRemoved(i);  // Notify adapter about the removed item
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                // This method can be used if you want to handle reordering requests
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(StudentDetailActivity.this, "Failed to load requests", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Filter requests based on the input in the search field
    private void filterRequests(String query) {
        List<Request> filteredList = new ArrayList<>();

        // Loop through requests and filter based on the query
        for (Request request : requestsList) {
            String requestDate = request.getDate();
            if (requestDate != null && requestDate.contains(query)) {
                filteredList.add(request);
            }
        }

        // Update the adapter with filtered list
        requestAdapter.updateRequests(filteredList);
    }

    // Request Model Class
    public static class Request {
        private String id;
        private String reason;
        private String status;
        private String date;
        private String dateOfLeave;
        private String dateofReturn;
        private String studentId;

        public Request() {
            // Empty constructor needed for Firebase
        }

        public Request(String id, String reason, String status, String date, String dateOfLeave, String dateofReturn, String studentId) {
            this.id = id;
            this.reason = reason;
            this.status = status;
            this.date = date;
            this.dateOfLeave = dateOfLeave;
            this.dateofReturn = dateofReturn;
            this.studentId = studentId;
        }

        // Getters and setters for all fields
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDateOfLeave() {
            return dateOfLeave;
        }

        public void setDateOfLeave(String dateOfLeave) {
            this.dateOfLeave = dateOfLeave;
        }

        public String getDateofReturn() {
            return dateofReturn;
        }

        public void setDateofReturn(String dateofReturn) {
            this.dateofReturn = dateofReturn;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }
    }

    // Request Adapter Class for RecyclerView
    public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

        private final StudentDetailActivity context;
        private List<Request> requestsList;

        public RequestAdapter(StudentDetailActivity context, List<Request> requestsList) {
            this.context = context;
            this.requestsList = requestsList;
        }

        @Override
        public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Inflate the CardView layout
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_request, parent, false);
            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RequestViewHolder holder, int position) {
            Request request = requestsList.get(position);
            holder.dateTextView.setText("Date: " + request.getDate());
            holder.reasonTextView.setText("Reason: " + request.getReason());
            holder.statusTextView.setText("Status: " + request.getStatus());

            // Show dateOfLeave and dateOfReturn if it's a week/month pass request
            if (request.getDateOfLeave() != null) {
                holder.leaveTextView.setVisibility(View.VISIBLE);
                holder.leaveTextView.setText("Leave Date: " + request.getDateOfLeave());
            } else {
                holder.leaveTextView.setVisibility(View.GONE);
            }

            if (request.getDateofReturn() != null) {
                holder.returnTextView.setVisibility(View.VISIBLE);
                holder.returnTextView.setText("Return Date: " + request.getDateofReturn());
            } else {
                holder.returnTextView.setVisibility(View.GONE);
            }

            // Set different color for status
            if ("pending".equalsIgnoreCase(request.getStatus())) {
                holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            } else if ("accepted".equalsIgnoreCase(request.getStatus())) {
                holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            }
        }

        @Override
        public int getItemCount() {
            return requestsList.size();
        }

        // Update requests in the adapter
        public void updateRequests(List<Request> requests) {
            this.requestsList = requests;
            notifyDataSetChanged();
        }

        public class RequestViewHolder extends RecyclerView.ViewHolder {

            TextView dateTextView, reasonTextView, statusTextView, leaveTextView, returnTextView;

            public RequestViewHolder(View itemView) {
                super(itemView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                reasonTextView = itemView.findViewById(R.id.reasonTextView);
                statusTextView = itemView.findViewById(R.id.statusTextView);
                leaveTextView = itemView.findViewById(R.id.leaveTextView);
                returnTextView = itemView.findViewById(R.id.returnTextView);
            }
        }
    }
}
