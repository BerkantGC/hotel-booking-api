package com.hotelbooking.gateaway_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

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

        String token = extractBearer(req.getHeaders())
                .orElseGet(() -> extractFromWsProtocol(req.getHeaders())
                        .orElseGet(() -> extractFromQuery(req.getURI())));

        if (token == null) {
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
            String role     = claims.get("role", String.class);
            String id       = String.valueOf(claims.get("id"));

            ServerHttpRequest mutated = req.mutate()
                    .header("X-User", username)
                    .header("X-User-UserId", id)
                    .header("X-User-Role", role)
                    .header("X-Internal-Secret", internalSecretKey)
                    .build();

            URI cleanUri = UriComponentsBuilder
                    .fromUri(mutated.getURI())
                    .replaceQueryParam("token")
                    .build(true)
                    .toUri();

            return chain.filter(
                    exchange.mutate()
                            .request(mutated.mutate().uri(cleanUri).build())
                            .build());

        } catch (Exception e) {
            System.err.println("❌ JWT validation failed: " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    /** Authorization: Bearer xxx */
    private Optional<String> extractBearer(HttpHeaders headers) {
        String h = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (h != null && h.startsWith("Bearer ")) return Optional.of(h.substring(7));
        return Optional.empty();
    }

    /** Sec-WebSocket-Protocol: jwt.xxx  (veya `Bearer,eyJ...`) */
    private Optional<String> extractFromWsProtocol(HttpHeaders headers) {
        List<String> protocols = headers.getOrEmpty("Sec-WebSocket-Protocol");
        return protocols.stream()
                .filter(p -> p.startsWith("jwt.") || p.startsWith("Bearer "))
                .map(p -> p.startsWith("Bearer ") ? p.substring(7) : p)
                .findFirst();
    }

    /** ws://.../chat?token=xxx */
    private String extractFromQuery(URI uri) {
        return UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst("token");
    }
}
