package com.safehaven.controllers;

import com.safehaven.config.DBConfig;
import com.safehaven.model.Shelter;
import com.safehaven.model.Request;
import com.safehaven.model.User;

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

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User loggedUser = (User) session.getAttribute("loggedUser");

        try (Connection conn = DBConfig.getConnection()) {

            if (loggedUser.isAdmin()) {
                // Admin: load all shelters and all requests
                List<Shelter> shelters = getAllShelters(conn);
                List<Request> requests = getAllRequests(conn);

                // Summary stats
                int totalBeds     = shelters.stream().mapToInt(Shelter::getTotalCapacity).sum();
                int availableBeds = shelters.stream().mapToInt(Shelter::getCurrentAvailableBeds).sum();
                long pendingCount = requests.stream().filter(Request::isPending).count();

                request.setAttribute("shelters", shelters);
                request.setAttribute("requests", requests);
                request.setAttribute("totalBeds", totalBeds);
                request.setAttribute("availableBeds", availableBeds);
                request.setAttribute("pendingCount", pendingCount);
                request.getRequestDispatcher("/WEB-INF/pages/adminDashboard.jsp").forward(request, response);

            } else {
                // Member: load only their shelters and incoming requests for those shelters
                List<Shelter> myShelters  = getSheltersByProvider(conn, loggedUser.getUserID());
                List<Request> myRequests  = getRequestsByProvider(conn, loggedUser.getUserID());

                request.setAttribute("shelters", myShelters);
                request.setAttribute("requests", myRequests);
                request.getRequestDispatcher("/WEB-INF/pages/memberDashboard.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            request.setAttribute("error", "Something went wrong loading the dashboard.");
            request.getRequestDispatcher("/WEB-INF/pages/error.jsp").forward(request, response);
        }
    }

    // --- Helper: load all shelters (Admin view) ---
    private List<Shelter> getAllShelters(Connection conn) throws SQLException {
        List<Shelter> list = new ArrayList<>();
        String sql = "SELECT s.*, u.fullName AS providerName FROM shelters s " +
                     "JOIN users u ON s.providerID = u.userID ORDER BY s.shelterID DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Shelter s = mapShelter(rs);
            s.setProviderName(rs.getString("providerName"));
            list.add(s);
        }
        return list;
    }

    // --- Helper: load shelters owned by one Member ---
    private List<Shelter> getSheltersByProvider(Connection conn, int providerID) throws SQLException {
        List<Shelter> list = new ArrayList<>();
        String sql = "SELECT * FROM shelters WHERE providerID = ? ORDER BY shelterID DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, providerID);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapShelter(rs));
        return list;
    }

    // --- Helper: load all requests (Admin view) ---
    private List<Request> getAllRequests(Connection conn) throws SQLException {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT r.*, s.shelterName FROM requests r " +
                     "JOIN shelters s ON r.shelterID = s.shelterID ORDER BY r.requestedAt DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Request r = mapRequest(rs);
            r.setShelterName(rs.getString("shelterName"));
            list.add(r);
        }
        return list;
    }

    // --- Helper: load requests for shelters owned by a Member ---
    private List<Request> getRequestsByProvider(Connection conn, int providerID) throws SQLException {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT r.*, s.shelterName FROM requests r " +
                     "JOIN shelters s ON r.shelterID = s.shelterID " +
                     "WHERE s.providerID = ? ORDER BY r.requestedAt DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, providerID);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Request r = mapRequest(rs);
            r.setShelterName(rs.getString("shelterName"));
            list.add(r);
        }
        return list;
    }

    // --- Map ResultSet row → Shelter object ---
    private Shelter mapShelter(ResultSet rs) throws SQLException {
        return new Shelter(
            rs.getInt("shelterID"),
            rs.getInt("providerID"),
            rs.getString("shelterName"),
            rs.getString("address"),
            rs.getString("city"),
            rs.getInt("totalCapacity"),
            rs.getInt("currentAvailableBeds"),
            rs.getInt("isActive") == 1,
            rs.getString("createdAt")
        );
    }

    // --- Map ResultSet row → Request object ---
    private Request mapRequest(ResultSet rs) throws SQLException {
        return new Request(
            rs.getInt("requestID"),
            rs.getInt("shelterID"),
            rs.getString("requesterName"),
            rs.getString("requesterPhone"),
            rs.getInt("numberOfPeople"),
            rs.getString("status"),
            rs.getString("notes"),
            rs.getString("requestedAt"),
            rs.getString("updatedAt")
        );
    }
}
