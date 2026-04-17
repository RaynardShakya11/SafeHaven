package com.safehaven.model;

/**
 * User.java
 * Represents a user in the SafeHaven system.
 * Role can be either 'Admin' (NGO Manager) or 'Member' (Shelter Provider).
 */
public class User {

    private int userID;
    private String fullName;
    private String email;
    private String password;   // BCrypt hashed — never store plain text
    private String phone;
    private String role;       // "Admin" or "Member"
    private boolean isLocked;
    private int failedAttempts;
    private String createdAt;

    // -------------------------
    // Constructors
    // -------------------------

    public User() {}

    // Used when registering a new user
    public User(String fullName, String email, String password, String phone, String role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }

    // Used when loading a full user record from the database
    public User(int userID, String fullName, String email, String password,
                String phone, String role, boolean isLocked, int failedAttempts, String createdAt) {
        this.userID = userID;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.isLocked = isLocked;
        this.failedAttempts = failedAttempts;
        this.createdAt = createdAt;
    }

    // -------------------------
    // Helper methods
    // -------------------------

    /** Returns true if this user has the Admin role. */
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(this.role);
    }

    /** Returns true if this user has the Member role. */
    public boolean isMember() {
        return "Member".equalsIgnoreCase(this.role);
    }

    // -------------------------
    // Getters & Setters
    // -------------------------

    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean isLocked) { this.isLocked = isLocked; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{userID=" + userID + ", fullName='" + fullName + "', role='" + role + "'}";
    }
}
