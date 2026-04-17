<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SafeHaven - Add Shelter</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<header class="navbar">
    <div class="nav-brand">SafeHaven</div>
    <nav class="nav-links">
        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-outline">Back to dashboard</a>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline">Logout</a>
    </nav>
</header>

<main class="container container-narrow">
    <h2>Add a new shelter</h2>

    <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error"><%= request.getAttribute("error") %></div>
    <% } %>

    <div class="form-card">
        <form action="${pageContext.request.contextPath}/shelter" method="post">
            <input type="hidden" name="action" value="add">

            <div class="form-group">
                <label for="shelterName">Shelter name</label>
                <input type="text" id="shelterName" name="shelterName" placeholder="e.g. Sunrise Shelter" required>
            </div>
            <div class="form-group">
                <label for="address">Address</label>
                <input type="text" id="address" name="address" placeholder="Street address" required>
            </div>
            <div class="form-group">
                <label for="city">City</label>
                <input type="text" id="city" name="city" placeholder="e.g. Kathmandu" required>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label for="totalCapacity">Total capacity</label>
                    <input type="number" id="totalCapacity" name="totalCapacity" min="1" placeholder="e.g. 50" required>
                </div>
                <div class="form-group">
                    <label for="currentAvailableBeds">Available beds</label>
                    <input type="number" id="currentAvailableBeds" name="currentAvailableBeds" min="0" placeholder="e.g. 20" required>
                </div>
            </div>
            <button type="submit" class="btn btn-primary btn-full">Add shelter</button>
        </form>
    </div>
</main>
</body>
</html>
