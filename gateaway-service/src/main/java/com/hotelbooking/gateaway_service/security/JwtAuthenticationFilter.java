package com.hotelbooking.gateaway_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final SecretKey key;

    @Value("${internal.secret.key}")
    private String internalSecretKey;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String token = extractToken(req);

        log.info("URI: {} ---> token: {}", req.getURI(), token);

        if (token == null) {
            // No token provided - add internal secret and continue
            return chain.filter(
                    exchange.mutate()
                            .request(req.mutate()
                                    .header("X-Internal-Secret", internalSecretKey)
                                    .build())
                            .build());
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            String id = String.valueOf(claims.get("id"));

            log.info("User {} with role {} logged in", username, role);
            // Create mutated request with user info and internal secret
            ServerHttpRequest.Builder builder = req.mutate()
                    .header("X-User", username)
                    .header("X-User-UserId", id)
                    .header("X-User-Role", role)
                    .header("X-Internal-Secret", internalSecretKey);

            return chain.filter(
                    exchange.mutate()
                            .request(builder.uri(req.getURI()).build())
                            .build());

        } catch (JwtException e) {
            log.error("JWT validation failed for URI {}: {}", req.getURI(), e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation for URI {}: {}", req.getURI(), e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * Extract token from various sources in order of preference:
     * 1. Authorization header (Bearer token)
     * 2. WebSocket protocol header
     * 3. Query parameter
     */
    private String extractToken(ServerHttpRequest request) {
        return extractBearer(request.getHeaders());
    }

    /** Authorization: Bearer xxx */
    private String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

}