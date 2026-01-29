package com.project.demo.websocket.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo.logic.entity.chat.ChatService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controlador WebSocket para manejar sesiones de chat y mensajes en tiempo real.
 */
@Component
@Slf4j
public class ChatWebSocketController extends TextWebSocketHandler {

    private final ChatService chatService;
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    /**
     * Constructor que inicializa el servicio de chat.
     *
     * @param chatService servicio que gestiona el procesamiento de mensajes de chat.
     */
    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Se ejecuta al establecerse una conexión WebSocket.
     *
     * @param session la sesión WebSocket establecida.
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String userId = session.getId();
        userSessions.put(userId, session);
        log.info("New WebSocket connection established for user: {}", userId);
    }

    /**
     * Maneja los mensajes de texto recibidos del usuario.
     *
     * @param session la sesión WebSocket del usuario.
     * @param message el mensaje de texto recibido.
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        try {
            log.info("Message received: {}", message.getPayload());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(message.getPayload());

            String content = jsonNode.get("content").asText();
            Long userId = jsonNode.get("userId").asLong();

            String sessionId = session.getId();
            sessionUserMap.putIfAbsent(sessionId, userId);

            String response = chatService.processMessage(content, userId);
            sendMessageToUser(sessionId, response);
        } catch (Exception e) {
            log.error("Error processing message: ", e);
            try {
                session.sendMessage(new TextMessage("Error processing your message"));
            } catch (IOException ex) {
                log.error("Error sending error message", ex);
            }
        }
    }

    /**
     * Se ejecuta al cerrar una conexión WebSocket.
     *
     * @param session la sesión WebSocket cerrada.
     * @param status el estado de cierre.
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String sessionId = session.getId();
        Long userId = sessionUserMap.get(sessionId);

        userSessions.remove(sessionId);
        sessionUserMap.remove(sessionId);

        if (userId != null) {
            chatService.resetChat(userId);
        }

        log.info("WebSocket connection closed for session: {}, userId: {}", sessionId, userId);
    }

    /**
     * Envía un mensaje específico a un usuario.
     *
     * @param userId el ID del usuario al que se enviará el mensaje.
     * @param message el contenido del mensaje a enviar.
     */
    public void sendMessageToUser(String userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("Error sending message to user {}: ", userId, e);
            }
        }
    }
}