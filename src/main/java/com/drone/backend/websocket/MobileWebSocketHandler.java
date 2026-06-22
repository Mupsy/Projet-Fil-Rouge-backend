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

    private static final int MAX_BUFFER_SIZE = 10 * 1024 * 1024; // 10 MB

    private final SessionRegistry registry;
    private final UnityWebSocketHandler unityHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong messagesInvalid  = new AtomicLong(0);

    public MobileWebSocketHandler(SessionRegistry registry,
                                   UnityWebSocketHandler unityHandler) {
        this.registry = registry;
        this.unityHandler = unityHandler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // ✅ Fix buffer : le mobile doit aussi pouvoir recevoir les frames JPG relayées
        session.setBinaryMessageSizeLimit(MAX_BUFFER_SIZE);
        session.setTextMessageSizeLimit(MAX_BUFFER_SIZE);

        registry.addMobile(session);
        log.info("📱 [MOBILE] Connexion établie — sessionId={} remoteAddr={} totalConnectés={} bufferMax={}MB",
                session.getId(),
                session.getRemoteAddress(),
                registry.getMobileCount(),
                MAX_BUFFER_SIZE / 1024 / 1024);
        log.info("📱 [MOBILE] Unity disponible: {}", registry.isUnityConnected());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.removeMobile(session);
        log.info("📱 [MOBILE] Connexion fermée — sessionId={} code={} reason='{}' totalRestants={} | Stats: reçus={} invalides={}",
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

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        messagesReceived.incrementAndGet();

        log.debug("📱 [MOBILE] Message reçu — sessionId={} payload='{}'",
                session.getId(), payload);

        if (!registry.isUnityConnected()) {
            log.warn("📱 [MOBILE] ⚠️ Unity non connecté — inputs perdus");
        }

        try {
            DroneInputs inputs = objectMapper.readValue(payload, DroneInputs.class);
            log.debug("📱 [MOBILE] Inputs validés — t={} p={} r={} y={} ts={}",
                    inputs.getT(), inputs.getP(),
                    inputs.getR(), inputs.getY(),
                    inputs.getTimestamp());
            unityHandler.forwardInputsToUnity(payload);
        } catch (Exception e) {
            messagesInvalid.incrementAndGet();
            log.error("📱 [MOBILE] ❌ JSON invalide — payload='{}' erreur='{}'",
                    payload, e.getMessage());
        }
    }
}