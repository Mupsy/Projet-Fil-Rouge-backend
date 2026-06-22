package com.drone.backend.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class UnityWebSocketHandler extends AbstractWebSocketHandler {

    private static final int MAX_BUFFER_SIZE = 10 * 1024 * 1024; // 10 MB

    private final SessionRegistry registry;

    private final AtomicLong framesSent     = new AtomicLong(0);
    private final AtomicLong framesReceived = new AtomicLong(0);
    private final AtomicLong bytesSent      = new AtomicLong(0);
    private final AtomicLong bytesReceived  = new AtomicLong(0);

    public UnityWebSocketHandler(SessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // ✅ Fix erreur 1009 : augmenter le buffer directement sur la session
        log.info("SESSION CREATED");

        session.setBinaryMessageSizeLimit(MAX_BUFFER_SIZE);
        session.setTextMessageSizeLimit(MAX_BUFFER_SIZE);

        registry.registerUnity(session);
        log.info("🎮 [UNITY] Connexion établie — sessionId={} remoteAddr={} bufferMax={}MB",
                session.getId(),
                session.getRemoteAddress(),
                MAX_BUFFER_SIZE / 1024 / 1024);
        log.debug("[UNITY] Headers: {}", session.getHandshakeHeaders());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.clearUnity();
        log.info("🎮 [UNITY] Connexion fermée — sessionId={} code={} reason='{}' | Stats: reçues={} envoyées={} bytesReçus={} bytesEnvoyés={}",
                session.getId(),
                status.getCode(),
                status.getReason(),
                framesReceived.get(),
                framesSent.get(),
                bytesReceived.get(),
                bytesSent.get());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("🎮 [UNITY] ❌ Erreur transport — sessionId={} erreur='{}'",
                session.getId(),
                exception.getMessage(),
                exception);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        log.info("Frame reçue : {} octets", message.getPayloadLength());
        int frameSize = message.getPayload().remaining();
        framesReceived.incrementAndGet();
        bytesReceived.addAndGet(frameSize);

        log.debug("🎮 [UNITY] Frame JPG reçue — taille={}KB mobileConnectés={}",
                frameSize / 1024,
                registry.getMobileCount());

        int sent = 0;
        int errors = 0;

        for (WebSocketSession mobile : registry.getMobileSessions()) {
            if (mobile.isOpen()) {
                try {
                    log.info("Envoi vers mobile : {} octets", frameSize);
                    mobile.sendMessage(new BinaryMessage(message.getPayload().duplicate()));
                    sent++;
                    framesSent.incrementAndGet();
                    bytesSent.addAndGet(frameSize);
                } catch (IOException e) {
                    errors++;
                    log.error("🎮 [UNITY] ❌ Erreur relay frame → mobile={} erreur='{}'",
                            mobile.getId(), e.getMessage());
                }
            } else {
                log.warn("🎮 [UNITY] ⚠️ Session mobile fermée ignorée — mobileId={}",
                        mobile.getId());
            }
        }

        log.debug("🎮 [UNITY] Relay résultat — envoyés={} erreurs={}", sent, errors);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.debug("🎮 [UNITY] Message texte reçu — payload='{}'", message.getPayload());
        for (WebSocketSession mobile : registry.getMobileSessions()) {
            if (mobile.isOpen()) {
                try {
                    mobile.sendMessage(message);
                } catch (IOException e) {
                    log.error("🎮 [UNITY] ❌ Erreur relay texte → mobile={} erreur='{}'",
                            mobile.getId(), e.getMessage());
                }
            }
        }
    }

    public void forwardInputsToUnity(String inputsJson) {
        WebSocketSession unity = registry.getUnitySession();

        if (unity == null) {
            log.warn("📱→🎮 [FORWARD] ⚠️ Unity non connecté — inputs ignorés");
            return;
        }
        if (!unity.isOpen()) {
            log.warn("📱→🎮 [FORWARD] ⚠️ Session Unity fermée — inputs ignorés");
            registry.clearUnity();
            return;
        }

        try {
            unity.sendMessage(new TextMessage(inputsJson));
            log.debug("📱→🎮 [FORWARD] Inputs envoyés: '{}'", inputsJson);
        } catch (IOException e) {
            log.error("📱→🎮 [FORWARD] ❌ Erreur envoi → Unity: '{}'", e.getMessage(), e);
        }
    }
}