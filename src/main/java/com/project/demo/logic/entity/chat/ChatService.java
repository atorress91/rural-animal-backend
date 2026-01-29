package com.project.demo.logic.entity.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio para procesar mensajes de chat utilizando Gemini y ejecutar comandos específicos.
 */
@Service
@Slf4j
public class ChatService {

    private final GeminiClient geminiClient;
    private final CommandInvoker commandInvoker;
    private final ChatContextManager contextManager;

    /**
     * Constructor que inicializa el cliente de Gemini y el invocador de comandos.
     *
     * @param geminiClient   el cliente para interactuar con Gemini.
     * @param commandInvoker el invocador para ejecutar comandos específicos.
     */
    public ChatService(GeminiClient geminiClient, CommandInvoker commandInvoker,ChatContextManager contextManager) {
        this.geminiClient = geminiClient;
        this.commandInvoker = commandInvoker;
        this.contextManager = contextManager;
    }

    /**
     * Procesa el mensaje recibido y determina la respuesta apropiada.
     *
     * @param message el mensaje a procesar.
     * @return la respuesta generada, o un mensaje de error si ocurre una excepción.
     */
    public String processMessage(String message, Long userId) {
        try {
            ChatContextManager.ConversationContext context = contextManager.getOrCreateContext(userId);

            if (!context.isInitialized()) {
                String welcomeMessage = geminiClient.startChat();
                context.setInitialized(true);
                context.updateLastInteraction();
                contextManager.updateContext(userId, context);
                return welcomeMessage;
            }

            Intent intent = geminiClient.analyzeIntent(message, userId);

            return commandInvoker.executeCommand(intent, message, userId);

        } catch (Exception e) {
            log.error("Error processing the message", e);
            return "Lo siento, hubo un error procesando tu mensaje. ¿Podrías intentarlo de nuevo?";
        }
    }

    /**
     * Reinicia el estado del chat, incluyendo el historial de mensajes.
     */
    public void resetChat(Long userId) {
        geminiClient.resetMessageHistory(userId);
        contextManager.clearContext(userId);
    }
}