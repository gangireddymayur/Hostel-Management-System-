package com.example.loginpage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class AdminAddMult extends AppCompatActivity {

    private Button uploadButton;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_mult_add);
        EdgeToEdge.enable(this);
        uploadButton = findViewById(R.id.uploadButton);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("students");

        // Register file picker launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        processExcelFile(fileUri);
                    }
                });

        uploadButton.setOnClickListener(v -> openFilePicker());
    }

    // Open the file picker to select an Excel file
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(intent);
    }

    // Process the selected Excel file using Apache POI library
    private void processExcelFile(Uri fileUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
            // Open the Excel file using Apache POI's XSSFWorkbook
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            ArrayList<HashMap<String, String>> students = new ArrayList<>();

            // Loop through rows (starting from 1 to skip the header row)
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                HashMap<String, String> student = new HashMap<>();

                // Get data from each cell and handle both string and numeric values
                student.put("branch", getCellValue(row.getCell(0)));
                student.put("name", getCellValue(row.getCell(1)));
                student.put("password", getCellValue(row.getCell(2)));
                student.put("rollNumber", getCellValue(row.getCell(3)));  // Ensure rollNumber is a string
                student.put("roomNumber", getCellValue(row.getCell(4)));
                student.put("year", getCellValue(row.getCell(5)));

                students.add(student);
            }

            // Send data to Firebase
            sendDataToServer(students);
            Toast.makeText(this, "File processed successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Helper method to get the value from a cell (handles both numeric and string types)
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Check if the numeric value is an integer or float
                if (DateUtil.isCellDateFormatted(cell)) {
                    // If it's a date, return it as a string (optional, depends on your use case)
                    return cell.getDateCellValue().toString();
                } else {
                    // If it's a whole number (e.g., 1234.0), convert it to an integer string
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (int) numericValue) {
                        return String.valueOf((int) numericValue);  // Convert to integer string
                    } else {
                        return String.valueOf(numericValue);  // Leave as float if not whole number
                    }
                }
            default:
                return "";
        }
    }


    // Send student data to Firebase
    private void sendDataToServer(ArrayList<HashMap<String, String>> students) {
        // Iterate over the student data and save to Firebase
        for (HashMap<String, String> student : students) {
            String rollNumber = student.get("rollNumber"); // Unique key for student

            // Ensure rollNumber is treated as a string and sanitized if necessary
            rollNumber = sanitizeFirebaseKey(rollNumber);

            Log.d("StudentData", "Uploading student: " + student.get("name"));

            // Create a map to store student data in the correct structure
            HashMap<String, Object> studentData = new HashMap<>();
            studentData.put("branch", student.get("branch"));
            studentData.put("name", student.get("name"));
            studentData.put("password", student.get("password"));
            studentData.put("rollNumber", student.get("rollNumber"));
            studentData.put("roomNumber", student.get("roomNumber"));
            studentData.put("year", student.get("year"));

            // Upload the data to Firebase under the student's roll number
            databaseReference.child(rollNumber).setValue(studentData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminAddMult.this, "Student data sent to Firebase.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminAddMult.this, "Failed to send data to Firebase.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Method to sanitize the roll number to ensure it's a valid Firebase key
    private String sanitizeFirebaseKey(String key) {
        // Firebase keys should not contain '.', '#', '$', '[', or ']', and should be strings
        return key.replaceAll("[^a-zA-Z0-9]", "_");  // Replace invalid characters with underscores
    }

}
