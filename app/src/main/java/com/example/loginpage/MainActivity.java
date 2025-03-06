package com.example.loginpage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText editTextRollNumber, editTextPassword;
    private Button buttonLogin;
    private DatabaseReference databaseReference;
    public static final String SHARED_PREFS = "sharedPrefs";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);
        editTextRollNumber = findViewById(R.id.editTextRollNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.loginButton);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("students");
        checkLoginStatus();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rollNumber = editTextRollNumber.getText().toString();
                String password = editTextPassword.getText().toString();
                if (!rollNumber.isEmpty() && !password.isEmpty()) {
                    // Check admin login (hardcoded for now)
                    if (rollNumber.equals("1") && password.equals("2")) {
                        saveCredentials("1", "2");
                        Intent it = new Intent(MainActivity.this, AdminScreen.class);
                        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                        startActivity(it);
                        finish(); // Optional
                    } else {
                        loginStudent(rollNumber, password);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginStudent(String rollNumber, String password) {
        // Disable login button to avoid multiple clicks during query
        buttonLogin.setEnabled(false);

        // Query Firebase to check if the roll number exists
        databaseReference.child(rollNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Re-enable the button after the Firebase query
                buttonLogin.setEnabled(true);

                if (snapshot.exists()) {
                    Student student = snapshot.getValue(Student.class);
                    if (student != null && student.getPassword().equals(password)) {
                        saveCredentials(rollNumber, password);

                        // Pass roll number to next activity
                        Intent it = new Intent(MainActivity.this, StudentScreen.class);
                        it.putExtra("key", rollNumber);
                        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                        startActivity(it);
                        finish(); // Optional
                        Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Roll Number not found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Re-enable the button in case of an error
                buttonLogin.setEnabled(true);
                Toast.makeText(MainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString(USERNAME_KEY, null);
        String savedPassword = sharedPreferences.getString(PASSWORD_KEY, null);

        // Check if both username and password are not null
        if (savedUsername != null && savedPassword != null) {
            // Check if the saved credentials are the admin credentials
            if (savedUsername.equals("1") && savedPassword.equals("2")) {
                Intent intent = new Intent(MainActivity.this, AdminScreen.class);
                startActivity(intent);
                finish(); // Close current activity so user can't go back to the login screen
            } else {
                // Proceed with the student screen
                Intent intent = new Intent(MainActivity.this, StudentScreen.class);
                intent.putExtra("key", savedUsername);  // Pass saved username to StudentScreen
                startActivity(intent);
                finish();
            }
        }
    }

    private void saveCredentials(String username, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USERNAME_KEY, username);
        editor.putString(PASSWORD_KEY, password);
        editor.apply();
    }
}
