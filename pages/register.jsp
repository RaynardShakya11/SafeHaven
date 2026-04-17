<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SafeHaven - Register</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-wrapper">
    <div class="auth-card">
        <div class="auth-logo">
            <h1>SafeHaven</h1>
            <p>Create your account</p>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/register" method="post">
            <div class="form-group">
                <label for="fullName">Full name</label>
                <input type="text" id="fullName" name="fullName" placeholder="e.g. Ram Sharma" required>
            </div>
            <div class="form-group">
                <label for="email">Email address</label>
                <input type="email" id="email" name="email" placeholder="you@example.com" required>
            </div>
            <div class="form-group">
                <label for="phone">Phone number</label>
                <input type="text" id="phone" name="phone" placeholder="10-digit number" maxlength="10" required>
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" placeholder="Minimum 8 characters" required>
            </div>
            <div class="form-group">
                <label for="role">Role</label>
                <select id="role" name="role">
                    <option value="Member">Member (Shelter Provider)</option>
                    <option value="Admin">Admin (NGO Manager)</option>
                </select>
            </div>
            <button type="submit" class="btn btn-primary btn-full">Create account</button>
        </form>

        <p class="auth-link">Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in</a></p>
    </div>
</div>
</body>
</html>
