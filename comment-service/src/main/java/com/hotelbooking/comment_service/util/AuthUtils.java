package com.hotelbooking.comment_service.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {
    public static Long getUserId() {
        String id = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return Long.parseLong(id);
    }

    public static boolean isSignedIn() {
        return SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                !(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof String &&
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser"));
    }

}