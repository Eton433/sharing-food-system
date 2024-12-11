package com.example.sharingfood;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理
        registry.enableSimpleBroker("/topic"); // 客戶端訂閱的前綴
        registry.setApplicationDestinationPrefixes("/app"); // 客戶端發送消息的前綴
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 註冊 WebSocket 端點
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }
}
