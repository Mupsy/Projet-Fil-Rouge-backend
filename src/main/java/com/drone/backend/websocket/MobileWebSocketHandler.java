
package com.drone.backend.websocket;

import com.drone.backend.model.DroneInputs;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;


@Slf4j
@Component
public class MobileWebSocketHandler extends AbstractWebSocketHandler {

    private final SessionRegistry registry;
    private final UnityWebSocketHandler unityHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MobileWebSocketHandler(SessionRegistry registry,
                                  UnityWebSocketHandler unityHandler) {
        this.registry = registry;
        this.unityHandler = unityHandler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        registry.addMobile(session);
        log.info("📱 Mobile connecté : {} (total: {})",
                session.getId(), registry.getMobileCount());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus status) {
        registry.removeMobile(session);
        log.info("📱 Mobile déconnecté : {} (total restant: {})",
                session.getId(), registry.getMobileCount());
    }

    @Override
    public void handleTransportError(WebSocketSession session,
                                     Throwable exception) {
        log.error("Erreur transport mobile {}: {}",
                session.getId(), exception.getMessage());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session,
                                     TextMessage message) {
        String payload = message.getPayload();
        log.debug("📥 Inputs reçus du mobile {}: {}",
                session.getId(), payload);

        try {
            DroneInputs inputs = objectMapper.readValue(payload,
                    DroneInputs.class);
            unityHandler.forwardInputsToUnity(payload);

        } catch (Exception e) {
            log.error("JSON invalide reçu du mobile {}: {}",
                    session.getId(), e.getMessage());
        }
    }
}