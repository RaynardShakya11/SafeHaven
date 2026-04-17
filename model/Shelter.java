package com.safehaven.model;

/**
 * Shelter.java
 * Represents a shelter facility managed by a Member (Shelter Provider).
 * Linked to the users table via providerID.
 */
public class Shelter {

    private int shelterID;
    private int providerID;
    private String providerName;       // joined from users table for display purposes
    private String shelterName;
    private String address;
    private String city;
    private int totalCapacity;
    private int currentAvailableBeds;
    private boolean isActive;
    private String createdAt;

    // -------------------------
    // Constructors
    // -------------------------

    public Shelter() {}

    // Used when a Member registers a new shelter
    public Shelter(int providerID, String shelterName, String address,
                   String city, int totalCapacity, int currentAvailableBeds) {
        this.providerID = providerID;
        this.shelterName = shelterName;
        this.address = address;
        this.city = city;
        this.totalCapacity = totalCapacity;
        this.currentAvailableBeds = currentAvailableBeds;
        this.isActive = true;
    }

    // Used when loading a full shelter record from the database
    public Shelter(int shelterID, int providerID, String shelterName, String address,
                   String city, int totalCapacity, int currentAvailableBeds,
                   boolean isActive, String createdAt) {
        this.shelterID = shelterID;
        this.providerID = providerID;
        this.shelterName = shelterName;
        this.address = address;
        this.city = city;
        this.totalCapacity = totalCapacity;
        this.currentAvailableBeds = currentAvailableBeds;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // -------------------------
    // Helper methods
    // -------------------------

    /** Returns true if the shelter has at least one available bed. */
    public boolean hasAvailability() {
        return currentAvailableBeds > 0;
    }

    /** Returns the occupancy as a percentage (0–100). */
    public int getOccupancyPercent() {
        if (totalCapacity == 0) return 0;
        return (int) (((totalCapacity - currentAvailableBeds) / (double) totalCapacity) * 100);
    }

    // -------------------------
    // Getters & Setters
    // -------------------------

    public int getShelterID() { return shelterID; }
    public void setShelterID(int shelterID) { this.shelterID = shelterID; }

    public int getProviderID() { return providerID; }
    public void setProviderID(int providerID) { this.providerID = providerID; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    public int getCurrentAvailableBeds() { return currentAvailableBeds; }
    public void setCurrentAvailableBeds(int currentAvailableBeds) { this.currentAvailableBeds = currentAvailableBeds; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Shelter{shelterID=" + shelterID + ", shelterName='" + shelterName +
               "', city='" + city + "', availableBeds=" + currentAvailableBeds + "}";
    }
}
