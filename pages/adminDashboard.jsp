<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.safehaven.model.User, com.safehaven.model.Shelter, com.safehaven.model.Request, java.util.List" %>
<%
    User loggedUser = (User) session.getAttribute("loggedUser");
    List<Shelter> shelters = (List<Shelter>) request.getAttribute("shelters");
    List<Request> requests = (List<Request>) request.getAttribute("requests");
    int totalBeds     = (int) request.getAttribute("totalBeds");
    int availableBeds = (int) request.getAttribute("availableBeds");
    long pendingCount = (long) request.getAttribute("pendingCount");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SafeHaven - Admin Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<header class="navbar">
    <div class="nav-brand">SafeHaven</div>
    <nav class="nav-links">
        <span class="nav-user">Welcome, <%= loggedUser.getFullName() %> (Admin)</span>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline">Logout</a>
    </nav>
</header>

<main class="container">
    <h2>Admin Dashboard</h2>

    <!-- Summary Cards -->
    <div class="stats-grid">
        <div class="stat-card">
            <p class="stat-label">Total shelters</p>
            <p class="stat-value"><%= shelters.size() %></p>
        </div>
        <div class="stat-card">
            <p class="stat-label">Total beds</p>
            <p class="stat-value"><%= totalBeds %></p>
        </div>
        <div class="stat-card">
            <p class="stat-label">Available beds</p>
            <p class="stat-value"><%= availableBeds %></p>
        </div>
        <div class="stat-card">
            <p class="stat-label">Pending requests</p>
            <p class="stat-value"><%= pendingCount %></p>
        </div>
    </div>

    <!-- Shelters Table -->
    <section class="section">
        <h3>All shelters</h3>
        <div class="table-wrapper">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Shelter</th>
                        <th>City</th>
                        <th>Provider</th>
                        <th>Total beds</th>
                        <th>Available</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                <% for (Shelter s : shelters) { %>
                    <tr>
                        <td><%= s.getShelterName() %></td>
                        <td><%= s.getCity() %></td>
                        <td><%= s.getProviderName() %></td>
                        <td><%= s.getTotalCapacity() %></td>
                        <td><%= s.getCurrentAvailableBeds() %></td>
                        <td><span class="badge <%= s.isActive() ? "badge-success" : "badge-danger" %>"><%= s.isActive() ? "Active" : "Inactive" %></span></td>
                        <td>
                            <form action="${pageContext.request.contextPath}/shelter" method="post" style="display:inline;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="shelterID" value="<%= s.getShelterID() %>">
                                <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Delete this shelter?')">Delete</button>
                            </form>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </section>

    <!-- Requests Table -->
    <section class="section">
        <h3>All assistance requests</h3>
        <div class="table-wrapper">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Requester</th>
                        <th>Phone</th>
                        <th>Shelter</th>
                        <th>People</th>
                        <th>Status</th>
                        <th>Requested at</th>
                    </tr>
                </thead>
                <tbody>
                <% for (Request r : requests) { %>
                    <tr>
                        <td><%= r.getRequesterName() %></td>
                        <td><%= r.getRequesterPhone() %></td>
                        <td><%= r.getShelterName() %></td>
                        <td><%= r.getNumberOfPeople() %></td>
                        <td>
                            <span class="badge
                                <%= r.isPending()   ? "badge-warning" : "" %>
                                <%= r.isFulfilled() ? "badge-success" : "" %>
                                <%= r.isRejected()  ? "badge-danger"  : "" %>">
                                <%= r.getStatus() %>
                            </span>
                        </td>
                        <td><%= r.getRequestedAt() %></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>
