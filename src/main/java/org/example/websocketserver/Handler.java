package org.example.websocketserver;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@EnableScheduling
@Slf4j
public class Handler extends TextWebSocketHandler{

    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session){
        sessions.add(session);
        log.info("New connection {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message){
        log.info("Session id {}, message from client: {}", session.getId(), message.getPayload());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, CloseStatus status){
        sessions.remove(session);
        log.info("Close connection {}, reason: {}", session.getId(), status.getReason());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception){
        log.error("Error on session {}, exception message {}", session.getId(), exception.getMessage());
        sessions.remove(session);
    }

    @Scheduled(fixedRate = 5000)
    public void sendMessages(){
        if (sessions.isEmpty()) {
            return;
        }
        String message = "Hello world!";
        for(WebSocketSession session : sessions){
            if (session.isOpen()){
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("Session id {}, error sending message {}", session.getId(), e.getMessage());
                    sessions.remove(session);
                }
            }else{
                sessions.remove(session);
            }
        }
        log.info("Message sent: {}", message);
    }
}
