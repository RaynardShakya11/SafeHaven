package com.safehaven.model;

/**
 * Request.java
 * Represents an assistance request made by a displaced individual
 * to a specific shelter in the SafeHaven system.
 */
public class Request {

    private int requestID;
    private int shelterID;
    private String shelterName;        // joined from shelters table for display purposes
    private String requesterName;
    private String requesterPhone;
    private int numberOfPeople;
    private String status;             // "Pending", "Fulfilled", or "Rejected"
    private String notes;
    private String requestedAt;
    private String updatedAt;

    // -------------------------
    // Constructors
    // -------------------------

    public Request() {}

    // Used when submitting a new request
    public Request(int shelterID, String requesterName, String requesterPhone,
                   int numberOfPeople, String notes) {
        this.shelterID = shelterID;
        this.requesterName = requesterName;
        this.requesterPhone = requesterPhone;
        this.numberOfPeople = numberOfPeople;
        this.notes = notes;
        this.status = "Pending";
    }

    // Used when loading a full request record from the database
    public Request(int requestID, int shelterID, String requesterName, String requesterPhone,
                   int numberOfPeople, String status, String notes,
                   String requestedAt, String updatedAt) {
        this.requestID = requestID;
        this.shelterID = shelterID;
        this.requesterName = requesterName;
        this.requesterPhone = requesterPhone;
        this.numberOfPeople = numberOfPeople;
        this.status = status;
        this.notes = notes;
        this.requestedAt = requestedAt;
        this.updatedAt = updatedAt;
    }

    // -------------------------
    // Helper methods
    // -------------------------

    /** Returns true if the request is still awaiting a decision. */
    public boolean isPending() {
        return "Pending".equalsIgnoreCase(this.status);
    }

    /** Returns true if the request has been fulfilled. */
    public boolean isFulfilled() {
        return "Fulfilled".equalsIgnoreCase(this.status);
    }

    /** Returns true if the request was rejected. */
    public boolean isRejected() {
        return "Rejected".equalsIgnoreCase(this.status);
    }

    // -------------------------
    // Getters & Setters
    // -------------------------

    public int getRequestID() { return requestID; }
    public void setRequestID(int requestID) { this.requestID = requestID; }

    public int getShelterID() { return shelterID; }
    public void setShelterID(int shelterID) { this.shelterID = shelterID; }

    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getRequesterPhone() { return requesterPhone; }
    public void setRequesterPhone(String requesterPhone) { this.requesterPhone = requesterPhone; }

    public int getNumberOfPeople() { return numberOfPeople; }
    public void setNumberOfPeople(int numberOfPeople) { this.numberOfPeople = numberOfPeople; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRequestedAt() { return requestedAt; }
    public void setRequestedAt(String requestedAt) { this.requestedAt = requestedAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Request{requestID=" + requestID + ", requesterName='" + requesterName +
               "', status='" + status + "', numberOfPeople=" + numberOfPeople + "}";
    }
}
