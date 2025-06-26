package com.hotelbooking.hotel_admin_service.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {
    public static Long getUserId() {
        String id = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return Long.parseLong(id);
    }

    public static String[] getUserRole() {
        return (String[]) SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray();
    }
}