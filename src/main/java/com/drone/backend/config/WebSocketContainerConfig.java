package com.drone.backend.config;

import org.apache.tomcat.websocket.server.WsContextListener;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Slf4j
@Configuration
public class WebSocketContainerConfig {

    private static final int MAX_SIZE = 10 * 1024 * 1024; // 10 MB

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

        container.setMaxBinaryMessageBufferSize(MAX_SIZE);
        container.setMaxTextMessageBufferSize(MAX_SIZE);
        container.setMaxSessionIdleTimeout(0L);

        return container;
    }
}