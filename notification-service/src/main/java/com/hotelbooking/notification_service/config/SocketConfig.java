package com.hotelbooking.notification_service.config;

import com.hotelbooking.notification_service.util.UserHandshakeHandler;
import com.hotelbooking.notification_service.util.UserHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class SocketConfig implements WebSocketMessageBrokerConfigurer {

    private final UserHandshakeInterceptor userHandshakeInterceptor;
    private final UserHandshakeHandler userHandshakeHandler;

    public SocketConfig(UserHandshakeInterceptor userHandshakeInterceptor,
                        UserHandshakeHandler userHandshakeHandler) {
        this.userHandshakeInterceptor = userHandshakeInterceptor;
        this.userHandshakeHandler = userHandshakeHandler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/notify")
                .setAllowedOriginPatterns("*")
                .addInterceptors(userHandshakeInterceptor)
                .setHandshakeHandler(userHandshakeHandler)
                .withSockJS();

        // Add endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/notify")
                .setAllowedOriginPatterns("*")
                .addInterceptors(userHandshakeInterceptor)
                .setHandshakeHandler(userHandshakeHandler);
    }
}