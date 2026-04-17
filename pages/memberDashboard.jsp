<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.safehaven.model.User, com.safehaven.model.Shelter, com.safehaven.model.Request, java.util.List" %>
<%
    User loggedUser   = (User) session.getAttribute("loggedUser");
    List<Shelter> shelters = (List<Shelter>) request.getAttribute("shelters");
    List<Request> requests = (List<Request>) request.getAttribute("requests");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SafeHaven - Member Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<header class="navbar">
    <div class="nav-brand">SafeHaven</div>
    <nav class="nav-links">
        <a href="${pageContext.request.contextPath}/shelter" class="btn btn-outline">Add shelter</a>
        <span class="nav-user">Welcome, <%= loggedUser.getFullName() %></span>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline">Logout</a>
    </nav>
</header>

<main class="container">
    <h2>My shelters</h2>

    <% if (shelters.isEmpty()) { %>
        <div class="empty-state">You have not added any shelters yet. <a href="${pageContext.request.contextPath}/shelter">Add one now.</a></div>
    <% } else { %>
    <div class="table-wrapper">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Shelter name</th>
                    <th>City</th>
                    <th>Total beds</th>
                    <th>Available beds</th>
                    <th>Occupancy</th>
                    <th>Update availability</th>
                </tr>
            </thead>
            <tbody>
            <% for (Shelter s : shelters) { %>
                <tr>
                    <td><%= s.getShelterName() %></td>
                    <td><%= s.getCity() %></td>
                    <td><%= s.getTotalCapacity() %></td>
                    <td><%= s.getCurrentAvailableBeds() %></td>
                    <td><%= s.getOccupancyPercent() %>%</td>
                    <td>
                        <form action="${pageContext.request.contextPath}/shelter" method="post" style="display:flex; gap:8px; align-items:center;">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="shelterID" value="<%= s.getShelterID() %>">
                            <input type="number" name="currentAvailableBeds" value="<%= s.getCurrentAvailableBeds() %>"
                                   min="0" max="<%= s.getTotalCapacity() %>" class="input-sm">
                            <button type="submit" class="btn btn-primary btn-sm">Save</button>
                        </form>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>

    <section class="section">
        <h3>Incoming requests</h3>
        <% if (requests.isEmpty()) { %>
            <div class="empty-state">No assistance requests yet.</div>
        <% } else { %>
        <div class="table-wrapper">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Requester</th>
                        <th>Phone</th>
                        <th>Shelter</th>
                        <th>People</th>
                        <th>Notes</th>
                        <th>Status</th>
                        <th>Date</th>
                    </tr>
                </thead>
                <tbody>
                <% for (Request r : requests) { %>
                    <tr>
                        <td><%= r.getRequesterName() %></td>
                        <td><%= r.getRequesterPhone() %></td>
                        <td><%= r.getShelterName() %></td>
                        <td><%= r.getNumberOfPeople() %></td>
                        <td><%= r.getNotes() != null ? r.getNotes() : "-" %></td>
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
        <% } %>
    </section>
</main>
</body>
</html>
