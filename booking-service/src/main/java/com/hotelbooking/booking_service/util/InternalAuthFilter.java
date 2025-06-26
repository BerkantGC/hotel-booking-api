package com.hotelbooking.booking_service.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class InternalAuthFilter implements Filter {

    @Value("${internal.secret.key}")
    private String internalSecretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String incomingSecret = req.getHeader("X-Internal-Secret");

        System.out.println("Internal secret: " + incomingSecret);
        if (incomingSecret == null || !incomingSecret.equals(internalSecretKey)) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized gateway");
            return;
        }

        chain.doFilter(request, response);
    }
}
