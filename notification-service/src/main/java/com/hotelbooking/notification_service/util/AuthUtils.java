package com.hotelbooking.notification_service.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {
    public static Long getUserId() {
        String id = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return Long.parseLong(id);
    }

    public static String[] getUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null)
            return new String[0];

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);
    }
}