package com.hotelbooking.notification_service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class UserHandshakeInterceptor implements HandshakeInterceptor {
    @Value("${internal.secret.key}")
    private String internalSecretKey;

    @Override
    public boolean beforeHandshake(ServerHttpRequest req,
                                   ServerHttpResponse res,
                                   WebSocketHandler handler,
                                   Map<String, Object> attributes) {

        String id = req.getHeaders().getFirst("X-User-UserId");
        String username = req.getHeaders().getFirst("X-User");
        String role = req.getHeaders().getFirst("X-User-Role");
        String secret = req.getHeaders().getFirst("X-Internal-Secret");

        if (secret == null || !secret.equals(internalSecretKey)) {
            log.error("Unauthorized gateway access: secret is not valid");
            res.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        if (id != null) {
            attributes.put("USER_ID", id);
            attributes.put("USER_USERNAME", username);
            attributes.put("USER_ROLE", role);
            log.info("User -> id: {}, username: {}, role: {} connected", id, username, role);
            return true;
        }
        log.error("Unauthorized gateway access: missing user");
        res.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
