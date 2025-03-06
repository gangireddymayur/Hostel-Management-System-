package com.example.loginpage;

public class Student {
    private String name;
    private String rollNumber;
    private String branch;
    private String gatepass;
    private String password;
    private Boolean request;  // Changed to Boolean as per your structure
    private Boolean request2; // Changed to Boolean as per your structure
    public String roomNumber;
    public String year;
    public String phone;

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String parentPhone;

    // Empty constructor for Firebase
    public Student() {

    }
    public Student(String name, String phone, String branch, String rollNumber) {
        this.name = name;
        this.rollNumber = rollNumber;
        this.branch = branch;
        this.phone = phone;
    }
    public Student(String name, String rollNumber, String roomNumber, String year, String branch, String phone, String parentPhone,String password) {
        this.name = name;
        this.rollNumber = rollNumber;
        this.roomNumber = roomNumber;
        this.year = year;
        this.branch = branch;
        this.phone = phone;
        this.parentPhone = parentPhone;
        this.password=password;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getGatepass() {
        return gatepass;
    }

    public void setGatepass(String gatepass) {
        this.gatepass = gatepass;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRequest() {
        return request;
    }

    public void setRequest(Boolean request) {
        this.request = request;
    }

    public Boolean getRequest2() {
        return request2;
    }

    public void setRequest2(Boolean request2) {
        this.request2 = request2;
    }
}
