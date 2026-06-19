
package com.drone.backend.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import java.io.IOException;


@Slf4j
@Component
public class UnityWebSocketHandler extends AbstractWebSocketHandler {

    private final SessionRegistry registry;

    public UnityWebSocketHandler(SessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        registry.registerUnity(session);
        log.info("✅ Unity connecté : {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus status) {
        registry.clearUnity();
        log.info("❌ Unity déconnecté : {}", status);
    }

    @Override
    public void handleTransportError(WebSocketSession session,
                                     Throwable exception) {
        log.error("Erreur transport Unity: {}", exception.getMessage());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session,
                                       BinaryMessage message) {
        byte[] jpgFrame = message.getPayload().array();

        for (WebSocketSession mobile : registry.getMobileSessions()) {
            if (mobile.isOpen()) {
                try {
                    mobile.sendMessage(new BinaryMessage(jpgFrame));
                } catch (IOException e) {
                    log.error("Erreur envoi frame au mobile {}: {}",
                            mobile.getId(), e.getMessage());
                }
            }
        }
    }


    @Override
    protected void handleTextMessage(WebSocketSession session,
                                     TextMessage message) {
        log.debug("📥 Message texte reçu de Unity: {}", message.getPayload());

        for (WebSocketSession mobile : registry.getMobileSessions()) {
            if (mobile.isOpen()) {
                try {
                    mobile.sendMessage(message);
                } catch (IOException e) {
                    log.error("Erreur relai texte au mobile: {}", e.getMessage());
                }
            }
        }
    }


    public void forwardInputsToUnity(String inputsJson) {
        WebSocketSession unity = registry.getUnitySession();
        if (unity != null && unity.isOpen()) {
            try {
                unity.sendMessage(new TextMessage(inputsJson));
            } catch (IOException e) {
                log.error("Erreur envoi inputs à Unity: {}", e.getMessage());
            }
        } else {
            log.warn("⚠️ Unity non connecté — inputs ignorés");
        }
    }
}