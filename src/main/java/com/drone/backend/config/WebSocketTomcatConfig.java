package com.drone.backend.config;

import org.apache.tomcat.websocket.server.WsSci;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketTomcatConfig {

    private static final int MAX = 10 * 1024 * 1024;

    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> websocketCustomizer() {
        return factory -> factory.addContextCustomizers(context -> {
            context.addServletContainerInitializer(new WsSci(), null);

            context.setInitParameter("org.apache.tomcat.websocket.binaryBufferSize",
                    String.valueOf(MAX));

            context.setInitParameter("org.apache.tomcat.websocket.textBufferSize",
                    String.valueOf(MAX));
        });
    }
}