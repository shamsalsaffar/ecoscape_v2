package com.java2024.ecoscape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    //STOMP är en protokol för kommunikation mellan server och klient
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //tillåtter all domains att koppla sig till denna socket, senare kan autontification adderas, men nu kan alla se
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*")
                .withSockJS();

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //där ska notiser skickas från server till klient
        registry.enableSimpleBroker("/topic", "/queue");
        //där ska notiser skickas från klient till server, tror inte att det (dock troligen inte behövs för push app notifikationer
        registry.setApplicationDestinationPrefixes("/app");
        //behövs för personliga notiser
        registry.setUserDestinationPrefix("/user");
    }
}

