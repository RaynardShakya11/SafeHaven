package com.safehaven.controllers;

import com.safehaven.config.DBConfig;

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

/**
 * LoginServlet - Handles user authentication for SafeHaven.
 * GET  → Loads the login page (login.jsp)
 * POST → Validates credentials and redirects based on role
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Maximum failed attempts before account lockout
    private static final int MAX_FAILED_ATTEMPTS = 5;

    /**
     * doGet - Forwards the user to the login page.
     * If user is already logged in, redirect to dashboard.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If session already exists and user is logged in, skip login page
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userID") != null) {
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            return;
        }

        // Forward to login JSP
        request.getRequestDispatcher("/WEB-INF/pages/login.jsp").forward(request, response);
    }

    /**
     * doPost - Processes the login form submission.
     * Validates credentials against the database,
     * checks for account lockout, and creates a session on success.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Retrieve form inputs
        String email    = request.getParameter("email");
        String password = request.getParameter("password");

        // Basic input validation
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Email and password are required.");
            request.getRequestDispatcher("/WEB-INF/pages/login.jsp").forward(request, response);
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConfig.getConnection();

            // Fetch user record by email
            String sql = "SELECT userID, fullName, email, password, phone, role, failedAttempts, isLocked " +
                         "FROM users WHERE email = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email.trim());
            rs = stmt.executeQuery();

            if (rs.next()) {
                boolean isLocked     = rs.getBoolean("isLocked");
                int failedAttempts   = rs.getInt("failedAttempts");
                String storedPassword = rs.getString("password");

                // Check if account is locked
                if (isLocked) {
                    request.setAttribute("errorMessage",
                        "Your account has been temporarily locked due to multiple failed attempts. " +
                        "Please contact the administrator.");
                    request.getRequestDispatcher("/WEB-INF/pages/login.jsp").forward(request, response);
                    return;
                }

                // Validate password (plain text comparison — swap with hash check if using BCrypt)
                if (storedPassword.equals(password)) {
                    // ---- Login Successful ----

                    // Reset failed attempts on success
                    resetFailedAttempts(conn, email.trim());

                    // Create session and store user attributes
                    HttpSession session = request.getSession(true);
                    session.setAttribute("userID",   rs.getInt("userID"));
                    session.setAttribute("fullName", rs.getString("fullName"));
                    session.setAttribute("email",    rs.getString("email"));
                    session.setAttribute("role",     rs.getString("role"));
                    session.setMaxInactiveInterval(30 * 60); // 30-minute session timeout

                    // Role-based redirect
                    String role = rs.getString("role");
                    if ("Admin".equalsIgnoreCase(role)) {
                        response.sendRedirect(request.getContextPath() + "/AdminDashboardServlet");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/DashboardServlet");
                    }

                } else {
                    // ---- Wrong Password ----
                    int newAttempts = failedAttempts + 1;
                    updateFailedAttempts(conn, email.trim(), newAttempts);

                    if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                        lockAccount(conn, email.trim());
                        request.setAttribute("errorMessage",
                            "Your account has been locked after " + MAX_FAILED_ATTEMPTS +
                            " failed attempts. Please contact the administrator.");
                    } else {
                        request.setAttribute("errorMessage",
                            "Invalid credentials. " +
                            (MAX_FAILED_ATTEMPTS - newAttempts) + " attempt(s) remaining.");
                    }

                    request.getRequestDispatcher("/WEB-INF/pages/login.jsp").forward(request, response);
                }

            } else {
                // No user found with that email
                request.setAttribute("errorMessage", "Invalid credentials. Please try again.");
                request.getRequestDispatcher("/WEB-INF/pages/login.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Something went wrong. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/pages/login.jsp").forward(request, response);

        } finally {
            // Close resources safely
            try { if (rs   != null) rs.close();   } catch (SQLException ignored) {}
            try { if (stmt != null) stmt.close();  } catch (SQLException ignored) {}
            try { if (conn != null) conn.close();  } catch (SQLException ignored) {}
        }
    }

    // -----------------------------------------------------------------------
    // Helper Methods
    // -----------------------------------------------------------------------

    /** Increments the failed login attempt count for a given email. */
    private void updateFailedAttempts(Connection conn, String email, int attempts)
            throws SQLException {
        String sql = "UPDATE users SET failedAttempts = ? WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attempts);
            stmt.setString(2, email);
            stmt.executeUpdate();
        }
    }

    /** Locks the account when failed attempts exceed the threshold. */
    private void lockAccount(Connection conn, String email) throws SQLException {
        String sql = "UPDATE users SET isLocked = TRUE WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        }
    }

    /** Resets failed attempts to 0 after a successful login. */
    private void resetFailedAttempts(Connection conn, String email) throws SQLException {
        String sql = "UPDATE users SET failedAttempts = 0, isLocked = FALSE WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        }
    }
}
