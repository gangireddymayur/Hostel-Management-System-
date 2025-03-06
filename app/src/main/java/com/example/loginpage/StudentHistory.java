package com.example.loginpage;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StudentHistory extends AppCompatActivity {

    RecyclerView historyRecyclerView;
    SearchView searchView;
    Button dayPassesButton, weekMonthRequestsButton;
    ImageButton refreshButton;
    List<Request> requestList = new ArrayList<>();
    List<Request> filteredList = new ArrayList<>();
    RequestAdapter adapter;
    String rollNumber;
    String historyType = "dayPasses";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dayPassRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_history);
        EdgeToEdge.enable(this);

        // Retrieve the roll number from intent
        Intent intent = getIntent();
        rollNumber = intent.getStringExtra("rollNumber");

        // Set Firebase reference to the "daypass" node
        dayPassRef = database.getReference("daypass");

        // Initialize UI components
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        searchView = findViewById(R.id.searchView);
        dayPassesButton = findViewById(R.id.dayPassesButton);
        weekMonthRequestsButton = findViewById(R.id.weekMonthRequestsButton);
        refreshButton = findViewById(R.id.refreshButton);

        // Set up RecyclerView adapter and layout manager
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestAdapter(filteredList); // Use filteredList for display
        historyRecyclerView.setAdapter(adapter);

        // Fetch and display Day Passes by default
        fetchDayPasses();

        // Handle button clicks
        dayPassesButton.setOnClickListener(v -> {
            if (!historyType.equals("dayPasses")) {
                historyType = "dayPasses";
                filteredList.clear(); // Clear the filtered list before switching data
                fetchDayPasses(); // Fetch Day Pass data
            }
        });

        weekMonthRequestsButton.setOnClickListener(v -> {
            if (!historyType.equals("weekMonthRequests")) {
                historyType = "weekMonthRequests";
                filteredList.clear(); // Clear the filtered list before switching data
                fetchWeekMonthRequests(); // Fetch Week/Month data
            }
        });

        // Refresh button click listener
        refreshButton.setOnClickListener(v -> {
            Toast.makeText(StudentHistory.this, "Refreshing data...", Toast.LENGTH_SHORT).show();
            if (historyType.equals("dayPasses")) {
                fetchDayPasses();
            } else if (historyType.equals("weekMonthRequests")) {
                fetchWeekMonthRequests();
            }
        });

        // Add SearchView functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterRequests(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRequests(newText);
                return true;
            }
        });
    }

    // Method to fetch Day Passes
    private void fetchDayPasses() {
        requestList.clear();
        filteredList.clear(); // Reset the filtered list

        dayPassRef.orderByChild("studentId").equalTo(rollNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            TreeMap<String, Request> sortedRequests = new TreeMap<>((date1, date2) -> compareDates(date2, date1));

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String date = snapshot.child("date").getValue(String.class);
                                String dateOfLeave = snapshot.child("dateOfLeave").getValue(String.class);
                                String reason = snapshot.child("reason").getValue(String.class);
                                String status = snapshot.child("status").getValue(String.class);

                                if (date != null && dateOfLeave != null && reason != null && status != null) {
                                    Request request = new Request(date, reason, status, dateOfLeave, null);
                                    sortedRequests.put(date, request);
                                } else {
                                    Log.e("Debug", "Missing data for snapshot: " + snapshot.getKey());
                                }
                            }

                            requestList.addAll(sortedRequests.values());
                            filteredList.addAll(requestList); // Initialize filteredList with all data
                        } else {
                            requestList.add(new Request("No data", "", "", "", ""));
                            filteredList.addAll(requestList);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(StudentHistory.this, "Failed to fetch day passes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to fetch Week/Month Requests
    private void fetchWeekMonthRequests() {
        requestList.clear();
        filteredList.clear(); // Reset the filtered list

        // Reference to weekMonthpass node in Firebase
        DatabaseReference weekMonthRef = database.getReference("weekMonthPass");

        // Query weekMonthpass node filtered by studentId
        weekMonthRef.orderByChild("studentId").equalTo(rollNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            TreeMap<String, Request> sortedRequests = new TreeMap<>((date1, date2) -> compareDates(date2, date1));

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String date = snapshot.child("date").getValue(String.class);
                                String dateOfLeave = snapshot.child("dateOfLeave").getValue(String.class);
                                String dateOfReturn = snapshot.child("dateofReturn").getValue(String.class);
                                String reason = snapshot.child("reason").getValue(String.class);
                                String status = snapshot.child("status").getValue(String.class);

                                if (date != null && dateOfLeave != null && dateOfReturn != null && reason != null && status != null) {
                                    Request request = new Request(date, reason, status, dateOfLeave, dateOfReturn);
                                    sortedRequests.put(date, request);
                                } else {
                                    Log.e("Debug", "Missing data for snapshot: " + snapshot.getKey());
                                }
                            }

                            requestList.addAll(sortedRequests.values());
                            filteredList.addAll(requestList);
                        } else {
                            requestList.add(new Request("No data", "", "", "", ""));
                            filteredList.addAll(requestList);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(StudentHistory.this, "Failed to fetch week/month requests.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper method to compare dates
    private int compareDates(String date1, String date2) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            return dateFormat.parse(date1).compareTo(dateFormat.parse(date2));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Method to filter requests by reason/status
    private void filterRequests(String query) {
        filteredList.clear();

        for (Request request : requestList) {
            if (request.getReason().toLowerCase().contains(query.toLowerCase()) ||
                    request.getStatus().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(request);
            }
        }

        adapter.notifyDataSetChanged(); // Update the RecyclerView with filtered data
    }

    // Request class to represent each request
    public static class Request {
        private String date;
        private String reason;
        private String status;
        private String leaveDate;
        private String returnDate;

        public Request(String date, String reason, String status, String leaveDate, String returnDate) {
            this.date = date;
            this.reason = reason;
            this.status = status;
            this.leaveDate = leaveDate;
            this.returnDate = returnDate;
        }

        public String getDate() { return date; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }
        public String getLeaveDate() { return leaveDate; }
        public String getReturnDate() { return returnDate; }
    }

    // RequestAdapter class to bind data to RecyclerView
    public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

        private List<Request> requestList;

        public RequestAdapter(List<Request> requestList) {
            this.requestList = requestList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Request request = requestList.get(position);

            holder.dateTextView.setText("Date: " + request.getDate());
            holder.reasonTextView.setText("Reason: " + request.getReason());
            holder.statusTextView.setText("Status: " + request.getStatus());

            if (request.getLeaveDate() != null && !request.getLeaveDate().isEmpty()) {
                holder.leaveTextView.setText("Leave Date: " + request.getLeaveDate());
                holder.leaveTextView.setVisibility(View.VISIBLE);
            } else {
                holder.leaveTextView.setVisibility(View.GONE);
            }

            if (request.getReturnDate() != null && !request.getReturnDate().isEmpty()) {
                holder.returnTextView.setText("Return Date: " + request.getReturnDate());
                holder.returnTextView.setVisibility(View.VISIBLE);
            } else {
                holder.returnTextView.setVisibility(View.GONE);
            }

            // Color code the status
            String status = request.getStatus();
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "rejected":
                        holder.statusTextView.setTextColor(Color.RED);
                        break;
                    case "accepted":
                        holder.statusTextView.setTextColor(Color.GREEN);
                        break;
                    case "pending":
                        holder.statusTextView.setTextColor(Color.BLUE);
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return requestList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView dateTextView, reasonTextView, statusTextView, leaveTextView, returnTextView;

            public ViewHolder(View itemView) {
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
