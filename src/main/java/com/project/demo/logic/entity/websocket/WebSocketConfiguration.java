package com.project.demo.logic.entity.websocket;

import com.project.demo.logic.entity.bid.BidService;
import com.project.demo.logic.entity.chat.ChatService;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
import com.project.demo.websocket.auction.AuctionWebSocketController;
import com.project.demo.websocket.chat.ChatWebSocketController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * Configuración para WebSocket en la aplicación.
 * Activa soporte para WebSocket y registra un manejador específico para el chat.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final ChatService chatService;
    private final BidService bidService;
    private final TblPublicationRepository tblPublicationRepository;

    /**
     * Constructor que inicializa el servicio de chat.
     *
     * @param chatService              servicio para manejar las operaciones del chat.
     * @param bidService               servicio para manejar las operaciones de subastas.
     * @param tblPublicationRepository repositorio necesario para las operaciones de publicaciones.
     */
    public WebSocketConfiguration(
            ChatService chatService,
            BidService bidService,
            TblPublicationRepository tblPublicationRepository
    ) {
        this.chatService = chatService;
        this.bidService = bidService;
        this.tblPublicationRepository = tblPublicationRepository;
    }

    /**
     * Registra el manejador WebSocket para cada funcionalidad y sus rutas".
     *
     * @param registry registro donde se agregan los manejadores para los websockets.
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        //configuración para el chat
        registry.addHandler(chatWebSocketHandler(), "/chat")
                .setAllowedOrigins("*");

        //configuración para las subastas
        registry.addHandler(auctionWebSocketHandler(), "/auction-ws")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }

    /**
     * Define el manejador chatWebSocketHandler como un bean para el chat.
     *
     * @return manejador WebSocket para el chat.
     */
    @Bean
    public WebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketController(chatService);
    }


    /**
     * Define el manejador auctionWebSocketHandler  como un bean para la subasta.
     *
     * @return manejador WebSocket para la subasta
     */
    @Bean
    public WebSocketHandler auctionWebSocketHandler() {
        return new AuctionWebSocketController(bidService);
    }
}