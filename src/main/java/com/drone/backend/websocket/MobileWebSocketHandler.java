
package com.drone.backend.websocket;

import com.drone.backend.model.DroneInputs;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class MobileWebSocketHandler extends AbstractWebSocketHandler {

    private final SessionRegistry registry;
    private final UnityWebSocketHandler unityHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Compteurs stats
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong messagesInvalid  = new AtomicLong(0);

    public MobileWebSocketHandler(SessionRegistry registry,
                                   UnityWebSocketHandler unityHandler) {
        this.registry = registry;
        this.unityHandler = unityHandler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        registry.addMobile(session);
        log.info("📱 [MOBILE] Connexion établie — sessionId={} remoteAddr={} totalConnectés={}",
                session.getId(),
                session.getRemoteAddress(),
                registry.getMobileCount());
        log.info("📱 [MOBILE] Unity disponible: {}", registry.isUnityConnected());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.removeMobile(session);
        log.info("📱 [MOBILE] Connexion fermée — sessionId={} code={} reason='{}' totalRestants={} | Stats: messagesReçus={} invalides={}",
                session.getId(),
                status.getCode(),
                status.getReason(),
                registry.getMobileCount(),
                messagesReceived.get(),
                messagesInvalid.get());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("📱 [MOBILE] ❌ Erreur transport — sessionId={} erreur='{}'",
                session.getId(),
                exception.getMessage(),
                exception);
    }

    /**
     * Reçoit les inputs JSON du mobile et les forward à Unity.
     * Format : {"t":0.5,"p":0.1,"r":0.0,"y":0.2,"timestamp":...}
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        messagesReceived.incrementAndGet();

        log.debug("📱 [MOBILE] Message reçu — sessionId={} payload='{}'",
                session.getId(), payload);

        // Vérification connexion Unity
        if (!registry.isUnityConnected()) {
            log.warn("📱 [MOBILE] ⚠️ Message reçu mais Unity non connecté — inputs perdus: '{}'",
                    payload);
        }

        try {
            // Validation du format JSON
            DroneInputs inputs = objectMapper.readValue(payload, DroneInputs.class);

            log.debug("📱 [MOBILE] Inputs validés — t={} p={} r={} y={} ts={}",
                    inputs.getT(),
                    inputs.getP(),
                    inputs.getR(),
                    inputs.getY(),
                    inputs.getTimestamp());

            // Forward à Unity
            unityHandler.forwardInputsToUnity(payload);

        } catch (Exception e) {
            messagesInvalid.incrementAndGet();
            log.error("📱 [MOBILE] ❌ JSON invalide — sessionId={} payload='{}' erreur='{}'",
                    session.getId(),
                    payload,
                    e.getMessage());
        }
    }
}