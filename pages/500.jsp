<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>SafeHaven - Server error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="error-page">
    <h1>500</h1>
    <p>Something went wrong on our end. Please try again shortly.</p>
    <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary">Go to dashboard</a>
</div>
</body>
</html>
