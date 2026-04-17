package com.safehaven.controllers;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * AuthFilter.java
 * Intercepts every request and redirects unauthenticated users to /login.
 * Public URLs (login, register, css) are excluded from filtering.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    // URLs that do NOT require login
    private static final String[] PUBLIC_URLS = {
        "/login", "/register", "/css/"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // Allow public URLs through without checking session
        for (String publicUrl : PUBLIC_URLS) {
            if (uri.startsWith(contextPath + publicUrl)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // Check session
        HttpSession session = request.getSession(false);
        boolean loggedIn    = (session != null && session.getAttribute("loggedUser") != null);

        if (!loggedIn) {
            response.sendRedirect(contextPath + "/login");
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void destroy() {}
}
