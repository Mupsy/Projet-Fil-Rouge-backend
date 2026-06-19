
package com.drone.backend.config;

import com.drone.backend.websocket.MobileWebSocketHandler;
import com.drone.backend.websocket.UnityWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final UnityWebSocketHandler unityHandler;
    private final MobileWebSocketHandler mobileHandler;

    public WebSocketConfig(UnityWebSocketHandler unityHandler,
                           MobileWebSocketHandler mobileHandler) {
        this.unityHandler = unityHandler;
        this.mobileHandler = mobileHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(unityHandler, "/unity")
                .setAllowedOrigins("*");
        registry.addHandler(mobileHandler, "/mobile")
                .setAllowedOrigins("*");
    }
}
