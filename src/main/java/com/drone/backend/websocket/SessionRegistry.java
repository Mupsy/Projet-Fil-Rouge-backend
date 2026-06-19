
package com.drone.backend.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class SessionRegistry {

    private volatile WebSocketSession unitySession;

    private final Set<WebSocketSession> mobileSessions =
            ConcurrentHashMap.newKeySet();

    public void registerUnity(WebSocketSession session) {
        this.unitySession = session;
    }

    public void clearUnity() {
        this.unitySession = null;
    }

    public WebSocketSession getUnitySession() {
        return unitySession;
    }

    public boolean isUnityConnected() {
        return unitySession != null && unitySession.isOpen();
    }

    public void addMobile(WebSocketSession session) {
        mobileSessions.add(session);
    }

    public void removeMobile(WebSocketSession session) {
        mobileSessions.remove(session);
    }

    public Set<WebSocketSession> getMobileSessions() {
        return mobileSessions;
    }

    public int getMobileCount() {
        return mobileSessions.size();
    }
}
