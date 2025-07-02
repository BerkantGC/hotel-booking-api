package com.hotelbooking.comment_service.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
public class InternalAuthFilter implements Filter {

    @Value("${internal.secret}")
    private String internalSecret;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String incomingSecret = req.getHeader("X-Internal-Secret");
        String role = req.getHeader("X-User-Role");
        String id = req.getHeader("X-User-UserId");

        // For non-internal requests (no secret), require authentication
        if (incomingSecret == null) {
            System.out.println("No secret header found");
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "No authentication provided");
            return;
        }

        // For internal requests, validate the secret
        if (!incomingSecret.equals(internalSecret)) {
            System.out.println("Unauthorized gateway");
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized gateway");
            return;
        }

        // For valid internal requests, set up authentication if user info is provided
        if (role != null && id != null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(id, null, Collections.singletonList(authority));
            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("User authenticated with ID: " + id + " and role: " + role);
        }

        chain.doFilter(request, response);
    }
}
