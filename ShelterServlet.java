package com.safehaven.controllers;

import com.safehaven.config.DBConfig;
import com.safehaven.model.Shelter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ShelterServlet - Manages shelter listings for SafeHaven.
 *
 * GET  → Loads shelter data and displays the shelter management page
 * POST → Handles actions: add, update, or delete a shelter
 *
 * Access Control:
 *   Admin  → Can view all shelters, add, update, and delete
 *   Member → Can only view and update their own shelter's bed availability
 */
@WebServlet("/ShelterServlet")
public class ShelterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private String providerName;
 // + getter/setter
 public String getProviderName() { return providerName; }
 public void setProviderName(String providerName) { this.providerName = providerName; }

    /**
     * doGet - Loads shelters from the database and forwards to the shelter JSP.
     * Admins see all shelters; Members only see their own shelter(s).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ---- Session / Auth check ----
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userID") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        int    userID = (int)    session.getAttribute("userID");
        String role   = (String) session.getAttribute("role");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConfig.getConnection();
            List<Shelter> shelterList = new ArrayList<>();

            if ("Admin".equalsIgnoreCase(role)) {
                // Admin sees all shelters
                String sql = "SELECT s.shelterID, s.providerID, u.fullName AS providerName, " +
                             "s.shelterName, s.address, s.totalCapacity, s.currentAvailableBeds " +
                             "FROM shelters s " +
                             "JOIN users u ON s.providerID = u.userID " +
                             "ORDER BY s.shelterName ASC";
                stmt = conn.prepareStatement(sql);
            } else {
                // Member sees only their own shelters
                String sql = "SELECT s.shelterID, s.providerID, u.fullName AS providerName, " +
                             "s.shelterName, s.address, s.totalCapacity, s.currentAvailableBeds " +
                             "FROM shelters s " +
                             "JOIN users u ON s.providerID = u.userID " +
                             "WHERE s.providerID = ? " +
                             "ORDER BY s.shelterName ASC";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userID);
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                Shelter shelter = new Shelter();
                shelter.setShelterID(rs.getInt("shelterID"));
                shelter.setProviderID(rs.getInt("providerID"));
                shelter.setProviderName(rs.getString("providerName"));
                shelter.setShelterName(rs.getString("shelterName"));
                shelter.setAddress(rs.getString("address"));
                shelter.setTotalCapacity(rs.getInt("totalCapacity"));
                shelter.setCurrentAvailableBeds(rs.getInt("currentAvailableBeds"));
                shelterList.add(shelter);
            }

            // Pass data to JSP
            request.setAttribute("shelterList", shelterList);
            request.setAttribute("role", role);
            request.getRequestDispatcher("/WEB-INF/pages/shelter.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Something went wrong while loading shelters.");
            request.getRequestDispatcher("/WEB-INF/pages/shelter.jsp").forward(request, response);

        } finally {
            try { if (rs   != null) rs.close();   } catch (SQLException ignored) {}
            try { if (stmt != null) stmt.close();  } catch (SQLException ignored) {}
            try { if (conn != null) conn.close();  } catch (SQLException ignored) {}
        }
    }

    /**
     * doPost - Handles form actions: "add", "update", or "delete".
     * The action is determined by a hidden form field named "action".
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ---- Session / Auth check ----
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userID") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String role   = (String) session.getAttribute("role");
        int    userID = (int)    session.getAttribute("userID");
        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/ShelterServlet");
            return;
        }

        switch (action.trim()) {
            case "add":
                // Only Admins can add shelters
                if (!"Admin".equalsIgnoreCase(role)) {
                    response.sendRedirect(request.getContextPath() + "/ShelterServlet");
                    return;
                }
                handleAddShelter(request, response);
                break;

            case "update":
                handleUpdateShelter(request, response, role, userID);
                break;

            case "delete":
                // Only Admins can delete shelters
                if (!"Admin".equalsIgnoreCase(role)) {
                    response.sendRedirect(request.getContextPath() + "/ShelterServlet");
                    return;
                }
                handleDeleteShelter(request, response);
                break;

            default:
                response.sendRedirect(request.getContextPath() + "/ShelterServlet");
        }
    }

    // -----------------------------------------------------------------------
    // Action Handlers
    // -----------------------------------------------------------------------

    /**
     * Adds a new shelter record (Admin only).
     * Required parameters: providerID, shelterName, address, totalCapacity, currentAvailableBeds
     */
    private void handleAddShelter(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String shelterName          = request.getParameter("shelterName");
        String address              = request.getParameter("address");
        String providerIDParam      = request.getParameter("providerID");
        String totalCapacityParam   = request.getParameter("totalCapacity");
        String availableBedsParam   = request.getParameter("currentAvailableBeds");

        // Validate inputs
        if (isNullOrEmpty(shelterName) || isNullOrEmpty(address) ||
            isNullOrEmpty(providerIDParam) || isNullOrEmpty(totalCapacityParam) ||
            isNullOrEmpty(availableBedsParam)) {
            request.setAttribute("errorMessage", "All fields are required to add a shelter.");
            doGet(request, response);
            return;
        }

        try {
            int providerID    = Integer.parseInt(providerIDParam.trim());
            int totalCap      = Integer.parseInt(totalCapacityParam.trim());
            int availableBeds = Integer.parseInt(availableBedsParam.trim());

            if (totalCap < 0 || availableBeds < 0) {
                request.setAttribute("errorMessage", "Capacity values cannot be negative.");
                doGet(request, response);
                return;
            }

            if (availableBeds > totalCap) {
                request.setAttribute("errorMessage",
                    "Available beds cannot exceed total capacity.");
                doGet(request, response);
                return;
            }

            Connection conn = null;
            try {
                conn = DBConfig.getConnection();
                String sql = "INSERT INTO shelters (providerID, shelterName, address, " +
                             "totalCapacity, currentAvailableBeds) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, providerID);
                    stmt.setString(2, shelterName.trim());
                    stmt.setString(3, address.trim());
                    stmt.setInt(4, totalCap);
                    stmt.setInt(5, availableBeds);
                    stmt.executeUpdate();
                }
                response.sendRedirect(request.getContextPath() +
                    "/ShelterServlet?success=Shelter+added+successfully.");

            } finally {
                if (conn != null) conn.close();
            }

        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage",
                "Capacity values must be valid numbers.");
            doGet(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Something went wrong. Please try again.");
            doGet(request, response);
        }
    }

    /**
     * Updates an existing shelter record.
     * Admins can update all fields; Members can only update their own shelter's
     * currentAvailableBeds.
     */
    private void handleUpdateShelter(HttpServletRequest request, HttpServletResponse response,
                                     String role, int loggedInUserID)
            throws ServletException, IOException {

        String shelterIDParam     = request.getParameter("shelterID");
        String availableBedsParam = request.getParameter("currentAvailableBeds");

        if (isNullOrEmpty(shelterIDParam) || isNullOrEmpty(availableBedsParam)) {
            request.setAttribute("errorMessage", "Shelter ID and available beds are required.");
            doGet(request, response);
            return;
        }

        try {
            int shelterID     = Integer.parseInt(shelterIDParam.trim());
            int availableBeds = Integer.parseInt(availableBedsParam.trim());

            if (availableBeds < 0) {
                request.setAttribute("errorMessage", "Available beds cannot be negative.");
                doGet(request, response);
                return;
            }

            Connection conn = null;
            try {
                conn = DBConfig.getConnection();

                if ("Admin".equalsIgnoreCase(role)) {
                    // Admin can update all fields
                    String shelterName        = request.getParameter("shelterName");
                    String address            = request.getParameter("address");
                    String totalCapacityParam = request.getParameter("totalCapacity");

                    if (isNullOrEmpty(shelterName) || isNullOrEmpty(address) ||
                        isNullOrEmpty(totalCapacityParam)) {
                        request.setAttribute("errorMessage", "All fields are required.");
                        doGet(request, response);
                        return;
                    }

                    int totalCap = Integer.parseInt(totalCapacityParam.trim());

                    if (availableBeds > totalCap) {
                        request.setAttribute("errorMessage",
                            "Available beds cannot exceed total capacity.");
                        doGet(request, response);
                        return;
                    }

                    String sql = "UPDATE shelters SET shelterName = ?, address = ?, " +
                                 "totalCapacity = ?, currentAvailableBeds = ? " +
                                 "WHERE shelterID = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, shelterName.trim());
                        stmt.setString(2, address.trim());
                        stmt.setInt(3, totalCap);
                        stmt.setInt(4, availableBeds);
                        stmt.setInt(5, shelterID);
                        stmt.executeUpdate();
                    }

                } else {
                    // Member can only update currentAvailableBeds for their own shelter
                    String sql = "UPDATE shelters SET currentAvailableBeds = ? " +
                                 "WHERE shelterID = ? AND providerID = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, availableBeds);
                        stmt.setInt(2, shelterID);
                        stmt.setInt(3, loggedInUserID); // prevents editing another member's shelter
                        stmt.executeUpdate();
                    }
                }

                response.sendRedirect(request.getContextPath() +
                    "/ShelterServlet?success=Shelter+updated+successfully.");

            } finally {
                if (conn != null) conn.close();
            }

        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Shelter ID and capacity must be valid numbers.");
            doGet(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Something went wrong. Please try again.");
            doGet(request, response);
        }
    }

    /**
     * Deletes a shelter record by shelterID (Admin only).
     */
    private void handleDeleteShelter(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String shelterIDParam = request.getParameter("shelterID");

        if (isNullOrEmpty(shelterIDParam)) {
            request.setAttribute("errorMessage", "Shelter ID is required to delete a shelter.");
            doGet(request, response);
            return;
        }

        try {
            int shelterID = Integer.parseInt(shelterIDParam.trim());

            Connection conn = null;
            try {
                conn = DBConfig.getConnection();
                String sql = "DELETE FROM shelters WHERE shelterID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, shelterID);
                    stmt.executeUpdate();
                }
                response.sendRedirect(request.getContextPath() +
                    "/ShelterServlet?success=Shelter+deleted+successfully.");

            } finally {
                if (conn != null) conn.close();
            }

        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Invalid shelter ID.");
            doGet(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Something went wrong. Please try again.");
            doGet(request, response);
        }
    }

    // -----------------------------------------------------------------------
    // Utility
    // -----------------------------------------------------------------------

    /** Null/empty string check helper. */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
