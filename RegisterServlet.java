package com.safehaven.controllers;

import com.safehaven.config.DBConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RegisterServlet - Handles new user registration for SafeHaven.
 * GET  → Loads the registration page (register.jsp)
 * POST → Validates input, checks duplicates, and inserts new user
 */
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * doGet - Forwards to the registration JSP page.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/WEB-INF/pages/register.jsp").forward(request, response);
    }

    /**
     * doPost - Processes the registration form submission.
     * Validates all input fields, checks for duplicate email/phone,
     * and inserts a new user record into the database.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Retrieve form parameters
        String fullName        = request.getParameter("fullName");
        String email           = request.getParameter("email");
        String password        = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String phone           = request.getParameter("phone");
        String role            = request.getParameter("role"); // "Admin" or "Member"

        // ---- Input Validation ----
        String validationError = validateInputs(fullName, email, password, confirmPassword, phone, role);
        if (validationError != null) {
            request.setAttribute("errorMessage", validationError);
            // Preserve entered values so user doesn't re-type everything
            preserveFormValues(request, fullName, email, phone, role);
            request.getRequestDispatcher("/WEB-INF/pages/register.jsp").forward(request, response);
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConfig.getConnection();

            // ---- Check for duplicate email ----
            if (isDuplicateEmail(conn, email.trim())) {
                request.setAttribute("errorMessage", "An account with this email already exists.");
                preserveFormValues(request, fullName, email, phone, role);
                request.getRequestDispatcher("/WEB-INF/pages/register.jsp").forward(request, response);
                return;
            }

            // ---- Check for duplicate phone ----
            if (isDuplicatePhone(conn, phone.trim())) {
                request.setAttribute("errorMessage",
                    "An account with this phone number already exists.");
                preserveFormValues(request, fullName, email, phone, role);
                request.getRequestDispatcher("/WEB-INF/pages/register.jsp").forward(request, response);
                return;
            }

            // ---- Insert new user ----
            // NOTE: For production, hash the password using BCrypt before storing.
            // Example: String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String sql = "INSERT INTO users (fullName, email, password, phone, role, failedAttempts, isLocked) " +
                         "VALUES (?, ?, ?, ?, ?, 0, FALSE)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, fullName.trim());
            stmt.setString(2, email.trim());
            stmt.setString(3, password);          // Replace with hashed password in production
            stmt.setString(4, phone.trim());
            stmt.setString(5, role.trim());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                // Registration successful — redirect to login with success message
                response.sendRedirect(request.getContextPath() +
                    "/LoginServlet?success=Account+created+successfully.+Please+log+in.");
            } else {
                request.setAttribute("errorMessage", "Registration failed. Please try again.");
                request.getRequestDispatcher("/WEB-INF/pages/register.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Something went wrong. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/pages/register.jsp").forward(request, response);

        } finally {
            try { if (rs   != null) rs.close();   } catch (SQLException ignored) {}
            try { if (stmt != null) stmt.close();  } catch (SQLException ignored) {}
            try { if (conn != null) conn.close();  } catch (SQLException ignored) {}
        }
    }

    // -----------------------------------------------------------------------
    // Helper Methods
    // -----------------------------------------------------------------------

    /**
     * Validates all registration form fields.
     * Returns an error message string if invalid, or null if everything is fine.
     */
    private String validateInputs(String fullName, String email, String password,
                                  String confirmPassword, String phone, String role) {

        // Check for empty fields
        if (isNullOrEmpty(fullName) || isNullOrEmpty(email) || isNullOrEmpty(password) ||
            isNullOrEmpty(confirmPassword) || isNullOrEmpty(phone) || isNullOrEmpty(role)) {
            return "All fields are required. Please fill in the form completely.";
        }

        // Full name must not contain numbers
        if (!fullName.trim().matches("[a-zA-Z\\s]+")) {
            return "Full name must contain letters only. Please enter a valid name.";
        }

        // Email format validation
        if (!email.trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return "Please enter a valid email address.";
        }

        // Password minimum length
        if (password.length() < 6) {
            return "Password must be at least 6 characters long.";
        }

        // Passwords must match
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match. Please re-enter your password.";
        }

        // Phone must be numeric digits (10-15 digits)
        if (!phone.trim().matches("\\d{10,15}")) {
            return "Phone number must contain 10 to 15 digits only.";
        }

        // Role must be one of the allowed values
        if (!"Admin".equals(role) && !"Member".equals(role)) {
            return "Invalid role selected. Please choose Admin or Member.";
        }

        return null; // All validations passed
    }

    /** Checks if an email already exists in the database. */
    private boolean isDuplicateEmail(Connection conn, String email) throws SQLException {
        String sql = "SELECT userID FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true if a record was found
            }
        }
    }

    /** Checks if a phone number already exists in the database. */
    private boolean isDuplicatePhone(Connection conn, String phone) throws SQLException {
        String sql = "SELECT userID FROM users WHERE phone = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true if a record was found
            }
        }
    }

    /** Preserves entered form values as request attributes so the JSP can re-populate the fields. */
    private void preserveFormValues(HttpServletRequest request,
                                    String fullName, String email,
                                    String phone, String role) {
        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
        request.setAttribute("role", role);
    }

    /** Null/empty string check helper. */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
