package com.hotelbooking.gateaway_service.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter {
    private final SecretKey key;

    @Value("${internal.secret.key}")
    private String internalSecretKey;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        boolean hasToken = authHeader != null && authHeader.startsWith("Bearer ");

        if (hasToken) {
            try {
                String token = authHeader.substring(7);
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                String id = String.valueOf(claims.get("id"));

                System.out.printf("🔐 Authenticated: username=%s, role=%s, id=%s%n", username, role, id);
                ServerHttpRequest mutated = exchange.getRequest().mutate()
                        .header("X-User", username)
                        .header("X-User-UserId", id)
                        .header("X-User-Role", role)
                        .header("X-Internal-Secret", internalSecretKey)
                        .build();

                return chain.filter(exchange.mutate().request(mutated).build());
            } catch (Exception e) {
                System.err.println("❌ JWT validation failed: " + e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        // Token yoksa da internal key'i yine ekle
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header("X-Internal-Secret", internalSecretKey)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }
}