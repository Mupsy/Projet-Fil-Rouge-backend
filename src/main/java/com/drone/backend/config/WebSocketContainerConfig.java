package com.drone.backend.config;

import org.apache.tomcat.websocket.server.WsContextListener;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
public class WebSocketContainerConfig {

    private static final int MAX_SIZE = 10 * 1024 * 1024; // 10 MB

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean factory = new ServletServerContainerFactoryBean();
        factory.setMaxBinaryMessageBufferSize(MAX_SIZE);
        factory.setMaxTextMessageBufferSize(MAX_SIZE);
        factory.setMaxSessionIdleTimeout(3600000L);
        return factory;
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addContextCustomizers(context -> {
            // Attributs JSR-356 lus par Tomcat avant d'ouvrir la session WS
            context.setAttribute(
                "org.apache.tomcat.websocket.binaryBufferSize",
                MAX_SIZE
            );
            context.setAttribute(
                "org.apache.tomcat.websocket.textBufferSize",
                MAX_SIZE
            );
        });
    }
}