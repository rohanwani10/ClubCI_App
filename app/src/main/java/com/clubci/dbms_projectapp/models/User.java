package com.clubci.dbms_projectapp.models;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String branch;
    private String phone;
    private String address;
    private String year;
    private String role;

    public User() {
    }

    public User(String username, String email, String fullName) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Convert User to JSON for API requests
     */
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (username != null)
            json.put("username", username);
        if (email != null)
            json.put("email", email);
        if (password != null)
            json.put("password", password);
        if (fullName != null)
            json.put("fullName", fullName);
        if (branch != null)
            json.put("branch", branch);
        if (phone != null)
            json.put("phone", phone);
        if (address != null)
            json.put("address", address);
        if (year != null)
            json.put("year", year);
        if (role != null)
            json.put("role", role);
        return json;
    }

    /**
     * Create User from JSON response
     */
    public static User fromJson(JSONObject json) throws JSONException {
        User user = new User();
        if (json.has("username"))
            user.setUsername(json.getString("username"));
        if (json.has("email"))
            user.setEmail(json.getString("email"));
        if (json.has("fullName"))
            user.setFullName(json.getString("fullName"));
        if (json.has("branch"))
            user.setBranch(json.getString("branch"));
        if (json.has("phone"))
            user.setPhone(json.getString("phone"));
        if (json.has("address"))
            user.setAddress(json.getString("address"));
        if (json.has("year"))
            user.setYear(json.getString("year"));
        if (json.has("role"))
            user.setRole(json.getString("role"));
        return user;
    }
}
