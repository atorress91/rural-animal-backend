package com.project.demo.websocket.auction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.demo.logic.entity.bid.BidService;
import com.project.demo.logic.entity.bid.TblBid;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controlador WebSocket para manejar las operaciones de subasta en tiempo real.
 */
@Component
public class AuctionWebSocketController extends TextWebSocketHandler {

    private final BidService bidService;
    private final ObjectMapper objectMapper;
    private final Map<Long, Set<WebSocketSession>> auctionSubscriptions;

    /**
     * Crea un nuevo controlador WebSocket para subastas.
     *
     * @param bidService el servicio de pujas
     */
    public AuctionWebSocketController(BidService bidService) {
        this.bidService = bidService;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.auctionSubscriptions = new ConcurrentHashMap<>();
    }

    /**
     * Maneja la conexión WebSocket una vez establecida.
     *
     * @param session la sesión WebSocket
     * @throws Exception si ocurre un error durante el manejo de la conexión
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {

        Long auctionId = getAuctionIdFromSession(session);
        if (auctionId != null) {
            auctionSubscriptions.computeIfAbsent(auctionId, k -> ConcurrentHashMap.newKeySet()).add(session);

            Optional<TblBid> latestBid = bidService.getLatestBidByPublication(auctionId);
            if (latestBid.isPresent()) {
                Map<String, Object> update = new HashMap<>();
                update.put("action", "bidUpdate");
                update.put("publicationId", auctionId);
                update.put("bidAmount", latestBid.get().getBidAmmount());
                update.put("userId", latestBid.get().getUser().getId());
                update.put("bidderName", latestBid.get().getUser().getName());
                update.put("bidDate", latestBid.get().getBidDate());

                String message = objectMapper.writeValueAsString(update);
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    /**
     * Maneja los mensajes de texto recibidos a través del WebSocket.
     *
     * @param session la sesión WebSocket
     * @param message el mensaje recibido
     * @throws Exception si ocurre un error durante el manejo del mensaje
     */
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String action = (String) payload.get("action");

        if ("placeBid".equals(action)) {
            Long publicationId = Long.valueOf(payload.get("publicationId").toString());
            Long userId = Long.valueOf(payload.get("userId").toString());

            try {
                Map<String, Object> bid = bidService.placeBid(publicationId, userId);

                broadcastBidUpdate(publicationId, bid);
            } catch (Exception e) {

                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("action", "error");
                errorResponse.put("message", e.getMessage());
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
            }
        } else if ("subscribe".equals(action)) {

            Long publicationId = Long.valueOf(payload.get("publicationId").toString());
            auctionSubscriptions.computeIfAbsent(publicationId, k -> ConcurrentHashMap.newKeySet()).add(session);
        }
    }

    /**
     * Maneja la desconexión del WebSocket.
     *
     * @param session la sesión WebSocket
     * @param status  el estado de cierre
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {

        auctionSubscriptions.values().forEach(sessions -> sessions.remove(session));
    }

    /**
     * Difunde la actualización de una puja a todos los suscriptores de la subasta.
     *
     * @param publicationId el ID de la publicación
     * @param bidInfo       la información de la puja
     * @throws Exception si ocurre un error durante la difusion de la actualización
     */
    private void broadcastBidUpdate(Long publicationId, Map<String, Object> bidInfo) throws Exception {
        Set<WebSocketSession> sessions = auctionSubscriptions.getOrDefault(publicationId, Collections.emptySet());
        Map<String, Object> update = new HashMap<>();
        update.put("action", "bidUpdate");
        update.put("publicationId", publicationId);
        update.put("bidAmount", bidInfo.get("bidAmount"));
        update.put("userId", bidInfo.get("bidderId"));
        update.put("bidderName", bidInfo.get("bidderName"));
        update.put("bidDate", bidInfo.get("bidDate"));

        String message = objectMapper.writeValueAsString(update);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    /**
     * Obtiene el ID de la subasta de la sesión WebSocket.
     *
     * @param session la sesión WebSocket
     * @return el ID de la subasta, o null si no se puede obtener
     */
    private Long getAuctionIdFromSession(WebSocketSession session) {

        String query = Objects.requireNonNull(session.getUri()).getQuery();
        if (query != null) {
            Map<String, String> params = Arrays.stream(query.split("&"))
                    .map(s -> s.split("="))
                    .filter(s -> s.length == 2)
                    .collect(HashMap::new, (m, s) -> m.put(s[0], s[1]), HashMap::putAll);

            if (params.containsKey("auctionId")) {
                return Long.valueOf(params.get("auctionId"));
            }
        }
        return null;
    }
}
