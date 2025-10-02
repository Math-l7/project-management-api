package project_management_api.project_management_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Ativa um broker simples, que envia mensagens para tópicos "/topic"
        config.enableSimpleBroker("/topic");
        // Prefixo para endpoints do lado do cliente que envia mensagens
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint que o front vai usar para se conectar via WebSocket + SockJS
        registry.addEndpoint("/ws-message")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
